## 背景

James Strachan ，groovy  的发明人，一个成熟的 java 开发人员，他承认他的主攻语言缺乏 Python 中有趣的
和有用的特性，如为通用的数据类型的自然支持，更加重要的是动态行为，他的想法是赋予java 这些有趣的特性。  

groovy 是在 java平台上的、 具有象 Python， Ruby 和 Smalltalk 语言特性的灵活动态语言， groovy 保证了这些
特性象 java 语法一样被 java 开发者使用。  

groovy 与 java 平台无缝集成  ， groovy 的许多代码是使用 java 实现的，其余部分是用groovy 实现的。

groovy编译成class文件，以至于jvm不知道是正在运行的代码是java写的，还是groovy写的。

从 groovy 调用 java 是没有任何问题的 , 包括大量的 java 类库也可以直接在 groovy 中使用。

大部分时候java的代码直接拷贝到groovy里面都是可以直接用的。但也不是100%。尤其是java本身也在发展，新加的机制不能保证groovy一定支持。比如java8的Lambda表达式，Groovy不支持这种语法，而采用闭包来实现。

## Groovy 开发环境

**Ubuntu**

在 Ubuntu 可以借助一些管理sdk的工具：

- curl -s get.gvmtool.net | bash
- source “$HOME/.gvm/bin/gvm-init.sh”
- gvm install groovy

或者

* curl -s get.sdkman.io | bash
* source "$HOME/.sdkman/bin/sdkman-init.sh"
* sdk install groovy

sdkman或者gvm是什么自行百度。其中groovy官网介绍了sdkman，我推荐尽量用官网的方式。

官网地址`https://groovy.apache.org/download.html#distro`，介绍的安装方式还有`$ sudo snap install groovy --classic`等等，自行查阅。

或者也可以直接到官网下载SDK压缩包。我们选择“SDK bundle”,因为包含了doc和src。如果不需要源码和文档，你就下载“binary”。

下载下来后解压，bin目录下的脚本，分为带有后缀.bat的，和不带后缀的。window环境运行bat，linux环境执行不带后者的。

**Windows**

Windows也可以用SDKMAN，也可以直接下载sdk压缩包。还可以选择下载windows安装包。

假设你下载的压缩包，请解压，然后配置GROOVY_HOME，添加到PATH。

cmd里面执行groovy -v测试配置成功。



IntelliJ IDEA可以支持写Groovy，不需要安装任何东西。

## 执行

方法1 命令行下启动groovyConsole，代码拷进去，然后按ctrl+R执行。

​         windows不区分大小写，所以groovyconsole也可以起来。

方法2 新建文件，后缀为groovy（习惯而已，不是强制要求）。保存代码。执行groovy file。

方法3  同方法二，但是先编译成java字节码，然后执行字节码：

​         1） groovyc -d outputDir  Test.groovy

​          2)  java  -classpath "D:\xxx\groovy-all-2.4.17.jar;." Test

## 基础语法

在java的基础上，有以下特性：

- Groovy 语句可以不用分号结尾。
- Groovy 中支持动态类型，即**定义变量的时候可以不指定其类型**。

```groovy
   def variable1 = 1   // 可以不使用分号结尾
   def varable2 = "I am a person"
   def  int x = 1   // 变量定义时，也可以直接指定类型

   //有时你发现不指定def或类型也可能
   yy = 1//但是这其实取决于在哪里。作为Script执行时，放在全局位置，这样写是可以的。
```

* 函数定义时，参数的类型也可以不指定

```groovy
String testFunction(arg1,arg2){// 无需指定参数类型
  ...
}
```

* 函数的返回值可以省略return关键字，返回值也可以是无类型的

```groovy
//不指定返回类型，则必须使用 def 关键字
def  nonReturnTypeFunc(){
     last_line   // 最后一行代码的执行结果就是本函数的返回值
}
 
// 如果指定了函数返回类型，则可不必加 def 关键字来定义函数
String  getString(){
   return "I am a string"
}
```

* 字符串

```groovy
//单引号’'中的内容严格对应 Java 中的 String，不对 $ 符号进行转义
def singleQuote='I am $ dolloar' // 输出就是 I am $ dolloar

//双引号""的内容则和脚本语言的处理有点像，如果字符中有 $ 号的话，则它会$ 表达式先求值
def x = 1,y = 2
def doubleQuoteWithDollar = "I have $x point ${y} dolloar" // 输出 I have 1 point 2 dolloar

//三个引号'''xxx'''中的字符串支持随意换行
def multieLines = ''' begin
     line  1 
     line  2
     end '''

```

* 函数调用的时候可以不加括号

```groovy
println("test") 
//等价于--->
println "test"

//但是如果函数没有参数，不能省略括号
getSomething() // 如果不加括号的话，Groovy 会误认为 getSomething 是一个变量

test (3),7 //不要被括号迷惑，这个括号不是函数后面的，而是(3)整体是一个块，其实就是`test 3,7`,因此也就是test(3,7)
//但是当最后一个参数是闭包时，以下都是一样的
test(2,3,{}) //传统写法
test 2,3,{} //省略括号
test (2,3){} //当函数的最后一个参数是闭包时，调用函数时闭包可以拿到括号后面

//当参数只有一个闭包，把括号省了，就成这样：
test{
xxxx
}//其实就是test({xxxx})
//所以用gradle构建项目时，build.gradle里面有许多这样的写法，例如
defaultConfig {
    applicationId "com.hsae.ccs20.onlinevideo"
    minSdkVersion 19
    targetSdkVersion 27
}
```

* 当调用时，只需要跟一个参数时，如果没有传参数，会自动给null

  ```groovy
  def aa(Integer par)//函数原型
  aa()//OK, par==null
  
  def aa(int par)//函数原型
  aa()//wrong
  ```

  建议不要依赖这些特性，因为不太容易搞清楚什么时候可以省，主动传一个null并不麻烦

* `use`函数 ,使类的静态方法可以作为第一个参数的类上的实例方法使用  
```groovy
//保存为AA.grooby
class AA{
	static def aa(String obj){//静态方法，第一个参数是String
		println obj
	}
}
def iamList = 'hahaha'
use(AA){
	iamList.aa()//iamList就是第一个参数传给obj
    //等价于
    AA.aa(iamList)
}
//然后执行>groovy A.groovy,输出hahaha
```

* 扩展方法

  有如下代码

  ```groovy
  def list = [11, 12, 13, 14]
  println list.getClass()
  list.each {//为什么能调用each方法？
      println "${it}"
  }
  /*
  输出：
  class java.util.ArrayList
  11
  12
  13
  14
  */
  ```

  我们确认list是一个jdk里的ArrayList，但是JDK里的ArrayList并没有each方法。利用IDE点进each内部，发现each方法位于：

  ```groovy
  package org.codehaus.groovy.runtime;
  public class DefaultGroovyMethods extends DefaultGroovyMethodsSupport {
      //........
  	public static <T> List<T> each(List<T> self, @ClosureParams(FirstGenericType.class) Closure closure)
      {
          return (List)each((Iterable)self, closure);
      }
      //....xxxx
  }
  ```

  实际上，groovy为jdk增加了很多扩展方法。我们也可以自己扩展一个类：

  >现在我们为`String`添加一个静态扩展方法，和一个非静态的扩展方法
  >
  >扩展函数语法要求：
  >
  >1. 必须有至少一个参数，且第一个参数类型是扩展的类
  >2. 函数必须为静态
  >
  >编写代码：
  >
  >1.
  >
  >class StringExt {
  >	//扩展实例方法第一个参数就是扩展的实例（固定要第一个参数）。
  >  static containStar(String self, String other) {
  >      return self.contains("*") && other.contains("*")
  >  }
  >}
  >
  >2.
  >
  >class StringStaticExt {
  >  static containX(String self, String other) {
  >  	//静态扩展self必为null
  >      println("self = " +self)
  >      return other.contains("x")
  >  }
  >}
  >
  >在`META-INF/service/org.codehaus.groovy.runtime.ExtensionModule`文件声明扩展函数位置:
  >
  >moduleName = MyStringExt //你的模块名
  >moduleVersion = 1.0  //版本
  >extensionClasses = org.fmy.StringExt //实例扩展函数
  >staticExtensionClasses =org.fmy.StringStaticExt //静态扩展函数
  >
  >最后打成jar包，放入你的工程中，即可写如下代码：
  >
  >> println "asd*sad".containStar("asdsd*") 
  >>
  >> println String.containX("2x")

  前一条介绍的use()函数也是实现扩展的一种方法。

  因为扩展机制的存在，String方法的来源有：

  1. **java.lang.String**原有的方法
  2. **DefaultGroovyMethods**
  3. **StringGroovyMethods**,继承自DefaultGroovyMethods
  4. 你自己的扩展

  

* 正则表达式

  ```groovy
  //~"regex"的形式定义正则表达式
  def reg = ~'a.*b'
  reg.getClass()//Result: class java.util.regex.Pattern
  
  //==~ 精确匹配，中间不能有空格,左边是string，右边是正则表达式，返回一个布尔
  String ss = 'a.*b'//ss是String类型
  def reg = ~'a.*b' //reg是Pattern类型
  "axxb" ==~ reg //这里返回true
  "axxb" ==~ ss //这里返回true
  "axxb" ==~  ~'a.*b'//true
  "axxb" ==~  'a.*b'//true
  "axxbf" ==~  ~'a.*b'//false，因为==~是精确匹配
  
  //=~ 返回是一个Matcher
  ('aaa' =~ 'a').getClass()//Result: class java.util.regex.Matcher
  (boolean)('abcd' =~ 'bc')//true,abcd中能匹配到bc。也因为可转成boolean，故=~表达式可以出现在if或while中
  ('abcd' =~ 'bc').matches()//false，完全匹配
  ```

  

* 其它语法糖

  ```groovy
  'same1' <=> 'same2'//相当于'same1'.compareTo('same2')
  str1 == str2 //相当于str1.equal(str2),java里面比较的是地址，groovy可以直接等号
  //在支持groovy的IDE里，按住ctrl，点击==号，跳转到对应类型的equals方法。比如str1 == str2跳转到String类的equals方法，1==1跳转到Integer的equals方法。这暗示了“==”在groovy里的不同。
  
  5.plus(3)//5+3
  5.mod(3)//5%3
5.0.minus 3//5.0 - 3,省略了函数调用的括号
  ```
  
  

## 数据类型

- 一个是 Java 中的基本数据类型。
- 另外一个是 Groovy 中的容器类。
- 最后一个非常重要的是闭包。

### 容器类

* List：链表，一般用 ArrayList 作为真正的实现类

```groovy
//变量定义：List 变量由 [] 定义，比如
def aList = [5,'string',true] //List 由 [] 定义，其元素可以是任何对象
 
//变量存取：可以直接通过索引存取，而且不用担心索引越界。如果索引超过当前链表长度，List 会自动往该索引添加元素
assert aList[1] == 'string'
assert aList[5] == null // 第 6 个元素为空
aList[100] = 100  // 设置第 101 个元素的值为 10
assert aList[100] == 100
//aList 到现在为止有多少个元素呢？
println aList.size  ===> 结果是 101
```

* Map 类

```groovy
//Map 变量由 [:] 定义，比如
def aMap = ['key1':'value1','key2':true] 

//key 可以用''或 "" 包起来，也可以不用引号包起来。比如
def aNewMap = [key1:"value",key2:true] // 其中的 key1 和 key2 默认被处理成字符串 "key1" 和 "key2"

//aConfuseMap 中的 key1 到底是 "key1" 还是变量 key1 的值“wowo”？
def key1="wowo"
def aConfusedMap=[key1:"who am i?"]
//显然，答案是字符串 "key1"。如果要是 "wowo" 的话，则 aConfusedMap 的定义必须设置成：
def aConfusedMap=[(key1):"who am i?"]

//Map 中元素的存取更加方便，它支持多种方法：
println aMap.keyName    //<== 这种表达方法好像 key 就是 aMap 的一个成员变量一样
println aMap['keyName'] //<== 这种表达方法更传统一点
aMap.anotherkey = "i am map"  //<== 为 map 添加新元素

```

* Range 类

```groovy
def aRange = 1..5  //<==Range 类型的变量 由 begin 值 + 两个点 +end 值表示
//这个 aRange 包含 1,2,3,4,5 这 5 个值
 
//如果不想包含最后一个元素，则
def aRangeWithoutEnd = 1..<5 // <== 包含 1,2,3,4 这 4 个元素
println aRange.from
println aRange.to
```

### 闭包Closure

闭包，是一种数据类型，它代表了一段可执行的代码

```groovy
def xxx = {paramters -> code}  // 或者  
def xxx = {纯 code}//无参数
```

举例：

```groovy
def aClosure = {// 闭包是一段代码，所以需要用花括号括起来..  
    Stringparam1, int param2 ->  // 这个箭头很关键。箭头前面是参数定义，箭头后面是代码  
    println"this is code" // 这是代码，最后一句是返回值，  
   // 也可以使用 return，和 Groovy 中普通函数一样  
}  

```

**如果闭包没定义参数的话，则隐含有一个参数，这个参数名字叫 it。it 代表你实际传入的参数。**

```groovy
def greeting = { "Hello, $it!" }
assert greeting('Patrick') == 'Hello, Patrick!' //true

//等同于
def greeting = { it -> "Hello, $it!" }
assert greeting('Patrick') == 'Hello, Patrick!'

```

采用下面这种写法，则表示闭包没有参数：

```groovy
def noParamClosure = { -> true } //这个时候，我们就不能给 noParamClosure 传参数了！
noParamClosure ("test") //<== 报错喔！
```

**尽管java中没有闭包这种特性，但是毕竟groovy也是编译成字节码的，因此闭包编译后必然是一个类，由反编译可知闭包会编译成Closure的子类。然后我们作为使用者，从语法的角度看，闭包更像 C/C++ 语言的函数指针**。

闭包定义好后，要调用它的方法就是：

```groovy
aClosure.call("this is string",100)
//或者  
aClosure("this is string", 100)  
```
解惑：
```groovy
//1.
{println "xxxxx"}//没有赋值，把它当做普通的代码块，不会生成闭包

//2.
//下面两行代码，为什么println "xxxxx" 会执行？并且为什么会产生两个表示闭包的内部类？
{println "xxxxx"}
{}
//--->不要被换行迷惑，合并为一行：
{println "xxxxx"}{}
//---》然后再把括号补全：
{println "xxxxx"}({})
//不就相当于--》
temp = {println "xxxxx"}
temp({})

//验证一下我的分析
def bb = {print "xxxxx ";return 7}
{}
println bb
//输出xxxxx 7，可见bb==7,而不是闭包，等价于-->
def bb = ({print "xxxxx ";return 7}({}))
println bb

//3.
//bb是一个闭包对象，但是bb出现在双引号里的$表达式里，虽然没有显式的调用，依然会执行此闭包里的语句
def bb = {print 'bb';return "abcd"}
println " xxx ${bb}  " + bb //tag1 输出 bb xxx   ConsoleScript17$_run_closure1@7cbf3748
println " xxx ${bb()}  " + bb//tag2 输出 bb xxx abcd  ConsoleScript17$_run_closure1@7cbf3748
println " xxx ${{print 'bb';return "abcd"}} "//tag3 输出 bb xxx abcd 
//所以tag1没有输出abcd，tag3会输出。为什么有这种区别呢？后文还会继续讨论相关话题，引出更多迷惑现象，最终结论是避免这样写。
```



## 脚本

使用groovyconsole命令会启动自带的编辑器，在编辑器代码区直接输入`println "Hello Groovy"`，然后按CTRL + R运行

或者新建文件test.groovy，代码只有一行`println "Hello Groovy"`，cmd执行: `groovy test.groovy`，输出Hello Groovy。

我们知道groovy是基于java的，java运行必须有一个main方法，代码必须放到类里面。单独一行println是不能执行的。

把test.groovy编译成class文件：`groovyc -d classes test.groovy`

然后用jd-gui打开，查看java代码：

```java
import groovy.lang.Binding;
import groovy.lang.Script;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.callsite.CallSite;

public class test extends Script {
    public test() {}
    public test(Binding context) { super(context); }
    
    public Object run() {
        CallSite[] arrayOfCallSite = $getCallSiteArray();
        return arrayOfCallSite[1].callCurrent(this, Integer.valueOf(3));
    }
  
    public static void main(String... args) {
        CallSite[] arrayOfCallSite = $getCallSiteArray();
        arrayOfCallSite[0].call(InvokerHelper.class, test.class, args);
    }
}

```

接着新建一个Hello.groovy，把类和main方法补充完整：

```groovy
public class Hello {
    public static void main(String[] args) {
        System.out.println("hello world");
    }
}
```

反编译后得到的是：

```java
//省略import
public class Hello  implements GroovyObject
{
  @Generated
  public Hello()
  {
    MetaClass localMetaClass = $getStaticMetaClass();
    this.metaClass = localMetaClass;
  }
  public static void main(String... args)
  {
    CallSite[] arrayOfCallSite = $getCallSiteArray();arrayOfCallSite[0].call(arrayOfCallSite[1].callGetProperty(System.class), "hello world");
  }
  //省略其它代码
}

```

请观察这两者的区别。直接给出结论： 如果 Groovy 文件里只有执行代码，没有类的定义，则 Groovy 编译器会生成一个 Script 的子类，类名和脚本文件的文件名一样，而脚本中的代码会被包含在一个名为`run`的方法中，同时还会生成一个main方法，作为整个脚本的入口。

这就是groovy作为脚本的真相。



可以利用“groovyc转class 然后反编译成java”这种方法，来分析一些问题。比如：

```groovy
def x = 1
def printx(){  
   println x  
} 
printx()
```

执行的时候报错：Caught: groovy.lang.MissingPropertyException: No such property: x for class: xxx

如果不理解机制，这个报错根本就是一头雾水。从脚本的角度看，def x和printx是平级的，都是在整个脚本中定义的，不可能报错。我们反编译看：

```java
public class Hello extends Script {
    public Object run() {
        CallSite[] arrayOfCallSite = $getCallSiteArray();
        Object x = Integer.valueOf(1);
        //xxxxxxxx省略
    }
//。。。。。省略
  //printx的定义
  public Object printx() {
    CallSite[] arrayOfCallSite = $getCallSiteArray();
    return arrayOfCallSite[2].callCurrent(this, arrayOfCallSite[3].callGroovyObjectGetProperty(this));
  }
}
```

你看，编译后def x = 1是在run方法里，而printx函数是类的成员，为什么报MissingPropertyException就很明了了。

解决这个问题非常简单，不要使用def关键字：

```groovy
x = 1
def printx(){  
   println x  
}  
printx()
```

## $getCallSiteArray

反编译的代码中有很多$getCallSiteArray，所有的方法调用都变成了CallSite的调用。我们好像没有看到$getCallSiteArray的定义，这是反编译工具的问题。似乎所有的反编译工具都不会把$开头的方法和字段显示出来，包括jdk里面自带的javap命令。我不知道是不是$符号有什么特别的含义。但是我发现XJad这个工具可以把$开头的那些属性和方法全部显示出来。另外也可以用字节码查看器/修改器看到这些隐藏起来的方法。

讨论哪个反编译工具更好用没有意义，只是在这个场景下，以研究groovy为目的，我建议用XJad或者Bytecode-Viewer.jar(从https://github.com/Konloch/bytecode-viewer/releases下载最新版，java -jar启动GUI)。

举一个例子：

```java
//groovy中的代码如下：
	method1()//调用method1方法
    println this//调用println方法
        
//XJad的反编译代码
private static void $createCallSiteArray_1(String as[])
{
    as[0] = "method1";//数组0存放method1
    as[1] = "println";//数组1存放 println
}
private static CallSite[] $getCallSiteArray()
{
    CallSiteArray callsitearray;
    if ($callSiteArray == null || (callsitearray = (CallSiteArray)$callSiteArray.get()) == null)
    {
        callsitearray = $createCallSiteArray();//调用$createCallSiteArray，里面调用$createCallSiteArray_1
        $callSiteArray = new SoftReference(callsitearray);
    }
    return callsitearray.array;
}
public Object doCall(Object it)
{
    CallSite acallsite1[] = $getCallSiteArray();
    acallsite1[0].callCurrent(this);//相当于调用 method1()
    return acallsite1[1].callCurrent(this, getThisObject());//相当于println this，注意getThisObject()才是groovy中传的参数this
}
```

groovy为什么要把调用委托到CallSite呢？以我的经验，八九不离十是为了实现某些动态特性。

## 闭包委托

重点知识。

### this

```groovy
println this
//放到groovyconsole里执行，输出： ConsoleScript4@20d9ed49
//拷贝到A.groovy里面，输出是： A@5170bcf4
//结合前一节“脚本”的知识，可见this就是当前类的对象，跟java一样

//但是如果把this放到闭包里呢？
def bb = {
    println this
}
bb()//输出依然是： A@5170bcf4。可见虽然从实现机制看，闭包是一个类，但是设计者让它更像一个函数指针。
//反编译看:acallsite1[1].callCurrent(this, getThisObject())而getThisObject()是外面的类

//更复杂一点
class A{
    def method1(){
        //1.
        //输出：method1 A@1431c10   A$_method1_closure1@655e3166
        //第一个this表示A对象没问题。第二个{this}，这个this仍然是A对象，但是{this}整体是定义了一个闭包
        //_method1_closure1表示的是{this}这个闭包对象，跟里面的this没关系，删掉也是一样的输出
        println "method1 " +  this.toString() + "   " + {this}
        
        //2.
        //输出 method1 A@5361ecad A@5361ecad A@5361ecad A$_method1_closure3@5289d982}，并且编译会产生_method1_closure2、_method1_closure2两个内部类
        //双引号里的${}表示对里面的表达式求值，里面再套一个{this}则定义闭包，所以${ {this}.toString() }对应输出是A$_method1_closure3可以理解。
        //但是为什么${{this}}对应输出仍然是A呢？
        println "method1 $this ${this} ${{this}} ${ {this}.toString() }" 
        
        //method1 null 7 null ConsoleScript22$_run_closure3@5b71215a，这个例子表明，$表达式里只有一个闭包对象时，闭包会执行（参数传空），返回值作为${}表达式的值。
        //如果把闭包看作对象，那么这种行为有点反直觉;但是看作函数指针的话，也勉强能理解。平时避免这样写就是了
        println "method1 ${} ${{7}} ${{}} ${{}.toString()}"
        
        //接上，基于groovy3.0.7, 第一个输出xxx   (回车) yyy （回车）method1 
        //第二行输出xxx   (回车) yyy （回车）zzzz （回车） method1 
        //第三行在zzzz后面把ttt也打出来了。777始终不会输出。总之这里面比较让人困惑，避免这样写。
        println "method1 ${println 'xxx';return {println 'yyy';return {println 'zzzz';return {println 'ttt';return 777}}}}"
        println "method1 ${println 'xxx';return {println 'yyy';return {println 'zzzz';return {println 'ttt';return 777}}()}}"
        println "method1 ${println 'xxx';return {println 'yyy';return {println 'zzzz';return {println 'ttt';return 777}()}()}}"
        
        //3.
        def cc = {
            println  "out " + this
            def inn = {
                 println "inner " + this 
            }
            inn()
        }
        cc()
    }
}
new A().method1()
//3的输出：
//out A@1adca6a3
//inner A@1adca6a3
//可见闭包不管怎样嵌套，this始终指的A对象


```

### thisObject、owner、delegate

这三个对象是闭包中才有的。

```groovy
//测试thisObject
class A{
    def method1(){         
        def cc = {
            println  "out " + thisObject
            def inn = {
                 println "inner " + thisObject 
                 println this==thisObject//true
            }
            inn()
        }
        cc()
    }
}
new A().method1()
//输出:A@64e0b68c、A@64e0b68c、true
//可见thisObject等于this,指向A对象。只是this可以在任何地方，thisObject是闭包的属性，只能出现在闭包里
```

```groovy
//测试owner
class A{
    def method1(){         
        def cc = {
            println  "out " + owner
            println this==owner //true
            //嵌套
            def inn = {
                 println "inner " + owner 
                 println this==owner //false
                
                //再嵌套
                 def ininer = {
                     println "owner " + owner //指向inn
                     println "owner.owner " + owner.owner //指向cc
                }
                ininer()
            }
            inn()
        }
        cc()
    }
}
new A().method1()
//输出如下：
out A@1cf9fb4e
true
inner A$_method1_closure1@6f9ac2de
false
owner A$_method1_closure1$_closure2@37dce6d0
owner.owner A$_method1_closure1@6f9ac2de
//解释：
owner是指向定义此闭包的那个对象。如果闭包定义在方法里，因为方法不是对象，故owner指向Class对象。
当闭包是在其他的闭包中定义时，那么owner是指向外面一层闭包对象。
//注意：
println "owner " + owner 不能写成 println "owner $owner" 
因为此时owner指向外层闭包，"out ${owner} "会执行闭包内的语句，形成无限递归，最后stack溢出。
```

```groovy
//测试delegate
//delegate默认就是owner,但是可以修改为其它对象。
def bb = {
    println delegate
}
bb()
bb.delegate = "haha"
bb()
bb.setDelegate("ok")
bb()
//输出
ConsoleScript104@558b3c6c
haha
ok

//测试2  默认调用方法的优先顺序是thisObject > owner > delegate
def bb = {
    def cc = {
        method1();
        method2()
    }
    cc.delegate = new AA()
    cc()//owner 和 delegate 都有method1方法，优先调用owner的.method2只有delegate有，故调用delegate的。
    //设置委托模式优先
    cc.setResolveStrategy(Closure.DELEGATE_FIRST)
    cc()//method1和method2都是调用的delegate里的
}
def method1(){println "method1 thisObject"}
class AA{
    def method1(){println " method1 delegate"}
    def method2(){println "method2 delegate"}
}
bb()
//输出如下：
method1 thisObject
method2 delegate
 method1 delegate
method2 delegate

```

现在可以思考一个问题：

安卓项目，build.gradle里面有一行:

```
defaultConfig {
    applicationId "com.hsae.ccs20.onlinevideo"
    minSdkVersion 19
    targetSdkVersion 21
}
```

如果你是新手，照本宣科，可能觉得没问题，但是现在你已经知道build.gradle里面都是groovy语句，这个闭包的thisObject、owner是build.gradle这个构建脚本的上下文，当前脚本里面你也找不到minSdkVersion这些属性的定义。那么语句为什么能执行呢？

原来，在gradle中，我们一般会指定delegate为当前的it，这样在闭包内就可以对该it进行配置。

```
defaultConfig {
    println "delegate: " + delegate
    println delegate==it
}
//然后sync，控制台输出:
delegate: DefaultConfig_Decorated{name=main, dimension=null, minSdkVersion=null, targetSdkVersion=null, renderscriptTargetApi=null, renderscriptSupportModeEnabled=null, renderscriptSupportModeBlasEnabled=null, renderscriptNdkModeEnabled=null, versionCode=null, versionName=null, applicationId=null, testApplicationId=null, testInstrumentationRunner=null, testInstrumentationRunnerArguments={}, testHandleProfiling=null, testFunctionalTest=null, signingConfig=null, resConfig=null, mBuildConfigFields={}, mResValues={}, mProguardFiles=[], mConsumerProguardFiles=[], mManifestPlaceholders={}, mWearAppUnbundled=null}
true
```

这个知识十分重要。