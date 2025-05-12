源码位置:

```
# rc文件
system/core/rootdir目录：
init.zygote64.rc
init.zygote32.rc
init.zygote64_32.rc
init.zygote32_64.rc

# app_process
frameworks/base/cmds/app_process/app_main.cpp
frameworks/base/cmds/app_process/Android.bp

/frameworks/base/core/jni/AndroidRuntime.cpp
/libnativehelper/JniInvocation.cpp 
/frameworks/base/core/java/com/android/internal/os/Zygote.java 
/frameworks/base/core/java/com/android/internal/os/ZygoteInit.java 
/frameworks/base/core/java/com/android/internal/os/ZygoteServer.java 
/frameworks/base/core/java/com/android/internal/os/ZygoteConnection.java 
/frameworks/base/core/java/com/android/internal/os/RuntimeInit.java 

```





init.rc:

```
# /system/etc/init/hw/init.rc
import /system/etc/init/hw/init.${ro.zygote}.rc

console:/system/etc/init/hw # getprop ro.zygote
zygote64
```

/system/etc/init/hw/init.zygote64.rc

```
service zygote /system/bin/app_process64 -Xzygote /system/bin --zygote --start-system-server
    class main
    priority -20
    user root
    group root readproc reserved_disk
    socket zygote stream 660 root system
    socket usap_pool_primary stream 660 root system
    onrestart exec_background - system system -- /system/bin/vdc volume abort_fuse
    onrestart write /sys/power/state on
    onrestart restart audioserver
    onrestart restart cameraserver
    onrestart restart media
    onrestart restart netd
    onrestart restart wificond
    writepid /dev/cpuset/quick-task/tasks
    #writepid /dev/cpuset/foreground/tasks
    critical window=${zygote.critical_window.minute:-off} target=zygote-fatal
```







```java
/*
setpgid(pid_t pid,pid_t pgid)函数性质：
性质1：一个进程只能为自己或子进程设置进程组ID，不能设置其父进程的进程组ID。
性质2：if(pid == pgid), 由pid指定的进程变成进程组长;即进程pid的进程组ID pgid=pid.
性质3：if(pid==0),将当前进程的pid作为进程组ID.
性质4：if(pgid==0),将pid作为进程组ID.
*/
Os.setpgid(0, 0);



```

