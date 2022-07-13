皆知刷机分为线刷和卡刷。卡刷指把update.zip放到sdcard，然后进recovery升级。线刷就是通过fastboot刷入系统。线刷的镜像包我们常常称为“烧录包”。

“烧录包”其实就是编译源码生成的各个分区镜像和其它资源。里面肯定有一个boot.img、bootloader.img。后者自然是bootloader，前者就是内核。

## Linux内核文件

Linux内核，编译可以生成不同格式的映像文件，例如：

```shell
# make zImage
# make uImage
```

下面介绍几种linux内核文件的格式：

* vmlinux 编译出来的最原始的内核文件，未压缩。
* zImage  是vmlinux经过gzip压缩后的文件
* bzImage bz表示“big zImage”，不是用bzip2压缩的。
* uImage  U-boot专用的映像文件，它是在zImage之前加上一个长度为0x40的tag。
* *vmlinuz*   “vm”代表 “Virtual Memory”，位于/boot/vmlinuz，一般是一个软链接，指向bzImage或zImage的链接

## boot.img解包

既然boot.img是内核，那么必然也是上面几种格式的一种。实时上，Android 产品中，内核格式是Linux标准的zImage**，**根文件系统采用ramdisk格式。这两者在Android下是直接合并在一起取名为boot.img,会放在一个独立分区(boot分区)当中，这个分区格式是Android自行制定的格式。

boot.img的格式比较简单，它主要分为**三大块**（有的可能有四块）：

> +—————–+ 
> | boot header | 1 page 
> +—————–+ 
> | kernel | n pages 
> +—————–+ 
> | ramdisk | m pages 
> +—————–+ 
> | second stage | o pages 
> +—————–+ 
> n = (kernel_size + page_size – 1) / page_size 
> m = (ramdisk_size + page_size – 1) / page_size 
> o = (second_size + page_size – 1) / page_size 
>
> 0. all entities are page_size aligned in flash 
> 1. kernel and ramdisk are required (size != 0) 
> 2. second is optional (second_size == 0 -> no second) 

boot.img的制作工具是mkbootimg，源码位于system/core/mkbootimg/ 。也可以从网上找到许多别人写的分析工具，如linux shell脚本的repack-zImage，C语言编写的 unbootimg。

>  unpackbootimg -i .\tmp\boot.img -o .\out 


它会解压出如下文件：

boot.img-zImage (内核文件）

boot.img-ramdisk.gz (根文件系统打包文件） 
boot.img-cmdline (mkbootimg cmdline参数) 
boot.img-pagesize (mkbootimg pagesize参数) 
boot.img-base (mkbootimg base参数)

**mkbootimg （Android自带）**常见的命令格式：

```shell
./mkbootimg --cmdline 'no_console_suspend=1 console=null' --kernel zImage --ramdisk boot/boot.img-ramdisk.gz -o boot.img --base 02e00000 
```

这句含义是把内核文件zImage和boot目录下的根文件压缩包 boot.img-ramdisk.gz打包成boot.img. 
其中cmdline和base的值均来源于unpackbootimg的结果 。

## boot.img-ramdisk.gz

boot.img-zImage是内核，boot.img-ramdisk.gz就是ramdisk。

file ramdisk.gz命令查看：

```shell
$ file ramdisk.gz 
ramdisk.gz: u-boot legacy uImage, ramdisk, Linux/ARM, RAMDisk Image (Not compressed), 22077065 bytes, Wed Feb 20 05:44:29 2019, Load Address: 0x88080000, Entry Point: 0x88080000, Header CRC: 0xB56EB752, Data CRC: 0xFED1A0FD
```

dumpimage -l ramdisk.gz命令查看：

```shell
$ dumpimage -l ramdisk.gz
Image Name:   ramdisk
Created:      Wed Feb 20 13:44:29 2019
Image Type:   ARM Linux RAMDisk Image (uncompressed)
Data Size:    22077065 Bytes = 21559.63 kB = 21.05 MB
Load Address: 88080000
Entry Point:  88080000
```

可知ramdisk.gz实际为ramdisk.img，增加了u-boot头，64Bytes大小。我们用dd命令去掉u-boot头，然后再用file ramdisk命令查看：

```shell
$ sudo dd if=ramdisk.gz of=ramdisk bs=64 skip=1
344954+1 records in
344954+1 records out
22077065 bytes (22 MB, 21 MiB) copied, 0.497012 s, 44.4 MB/s

$ file ramdisk
ramdisk: gzip compressed data, last modified: Wed Feb 20 05:44:14 2019, from Unix
```

可知此时ramdisk为一个gzip压缩的文件。修改ramdisk为*.gz文件格式，并使用gunzip ramdisk.gz命令解压，并用file ramdisk命令查看如下：

```shell
$ mv ramdisk ramdisk.gz
$ gunzip ramdisk.gz 
$ file ramdisk 
ramdisk: ASCII cpio archive (SVR4 with no CRC)
```

可知解压出的ramdisk为一个cpio格式的压缩包。于是我们用cpio 命令将ramdisk文件里的文件解压出来：

```shell
$ mkdir tmp
 $ cd tmp/
 $ sudo cpio -idv < ../ramdisk
 $ ls
bin  boot  dev  etc  init  lib  lib32  libexec  linuxrc  media  mnt  opt  proc  root  run  sbin  sys  tmp  usr  var
```

至此，我们看到了linux虚拟文件系统根目录的各个文件。



我们可以在虚拟文件系统目录里修改内容，然后打包回ramdisk.gz:

1. `find . |cpio -ov -H newc |gzip > ../ramdisk.img`

   此时已经打包为gzip格式.

2. mkimage命令添加uboot头，由于上面显示没有压缩格式，所以打包时选择none

   ```shell
   $ mkimage -n "ramdisk" -A arm -O linux -T ramdisk -C none -a 88080000 -e 88080000 -d ramdisk.img ramdisk.gz
   Image Name:   ramdisk
   Created:      Wed Feb 20 14:31:13 2019
   Image Type:   ARM Linux RAMDisk Image (uncompressed)
   Data Size:    50200576 Bytes = 49024.00 kB = 47.88 MB
   Load Address: 88080000
   Entry Point:  88080000
   ```

3. 此时已全部还原ramdisk.gz。接着可以用mkbootimg生成boot.img。