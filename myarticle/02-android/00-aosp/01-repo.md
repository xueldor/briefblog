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

```shell
#从我们公司内网服务器上下载iov2.0_plus这个分支代码
1. repo init -u ssh://xxx@192.168.20.45:29418/Android/imx8x_p/platform/manifest -b iov2.0_plus -m default.xml --repo-url=ssh://xxx@192.168.20.45:29418/tools/repo
2. repo sync -dcq
```

下载原生的aosp代码，通常会从国内镜像下载，有清华源、中科大源、北京外国语大学等。感觉好像中科大源比清华源更快一点，北外没试过。奇怪的是我从中科大源repo sync时出现了几个网络异常，重试还是出现，但是貌似并没有影响我编译，应该是可以无视的。不放心的话就用清华源吧。

从清华源下载参考：https://mirrors.tuna.tsinghua.edu.cn/help/AOSP/

从中科大下载参考： https://lug.ustc.edu.cn/wiki/mirrors/help/aosp/

北京外国语大学镜像站：https://mirrors.bfsu.edu.cn/help/AOSP/

另外还有一些第三方的 Android 发行版，比如Lineage OS，也可以从清华的镜像下载：

```
repo init -u https://mirrors.tuna.tsinghua.edu.cn/git/lineageOS/LineageOS/android.git -b cm-14.1
```

初始化好后需要修改.repo/manifest.xml。教程参考[lineageOS 源代码镜像使用帮助](https://mirrors.tuna.tsinghua.edu.cn/help/lineageOS/)

## Repo命令

有关git和repo的命令使用方法，因为之前写过笔记记录，放在我的GitHub的StudyNotes仓库，这里不重复写了。

repo命令参考官方资料https://source.android.google.cn/setup/develop/repo?hl=zh-cn

另外可以命令行下用--help查询：

```shell
work@S111:~/aosp$ repo -help
Usage: repo [-p|--paginate|--no-pager] COMMAND [ARGS]

Options:
  -h, --help      show this help message and exit
  -p, --paginate  display command output in the pager
  --no-pager      disable the pager
  --color=COLOR   control color usage: auto, always, never
  --trace         trace git command execution
  --time          time repo command execution
  --version       display this version of repo
  
work@S111:~/aosp$ repo -p
usage: repo COMMAND [ARGS]
The most commonly used repo commands are:
  abandon        Permanently abandon a development branch
  branch         View current topic branches
  branches       View current topic branches
  checkout       Checkout a branch for development
  cherry-pick    Cherry-pick a change.
  diff           Show changes between commit and working tree
  diffmanifests  Manifest diff utility
  download       Download and checkout a change
  grep           Print lines matching a pattern
  info           Get info on the manifest branch, current branch or unmerged branches
  init           Initialize repo in the current directory
  list           List projects and their associated directories
  overview       Display overview of unmerged project branches
  prune          Prune (delete) already merged topics
  rebase         Rebase local branches on upstream branch
  smartsync      Update working tree to the latest known good revision
  stage          Stage file(s) for commit
  start          Start a new branch for development
  status         Show the working tree status
  sync           Update working tree to the latest revision
  upload         Upload changes for code review
See 'repo help <command>' for more information on a specific command.
See 'repo help --all' for a complete list of recognized commands.

work@S111:~/aosp$ repo sync -h
Usage: repo sync [<project>...]

Options:
  -h, --help            show this help message and exit
  -f, --force-broken    continue sync even if a project fails to sync
  --force-sync          overwrite an existing git directory if it needs to
                        point to a different object directory. WARNING: this
                        may cause loss of data
  -l, --local-only      only update working tree, don't fetch
  -n, --network-only    fetch only, don't update working tree
  -d, --detach          detach projects back to manifest revision
  -c, --current-branch  fetch only current branch from server
  -q, --quiet           be more quiet
  -j JOBS, --jobs=JOBS  projects to fetch simultaneously (default 4)
  -m NAME.xml, --manifest-name=NAME.xml
                        temporary manifest to use for this sync
  --no-clone-bundle     disable use of /clone.bundle on HTTP/HTTPS
  -u MANIFEST_SERVER_USERNAME, --manifest-server-username=MANIFEST_SERVER_USERNAME
                        username to authenticate with the manifest server
  -p MANIFEST_SERVER_PASSWORD, --manifest-server-password=MANIFEST_SERVER_PASSWORD
                        password to authenticate with the manifest server
  --fetch-submodules    fetch submodules from server
  --no-tags             don't fetch tags
  --optimized-fetch     only fetch projects fixed to sha1 if revision does not
                        exist locally
  --prune               delete refs that no longer exist on the remote
  -s, --smart-sync      smart sync using manifest from the latest known good
                        build
  -t SMART_TAG, --smart-tag=SMART_TAG
                        smart sync using manifest from a known tag

  repo Version options:
    --no-repo-verify    do not verify repo source code
    
work@S111:~/aosp$ repo help sync
Summary
-------
Update working tree to the latest revision

Usage: repo sync [<project>...]
。。。。。。省略

```

上面几个常用的：

-l :只检出，不下载

-c ：只同步当前的分支

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