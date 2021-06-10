# 应用高CPU问题分析

1. ps -A | grep com.example.demoandroid 获取进程id

![1598959224(1)](07-案例2-traceview/clip_image001.png)

2. top -H -p processid查看哪个线程繁忙
    ![1598959272(1)](07-案例2-traceview/clip_image002.png)

3. am profile start proccessid /sdcard/tt2.trace 
    ![1598959387(1)](07-案例2-traceview/clip_image002-1623312440724.png)

4. am profile stop processid

![1598959454(1)](07-案例2-traceview/clip_image001-1623312457927.png)

5. 进入sdcard,查看tt2.trace文件

![1598959561(1)](07-案例2-traceview/clip_image002-1623312472275.png)

6. adb pull /sdcard/tt2.trace 到本地电脑

7.  将tt2.trace拖进到Android Studio中(eclipse会出现打开不了问题)

![1598960837(1)](07-案例2-traceview/clip_image002-1623312520276.png)

鼠标在底下部分停留，可以查看调用什么方法，耗时多久

![1598961529(1)](07-案例2-traceview/clip_image002-1623312534794.png)

 

发现MainActivity.sendMsg方法一直在调用，结合代码，发现sendMsg调用存在死循环逻辑

![1598961652(1)](07-案例2-traceview/clip_image001-1623312546711.png)