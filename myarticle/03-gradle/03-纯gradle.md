我研究gradle 的初衷是，android studio默认用gradle作为项目构建工具。但是现在还是不要考虑安卓的情况，研究一下“纯粹的gradle”是怎么回事，否则到后面，你搞不清项目里的配置脚本哪些是gradle的基础特性，哪些是android 插件引入的。

## gradle 是什么

*  **gradle 是一个自动化构建工具**

  gradle 是通过组织一系列 task 来最终完成自动化构建的，所以 task 是 gradle 里最重要的概念
  以生成一个可用的 apk 为例，整个过程要经过 资源的处理，javac 编译，dex 打包，apk 打包，签名等等步骤，每个步骤就对应到 gradle 里的一个 task

* **gradle 脚本里使用 groovy 或者 kotlin DSL**

  以前都是用的groovy，Gradle5.0 新增了 *Kotlin* DSL 1.0 。毕竟Kotlin有一个好爹，在Google的推动下，说不定将来能代替groovy在gradle中使用。暂时没兴趣研究 *Kotlin* DSL。

* **gradle 自身基于 groovy 编写**

  *.gradle 脚本的本质就是类的定义，一些配置项的本质都是方法调用，参数是后面的 {} 闭包

  比如 build.gradle 对应 Project 类，buildScript 对应 Project.buildScript 方法

## 基本组件

* Project： 每一个待编译的工程都叫一个 Project，拥有一个build.gradle

  利用gradle projects命令查看project信息：

  ```shell
  E:\14workspace\ccs2.0_plus_qm\SecondaryRender>gradlew.bat projects
  Starting a Gradle Daemon, 1 incompatible Daemon could not be reused, use --status for details
  
  > Task :projects
  
  ------------------------------------------------------------
  Root project
  ------------------------------------------------------------
  
  Root project 'SecondaryRender'
  \--- Project ':app'
  ```

  这个输出结果表明，此项目有名叫SecondaryRender，里面有包含了一个名叫app的project。对应到android studio里面，SecondaryRender就是你新建的工程，而app是你在工程下添加的模块。

* Task： 项目的构建本质上就是执行一系列的 Task。

  > 比如Android APK的构建过程包含**Java 源码编译 Task、资源编译 Task、JNI 编译 Task、lint 检查 Task、打包生成 APK 的 Task、签名 Task 等**。这些Task是在**插件**中定义的。

  执行task命令：> gradle task_name。所以以后编译release版本的apk你可以用shell：`> ./gradlew assembleRelease`，跟菜单里点击build是一样的。


## Gradle Wrapper

  为了用gradle构建项目，你要下载gradle-xx--version.zip,配置环境变量。现实问题是，不同项目可能用不同版本的gradle，团队开发中也需要统一gradle版本。因此现实项目中往往采用Wrapper的方式。Wrapper是一个shell脚本，window下叫gradlew.bat。AS新建的项目默认就是wrapper模式。

### 生成wrapper

项目目录下打开shell，执行“wrapper”这个task：`gradle wrapper`,然后看到目录下多了几个文件：

![image-20210319191042546](03-纯gradle/image-20210319191042546.png)

gradle文件夹里面是wrapper/gradle-wrapper.jar 和 wrapper/gradle-wrapper.properties 两个文件。

然后，执行task的命令改成"gradlew task_name"或"gradlew.bat task_name"。

一旦生成这些wrapper文件，你就不再依赖过去配置的gradle home了。比如你可以把这个目录拷贝到一个没有配置过任何gradle的电脑上，用gradlew命令。

gradle-wrapper.properties的内容：

```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-4.10.1-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

在执行任何task之前，你先打开这个文件，把distributionUrl改成你需要的gradle版本。

然后在执行task时，自动下载zip到 "home/.gradle/wrapper/dists/gradle-xxx-all/3221gyojl5jsh0helicew7rwx"（后面那个路径名因版本而异）。所有依赖的资源都会自动下载。

因为国内网络问题，有时下载很慢，你可以提前准备好zip，拷贝到这个目录。

### 配置wrapper Task

新建build.gradle,自定义gradle版本，代码如下：

```groovy
//5.0之前的版本
task wrapper(type: Wrapper){
	gradleVersion = '3.0'
}
//5.0以后
wrapper{
    gradleVersion = '3.0'
    //or 直接指定distributionUrl，但是这么写不会验证url是否有效
    distributionUrl = 'https://services.gradle.org/distributions/gradle-5.4.1-all.zip'
}
```

然后执行gradle wrapper，生成的gradle-wrapper.properties变成3.0版本了：

```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-3.0-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

还可以用参数：`gradlew.bat wrapper --gradle-version 2.4`,这样distributionUrl就是2.4版本了。

## 日志级别

gradle -q task  //quiet

gradle -i task   //info

gradle -d task  //debug,一般会输出所有日志

## 输出错误堆栈信息

gradle -s  task  //关键的堆栈信息

gradle -S  task  //输出全部堆栈信息，不推荐，因为太多太长

## 自己打印日志

* println '一段日志'
* logger.quiet('quiet级别的日志')
* logger.error('error级别的日志')
* logger.warn('warn级别的日志')
* logger.lifecycle('lifecycle级别的日志')
* logger.info('info级别的日志')
* logger.debug('debug级别的日志')

## 使用帮助

gradle -h //gradle --help

如果要查看某个task的帮助，使用gradle help --task taskname。比如：

```shell
E:\14workspace\testgroovy>gradle help --task wrapper

> Configure project :
xxxxxxxxxxxxxx

> Task :help
Detailed task information for wrapper

Path
     :wrapper

Type
     Wrapper (org.gradle.api.tasks.wrapper.Wrapper)

Options
     --distribution-type     The type of the Gradle distribution to be used by the wrapper.
                             Available values are:
                                  ALL
....................
```

## 查看所有task

gradle tasks

## 强制刷新缓存

依赖资源都是有缓存的。在命令行运行时可以加上--refresh-dependencies参数强制刷新。比如：

./gradlew --refresh-dependencies assemble

这个功能IDE是很难做到的，这就是命令行的优势。

## 多任务调用

gradle clean jar  //先执行clean，在执行jar

