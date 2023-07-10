（另见 笔记记录-android hal-uevent.md）

这里解释一下uevent和NetlinkEvent。uevnet是内核产生的事件，用于设备驱动程序特定的事件通知机制。本身是一个对象，通过Netlink套接字发送给用户空间。Netlink是通信机制，不是仅仅能传递uevent 。NetlinkEvent是通过Netlink传递的事件对象，更宽泛，可以包含uevent消息，但不限于此。

## 启动

ueventd的代码在init里面。启动时，根据命令行参数判断是init还是ueventd：

```cpp
int main(int argc, char** argv) {
    if (!strcmp(basename(argv[0]), "ueventd")) {
        return ueventd_main(argc, argv);//启动uevntd进程
    }
...
    //启动init进程
    return FirstStageMain(argc, argv);
}
```

启动流程:

```cpp
auto ueventd_configuration = ParseConfig({"/system/etc/ueventd.rc", "/vendor/ueventd.rc",
                                            "/odm/ueventd.rc", "/ueventd." + hardware + ".rc"});
```

ueventd.rc里包含大量类似这样的配置：

```
/dev/ashmem*              0666   root       root
/dev/binder               0666   root       root
/dev/hwbinder             0666   root       root
/dev/vndbinder            0666   root       root
```

ueventd解析后，会创建这些设备，并确保权限和所属用户。

然后是冷启动

```cpp
 if (!android::base::GetBoolProperty(kColdBootDoneProp, false)) {
     ColdBoot cold_boot(uevent_listener, uevent_handlers,
                        ueventd_configuration.enable_parallel_restorecon);
     cold_boot.Run();
 }
```

因为有些设备是一开始就连接在主机上的，这种设备的事件，内核发送uevent比较早，ueventd进程还没启动，所以自然就接收不到。冷启动是解决这个问题的。

```cpp
static const char* kRegenerationPaths[] = {"/sys/devices"};
void UeventListener::RegenerateUevents(const ListenerCallback& callback) const {
    for (const auto path : kRegenerationPaths) {
        if (RegenerateUeventsForPath(path, callback) == ListenerAction::kStop) return;
    }
}

ListenerAction UeventListener::RegenerateUeventsForDir(DIR* d,
                                                       const ListenerCallback& callback) const {
    int dfd = dirfd(d);

    int fd = openat(dfd, "uevent", O_WRONLY | O_CLOEXEC);
    if (fd >= 0) {
        write(fd, "add\n", 4);
        close(fd);
。。。
}
```

冷启动内部的逻辑是，遍历/sys/devices目录，找到uevent文件，往里面写“add\n”，然后就会触发内核重新发送事件。

```cpp
uevent_handlers.emplace_back(std::make_unique<DeviceHandler>(...));
uevent_handlers.emplace_back(std::make_unique<FirmwareHandler>(...));

uevent_listener.Poll([&uevent_handlers](const Uevent& uevent) {
    for (auto& uevent_handler : uevent_handlers) {
        uevent_handler->HandleUevent(uevent);
    }
    return ListenerAction::kContinue;
});
```

从队列poll事件，用DeviceHandler或FirmwareHandler来处理。

## 以ccs5.0插入读卡器为例

一，ueventd部分

关键代码：

```cpp
//system\core\init\ueventd.cpp 
uevent_listener.Poll([&uevent_handlers](const Uevent& uevent) {
    for (auto& uevent_handler : uevent_handlers) {
        uevent_handler->HandleUevent(uevent);
    }
    return ListenerAction::kContinue;
});
```

uevent对象经由system\core\init\uevent_listener.cpp的ParseEvent函数解析得到，打印出来如下：

```
#插入读卡器
#ueventd进程收到以下uevent
action=add,device_name=bus/usb/002/012,subsystem=usb, path=/devices/platform/soc/a800000.hsusb/a800000.dwc3/xhci-hcd.2.auto/usb2/2-1, firmware=, partition_name=, modalias=, major=189, minor=139,partition_num=-1
action=add,device_name=,subsystem=usb, path=/devices/platform/soc/a800000.hsusb/a800000.dwc3/xhci-hcd.2.auto/usb2/2-1/2-1:1.0, firmware=, partition_name=, modalias=usb:v067Bp2731d0100dc00dsc00dp00ic08isc06ip50in00, major=-1, minor=-1,partition_num=-1
action=add,device_name=,subsystem=scsi, path=/devices/platform/soc/a800000.hsusb/a800000.dwc3/xhci-hcd.2.auto/usb2/2-1/2-1:1.0/host1, firmware=, partition_name=, modalias=, major=-1, minor=-1,partition_num=-1
action=add,device_name=,subsystem=scsi_host, path=/devices/platform/soc/a800000.hsusb/a800000.dwc3/xhci-hcd.2.auto/usb2/2-1/2-1:1.0/host1/scsi_host/host1, firmware=, partition_name=, modalias=, major=-1, minor=-1,partition_num=-1
action=bind,device_name=,subsystem=usb, path=/devices/platform/soc/a800000.hsusb/a800000.dwc3/xhci-hcd.2.auto/usb2/2-1/2-1:1.0, firmware=, partition_name=, modalias=usb:v067Bp2731d0100dc00dsc00dp00ic08isc06ip50in00, major=-1, minor=-1,partition_num=-1
action=bind,device_name=bus/usb/002/012,subsystem=usb, path=/devices/platform/soc/a800000.hsusb/a800000.dwc3/xhci-hcd.2.auto/usb2/2-1, firmware=, partition_name=, modalias=, major=189, minor=139,partition_num=-1

#we only关注接下来的两个subsystem=block类型的事件
ueventd: action=add, subsystem=block, , path=/devices/platform/soc/a800000.hsusb/a800000.dwc3/xhci-hcd.2.auto/usb2/2-1/2-1:1.0/host1/target1:0:0/1:0:0:0/block/sdg, device_name=sdg;
[Fri Jun  2 02:35:17 2023] ueventd: add, block, , /devices/platform/soc/a800000.hsusb/a800000.dwc3/xhci-hcd.2.auto/usb2/2-1/2-1:1.0/host1/target1:0:0/1:0:0:0/block/sdg/sdg4 sdg4;

```

uevent_handler有两种：DeviceHandler、FirmwareHandler。找到这两个类的HandleUevent方法，插入读卡器（一张TF卡）的事件是由DeviceHandler处理的。

```cpp
void DeviceHandler::HandleUevent(const Uevent& uevent) {
    ....
    // if it's not a /dev device, nothing to do
    if (uevent.major < 0 || uevent.minor < 0) return;

    std::string devpath;
    std::vector<std::string> links;
    bool block = false;

    if (uevent.subsystem == "block") {
        block = true;
        devpath = "/dev/block/" + Basename(uevent.path);

        if (StartsWith(uevent.path, "/devices")) {
            links = GetBlockDeviceSymlinks(uevent);
        }
    } else
    ....
    HandleDevice(uevent.action, devpath, block, uevent.major, uevent.minor, links);

void DeviceHandler::HandleDevice(const std::string& action, const std::string& devpath, bool block,
                                 int major, int minor, const std::vector<std::string>& links) const {
    if (action == "add") {
        MakeDevice(devpath, block, major, minor, links);//创建/dev/block/sdg设备和/dev/block/sdg4
    }
    ....
}
```

MakeDevice里面会注册设备、设置设备的用户组、权限，涉及makedev、mknod、setegid、chown等系统函数，熟悉linux命令的同学应该一眼能猜出函数的作用。

我再这里添加了一行日志，供大家参考：

```
[Fri Jun  2 02:35:08 2023] ueventd: xxxx usb Dirname(devpath)=/dev/block, devpath=/dev/block/sdg,major=8, minor=96
[Fri Jun  2 02:35:31 2023] ueventd: xxxx usb Dirname(devpath)=/dev/block, devpath=/dev/block/sdg4,major=8, minor=100
```

同时到/dev目录下检查确认：

```shell
# /dev/block目录新增块设备
brw------- 1 root   root     8,  96 2023-06-01 19:01 sdg
brw------- 1 root   root     8, 100 2023-06-01 19:01 sdg4
```

