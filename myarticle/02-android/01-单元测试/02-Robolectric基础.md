# Robolectric基础

本文基于Android Studio和robolectric 4.4版本

## 搭建Robolectric环境

Android Gradle Plugin 版本大于3.2.1。

build.gradle：

android {

 testOptions {

  unitTests {

   includeAndroidResources = true

  }

 }

}

 

dependencies {

 testImplementation 'org.robolectric:robolectric:4.4'

}

如果你的环境引用的Android API Version是28或以下，robolectric版本改成4.3.1。

如果 Android Studio版本小于3.3，还要添加：

android.enableUnitTestBinaryResources=true

到gradle.properties。

 

## 运行

通过注解声明用Robolectric 运行test case：
````java
@RunWith(RobolectricTestRunner.class)
public class SandwichTest { } 
````

## Shadow机制

举例说明：

1、创建两个class：Foo.java和ShadowFoo.java，shadow类的名字我们约定总是原来的ClassName前面加一个Shadow(并不是必须的)。

````java
public class Foo {
   public Foo() {
     System.*out*.println("foo");
   }

   public int getIndex(){
     System.*out*.println("get index");
     return 2;
   }
 }
//ShadowFoo.java
@Implements(Foo.class)
public class ShadowFoo {
    public ShadowFoo() {

        System.out.println("ShadowFoo");
    }

    @Implementation
    public int getIndex(){
        System.out.println("get shadow index");
        return 5;
    }
}
````

通过在类名上面加注解@Implements(Foo.class)声明ShadowFoo是Foo的影子类，然后在ShadowFoo里面重新实现getIndex方法，在方法名上添加注解@Implementation。

 

2、在android studio的test目录下新建文件夹resources。resources目录下面按照包名新建目录，然后新建文件robolectric.properties，robolectric会自动扫描从根目录到包路径的每个目录。也就是说robolectric.properties文件可以放在resources目录、resources\com目录、resources\com\hsae目录等。

将：

`shadows=com.hsae.core.diagnose.ShadowFoo` 添加到robolectric.properties文件里面。

另一种等价的方法是在注解里指定Shadow：

@Config(sdk = 28,shadows = {ShadowFoo.class})

位置在@RunWith(RobolectricTestRunner.class)后面:

````java
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28,shadows = {ShadowFoo.class})
public class FooTest {xxxxx}
````

如果这个shadow被许多TestClass用到，最好还是放到resources里，只要写一次。

 

3、首先声明，Foo与ShadowFoo之间没有任何继承关系，不存在多态行为。

测试case里执行：
`Foo foo = new Foo();`
将会看到输出：

>  ShadowFoo
>  foo

说明在new Foo时，先创建了ShadowFoo，然后在ShadowFoo里面创建了Foo。

 

执行foo.getIndex()，输出get shadow index。表明虽然调的是foo的方法，其实真正执行的是影子的。

执行foo.getClass()，输出class com.hsae.core.diagnose.Foo。似乎表明对象确实是Foo类型，但是为什么getIndex执行的却是shadowFoo的方法？

可以理解为，因为我们没有用@Implementation修改Object#getClass的行为，所以调的是真实的foo.getClass方法，那么当然会打印`class com.hsae.core.diagnose.Foo`。而getIndex方法声明了 @Implementation注解，调用的是shadowFoo的方法。

如果知识局限于java，这里可能会感到迷惑，因为似乎对象明明是foo，执行的却是shadowFoo的方法，那么对象到底是什么类型呢? 这对我们分析bug造成了冲击，因为我们平时在解bug时，为了确定对象的真实类型，会加一个断点，当执行到断点处，我们就能看到对象的实际类型，但是这里肯定看到的是Foo，因为debug工具也是调用的getClass方法(或者等价方法)。或者 只有日志可供分析时，为了确定代码走的是哪一个方法，我们在日志里将foo的真实类型打印出来，从而推断代码走的是Foo的还是Foo的子类的。以往的经验告诉我们，如果类型打印是Foo,那么foo.getIndex一定调用的Foo类里的那个实现。

但是用了robolectric后，常识和经验在这里好像失效了，于是我们变得不那么自信，也不敢相信自己分析bug的结论，不敢把分析结论给客户看了。其实很简单，一切魔法都是因为框架动态修改了jvm字节码，也就是说，加载到内存中的Foo类的字节码，跟文件里写的已经不一样了。大家可以试着自己用asm、cglib、aspectJ、javassist等框架实现这个功能，参考https://www.jianshu.com/p/d760e48ea7b0。如果你的项目里没有使用这些框架，那么那些经验还是很可靠的。

 

4、Foo类中原来没有，ShadowFoo中添加的方法，Foo foo对象是没法调用的。执行ShadowFoo shadowFoo = Shadow.extract(foo)，获取影子的实例，然后就可以调用ShadowFoo类添加的方法了。robolectric的jar包里，用这种方法给android原生类添加了许多扩展功能。

  例如，android里的Activity类，可以通过：

> ShadowActivity shadowActivity = Shadows.shadowOf(activity)

获取相应的Shadow类，然后才能调用shadowActivity.getNextStartedActivity()。显然原生的Activity里是，没有getNextStartedActivity 这个方法的，robolectric在ShadowActivity里添加了这个方法,辅助我们做单元测试。

Shadows.shadowOf里面只有一行代码就是调用Shadow.extract。对于我们自定义的Shadow，只能用Shadow.extract获取。

> xue: 大部分介绍Robolectric的文章，开篇就是演示一下ShadowActivity 的使用方法，自定义shadow放在最后。我觉得这是本末倒置了。ShadowActivity 也好，ShadowLooper也好，shadow机制是它们的基础。

 

5、在ShadowFoo里用@RealObject标志`Foo mFoo;`,像下面这样：

@RealObject

Foo mFoo;

可以在ShadowFoo里访问原始对象，Robolectric自动将原始对象赋给mFoo，但是需要注意，mFoo.method()依旧会调用Shadow类的方法。而不是原始类的方法,仅仅能用来访问原始类的field。

 

6、前面说过，new Foo()时会先调ShadowFoo的构造函数，在调Foo的构造函数。如果Foo的构造函数在junit的环境下执行会抛出异常呢？在ShadowFoo里添加：

```
@Implementation
public void __constructor__() {//前后各有两个下划线
    System.out.println("__constructor__");
}
```

 然后在new Foo()时，会用`__constructor__`替代Foo的构造函数。

试着给Foo再加一个构造器：public Foo(String name)，相应的，`__constructor__`也要加上String参数，然后new Foo(“Jim”),执行的就是`__constructor__(“Jim”)`了，由于没有执行真实的构造函数，你需要在`__constructor__`里给mFoo的字段赋值。




7、有了Shadow机制，利用robolectric提供的大量shadow，和自定义的shadow，不仅可以在单元测试中逼真地模拟android环境，避免大量的mock，还可以解决许多其他问题，如native code、跨进程调用、添加测试接口等。

由于so是运行与android环境下的，robolectric理所当然不支持加载so，也就不能调用native方法。但是如果foo存在shadow，那么调用foo的native方法 不会报错，System.loadLibrary也不会有异常。相当于又为我们搞定了android测试的一个痛点。你还可以在shadow里面用java重写这个native方法。



8、static、private、final等robolectric都能支持shadow，极为强大。



9、Shadow类需要模仿类的继承结构，比如，如果你为ViewGroup实现了ShadowViewGroup，然后你的shadow类需要继承ViewGroup的超类的shadow ， ShadowView。



10、可以影子android sdk中的和自己代码里写的类。Jdk中的类无法被影子，了解一下classloader的委托机制就能理解了。通常jdk的类是不应该shadow的，原本是想shadow Thread类，把异步变成同步，方便单元测试，可惜现在看来是做不到的。



11、Robolectric.setupActivity、Robolectric.buildService、setupService等方法，用于支持测试activity、service。




12、更多参阅robolectric官网教程http://robolectric.org/