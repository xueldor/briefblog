修改 `frameworks/native/cmds/atrace/atrace.rc` 中 `service boottrace` 的内容,改成：

```
service boottrace /system/bin/atrace --async_start -b 30720 gfx input view webview wm am sm audio video binder_lock binder_driver camera hal res dalvik rs bionic power pm ss database network adb vibrator aidl sched  
    disabled
    oneshot
```

也可以直接push到车机里。

打开抓取 boottrace 的属性开关

```
adb  shell setprop persist.debug.atrace.boottrace 1
```

因为是persist属性，所以重新开机会记忆。

重启完成之后等待几秒，关闭 boottrace 属性开关：

```
adb  shell setprop persist.debug.atrace.boottrace 0
```

生成和拉取 boottrace 文件:

```
adb shell atrace --async_stop -z -c -o /data/local/tmp/boot_trace
adb  pull /data/local/tmp/boot_trace
```



另外：

system/core/init/perfboot.py

```
./perfboot.py --iterations=5 --interval=30 -v --output=/data/My_Doc/Performance/Bugs/bootup_op_4200151/J5D_UE.tsv
```

还没试过。



