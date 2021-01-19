# 介绍repo

## 代码库的管理

> 1. Git: 一种开源版本控制系统
> 2. Repo: Google专门为AOSP写的一个Python脚本，用来管理aosp中的多个 Git 代码库
> 3. Gerrit: 一个基于网页的代码审核系统，适用于使用 Git 的项目
>

## 下载 Repo
1. 对于未安装 Python 3.6 及更高版本的旧系统，适用于旧版 Python 2 系统的旧版 Repo

```shell
curl https://storage.googleapis.com/git-repo-downloads/repo-1 > ~/bin/repo
chmod a+x ~/bin/repo
```

2. 对于安装了Python 3的系统：

   ```shell
   curl https://storage.googleapis.com/git-repo-downloads/repo > ~/bin/repo
   chmod a+x ~/bin/repo
   ```

3. repo文件可以放到~/bin目录，也可以放到/usr/bin/目录。确保添加到PATH。
   比如，你可以在~/.bashrc文件末尾添加：
   ```shell
   export PATH=~/bin:$PATH
   ```

   还需要确保repo文件具有 'x' 权限: `sudo chmod a+x ~/bin/repo`

## Repo使用

下载代码按照以下步骤：

1. repo init -u ssh://xxx@192.168.20.45:29418/Android/imx8x_p/platform/manifest -b iov2.0_plus -m default.xml --repo-url=ssh://xxx@192.168.20.45:29418/tools/repo

2. repo sync -dcq

下载原生的aosp代码，通常会从国内镜像下载，有清华源和中科大源。感觉好像中科大源更快一点。

从清华源下载参考：https://mirrors.tuna.tsinghua.edu.cn/help/AOSP/

从中科大下载参考： https://lug.ustc.edu.cn/wiki/mirrors/help/aosp/

另外还有一些第三方的 Android 发行版，比如Lineage OS，也可以从清华的镜像下载：

```
repo init -u https://mirrors.tuna.tsinghua.edu.cn/git/lineageOS/LineageOS/android.git -b cm-14.1
```

初始化好后需要修改.repo/manifest.xml。教程参考[lineageOS 源代码镜像使用帮助](https://mirrors.tuna.tsinghua.edu.cn/help/lineageOS/)

## Repo命令

有关git和repo的命令使用方法，因为之前写过笔记记录，放在我的GitHub的StudyNotes仓库，这里不重复写了。

repo命令参考官方资料https://source.android.google.cn/setup/develop/repo?hl=zh-cn

## .repo目录

repo sync的过程大致分两步：下载仓库、检出文件。类似于git fetch和git checkout。

所有git仓库都放在.repo目录，也就是说，哪怕你把除了.repo之外的文件全部删除，也可以重新从.repo中检出所有项目。

.repo目录里面有这么几个子目录：

>  manifests  manifests.git  project-objects  projects  repo

对应三种类型的Git仓库，分别是Repo仓库、Manifest仓库和AOSP子项目仓库。

1. Repo仓库

   对应.repo/repo目录，包括检出的文件和.git目录。里面是一些Python脚本，用来操作AOSP的子项目。

2. Manifest仓库

   对应manifests 和manifests.git两个目录。manifests是检出的文件，manifests.git是git仓库。manifests目录里面也有一个.git目录，但里面只是一些指向manifests.git的链接和一些git状态信息。

   Manifest仓库保存了AOSP子项目仓库的元信息(比如总共有哪些子项目)。Repo仓库只有获取到这些元信息，才能知道怎么操作各个AOSP子项目。

3. AOSP子项目仓库

   也就是我们所说的安卓源代码。比如system、framework、vendor、app等。

   在.repo目录中，对应project-objects 、projects两个子目录。实际上真正保存代码的是project-objects目录，而projects目录类似manifests 目录，里面大多是一些链接，指向的还是project-objects目录。

   举个例子，SystemUI模块，以下的目录：

   aosp_dir/frameworks/SystemUI/    -----> 是检出后的代码，git工作区。

   aosp_dir/frameworks/SystemUI/.git   ----->  里面关键的文件都是指向aosp_dir/.repo/project-objects/platform/frameworks/SystemUI.git的

   aosp_dir/.repo/project-objects/platform/frameworks/SystemUI.git   ---->  git仓库，跟你自己git clone下来的仓库的.git目录是一样的。

   aosp_dir/.repo/projects/frameworks/SystemUI.git   --->  作用不清楚，反正里面都是指向上面的那个git仓库的。

了解了这三种仓库，我们就应该理解为什么repo init命令要加一个 `-u` 参数了，-u指定了manifest 的路径。repo只有拿到清单才知道要下载什么。

其实，如果不使用repo工具，也是可以对照manifest.xml文件清单直接使用“git clone”的方式一个project一个project的下载的。