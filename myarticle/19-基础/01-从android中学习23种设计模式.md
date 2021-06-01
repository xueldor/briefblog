https://juejin.cn/post/6956555653229838350

# 从android中学习23种设计模式

#### 前言

我曾经也有个决心，想把23种设计模式全部记住，但总是背了又忘，忘了又继续背，总是记不住。虽然网上有一大堆教学，分析，自己写一遍，比看十遍深刻多了。顺便学几个单词。话说回来，我们无时无刻都在根据设计模式写代码。

#### 设计模式的七大原则

尽量用最简单的话描述

##### 1、开闭原则（Open Closed Principle）

对类的扩展是开放，对修改关闭。
在程序需要扩展的时候，对于一个类，不要去修改原来的代码，而是通过继承的方式去扩展这个类。
目的：降低维护风险

##### 2、单一职责原则（Single Responsiblity Principle）

每个类应该且只有一个职责。
目的：提高可读性

##### 3、里式替换原则（Liskov Substitution Principle）

子类继承父类时，可以实现父类的抽象方法，不要 重写 父类的方法，子类增加自己特有的方法。
目的：防止继承带来的问题

##### 4、依赖倒转原则（Dependency Inversion Principle）

程序要依赖于抽象接口，不要依赖，于具体实现，针对接口编程。
目的：利于代码升级

##### 5、接口隔离原则（Interface Segregation Principle）

庞大的接口拆分成更小的和更具体的接口，一个接口只用于一个业务逻辑。
目的：使功能解耦，高内聚、低耦合

##### 6、迪米特原则（Principle of Least Knowledge）

一个对象应当对 其他 对象尽可能少的了解。 目的：自己做自己的事情

##### 7、合成复用原则（Composite Reuse Principle）

使用对象组合，而不是继承来达到复用的目的。
继承破坏了封装性，父类的任何改变，都可能导致子类出问题。
优先考虑 合成复用，A类和B类的合成使用，而不是B继承A的使用。
目的：少用继承 降低耦合

#### 23种设计模式

23种设计模式分为三类
创建型模式、结构型模式、行为型模式
顾名思义，创建型 就是怎么创建对象的。结构型就是对象与对象的关系，变成更大的结构。行为型 就是运行时复杂流程的 控制。

#### 创建型

##### 1、单例模式（Singleton Pattern）

目的：主要就是一个类，频繁的创建，销毁
优点：内存中只有一个实例，减少开销
缺点：没有接口，不能继承，违背了单一职责原则
实现：
1.懒汉式 静态方法，用到的时候再创建对象
2.饿汉式 静态变量，直接new出对象
3.双重锁 两个if判断，第一个if是为了防止不必要的线程同步，第二个if判断是为了避免 第二个线程 重复创建对象，`volatile` 为了防止指令重排
4.kotlin关键字object也可以创建单例，原理和懒汉式类似在static块中创建
5.枚举的方式，枚举编译后，也是在static块中new出对象

```java
//双重锁
public class Singleton{
    private volatile static Singleton instance;
    private Singleton(){};
    public static Singleton getInstance(){
        if(instance==null){
            sychronized(Singleton.class){
                if(instance==null)
                    instance=new Singleton();
            }
        }
        return instatnce;
    }
}

```

在android中有个隐藏抽象类

```java
public abstract class Singleton<T> {

    public Singleton() {
    }

    private T mInstance;

    protected abstract T create();

    public final T get() {
        synchronized (this) {
            if (mInstance == null) {
                mInstance = create();
            }
            return mInstance;
        }
    }
}

private static final Singleton<IActivityTaskManager> IActivityTaskManagerSingleton =
        new Singleton<IActivityTaskManager>() {
            @Override
            protected IActivityTaskManager create() {
                final IBinder b = ServiceManager.getService(Context.ACTIVITY_TASK_SERVICE);
                return IActivityTaskManager.Stub.asInterface(b);
            }
        };

```

这里用来获取了AMS对象，如果获取不到，就通过ServiceManager去获取。
这个Singleton是隐藏类，直接用不了，可以直接copy出来用。

##### 2、工厂模式（Factory Pattern）

目的：解决接口选择问题
优点：想要创建对象，只要知道名字就行，屏蔽了内部实现，只要关心接口
缺点：增加一个产品的时候，需要增加一个实现类 和 一个工厂，比如Dagger2,新增一个注入的Bean，Dagger2就会为我们生成一个Factory类。空间增大了
实现：

```java
public interface Clothes {
    void getClothes();
}
public class Jacket implements Clothes {
    @Override
    public void getClothes() {
        System.out.println("夹克衫");
    }
}
public class Sweater implements Clothes {
    @Override
    public void getClothes() {
        System.out.println("毛衣");
    }
}
//工厂 假设他是衣柜
public class WardrobeFactory {

    public Clothes getShape(String shape) {
        switch (shape) {
            case "Jacket":
                return new Jacket();
            case "Sweater":
                return new Sweater();
        }
        return null;
    }
}

```

衣柜假设是个工厂，衣柜里有衣服，抽象出衣服，衣服可以有夹克衫或者是毛衣
在android中根据名字去拿对象,比如获取系统服务

```java
context.getSystemService(Service.ALARM_SERVICE)
String ALARM_SERVICE = "alarm"     

```

获取闹铃服务

##### 3、抽象工厂模式（Abstract Factory Pattern）

目的：解决接口选择问题
优点：当产品种类很多的时候 ，根据名字拿到的当前产品种类的对象
缺点：扩展非常困难，增加一个同类新的产品，就需要增加一个新的工厂
实现：

```java
//裤子
public interface Trousers {
    void getTrousers();
}
//牛仔裤
public class Jeans implements Trousers {

    @Override
    public void getTrousers() {
        System.out.println("牛仔裤");
    }
}
//短裤
public class Shorts implements Trousers {

    @Override
    public void getTrousers() {
        System.out.println("短裤");
    }
}

//衣服
public interface Clothes {
    void getClothes();
}
//毛衣
public class Sweater implements Clothes {
    @Override
    public void getClothes() {
        System.out.println("毛衣");
    }
}
//夹克衫
public class Jacket implements Clothes {
    @Override
    public void getClothes() {
        System.out.println("夹克衫");
    }
}
//衣服和裤子都有了，抽象工厂
//抽象工厂，这个工厂需要完成 那衣服 和 裤子
public abstract class AbsFactory {
    public abstract Trousers getTrousers(String trousers);
    public abstract Clothes getClothes(String clothes);
}
//生产衣服
public class ClothesFactory extends AbsFactory{
    @Override
    public Trousers getTrousers(String trousers) {
        return null;
    }

    @Override
    public Clothes getClothes(String clothes) {
        switch (clothes){
            case "Jacket":
                return new Jacket();
            case "Sweater":
                return new Sweater();
        }
        return null;
    }
}
//生产裤子
public class TrousersFactory extends AbsFactory {
    @Override
    public Trousers getTrousers(String trousers) {
        switch (trousers){
            case "Shorts":
                return new Shorts();
            case "Jeans":
                return new Jeans();
        }
        return null;
    }

    @Override
    public Clothes getClothes(String shape) {
        return null;
    }
}
//衣橱类
public class WardrobeFactory {

    public static AbsFactory getFactory(String choice){
        switch (choice){
            case "Clothes":
                return new ClothesFactory();
            case "Trousers":
                return new TrousersFactory();
        }
        return null;
    }
}
//使用
//拿裤子
AbsFactory absFactory = WardrobeFactory.getFactory("Trousers");
//具体拿什么裤子
Trousers shorts = absFactory.getTrousers("Shorts");
//拿短裤
shorts.getTrousers();

```

代码比较多，还是比较清晰的，把工厂抽象了一下。
工厂模式关注 生产的产品
抽象工程模式关注 产品之间的关系
在android中，抽象工厂比较少，可以把整个Service看做一个大抽象工厂，通过名字去拿服务，拿到服务对象，再调用对象里的抽象方法。

##### 4、原型模式（Prototype Pattern）

目的：快速，高效的创建对象
优点：提高性能，没有构造函数的限制
缺点：配合克隆方法进行，需要考虑深拷贝和浅拷贝的问题
实现：

```java
public class Sheep implements Cloneable {

    String name;

    public Sheep(String name){
        this.name = name;
    }

    public Object clone(){
        Object obj = null;
        try {
            obj = super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return obj;
    }
}

```

完成Cloneable方法，增加一个clone方法，Object也有一个克隆方法，这是调用了Native方法，底层去克隆了一个对象。
在android中

```java
public class Intent implements Parcelable, Cloneable {
    @Override
    public Object clone() {
      return new Intent(this);
    }
}
//okhttp中 也用到了
@Override public RealCall clone() {
  return RealCall.newRealCall(client, originalRequest, forWebSocket);
}

```

说到底也就是复制一个新对象。

##### 5、建造者模式（Builder Pattern）

目的：配置一个复杂对象
优点：独立，容易扩展
缺点：如果对象复杂，会有很多建造者类
实现：

```java
public class Dialog {
    String title;
    boolean mCancelable  = false;

    Dialog(String title,boolean mCanclable){
        this.title = title;
        this.mCancelable = mCanclable;
    }

    public void show() {
        System.out.print("show");
    }

    static class Builder{
        String title;
        boolean mCancelable  = false;

        public Builder setCancelable(boolean flag) {
            mCancelable = flag;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Dialog build(){
            return new Dialog(this.title,this.mCancelable);
        }
    }
}

```

这个建造者不拘泥于形式，建造者模式用于创建一个复杂对象。 在android中，Dialog就用到了建造者模式，第三方库的okhttp、Retrofit

#### 结构型

##### 6、代理模式（Proxy Pattern）

目的：增强他的职责，比如访问远程的服务器，如果直接访问可能会带来一些麻烦，通过一个代理去帮我们访问
优点:职责清晰，易扩展
缺点:增加了代理可能会导致速度慢，代理类会比较复杂
实现:

```java
//访问数据库接口
public interface DataBase {
    void select();
}
//正真 做查询操作
public class RealImage implements DataBase {

    @Override
    public void select() {
        System.out.println("查询");
    }
}
//代理类
public class ProxyDB implements DataBase{

    RealImage realImage;

    public ProxyDB(){
        if(realImage==null){
            realImage = new RealImage();
        }
    }

    @Override
    public void select() {
        realImage.select();
    }
}
//使用
DataBase proxyDb = new ProxyDB();
proxyDb.select();

```

这里用了ProxyDB 这个代理类，去访问数据，并没有直接去访问数据库。 在android中，当我们编写好AIDL文件后，编译器会自动给我们增加一些代码

```java
public interface IRemoteService extends android.os.IInterface{
  public static abstract class Stub extends android.os.Binder implements ...{
    public static com.learnaidl.IRemoteService asInterface(..) {

        return new ..Proxy(obj);

      }

      private static class Proxy implements com.learnaidl.IRemoteService{
          ...
      }

  }  
}
//使用
IRemoteService.Stub()   

```

通过IRemoteService的Stub类拿对象，这个对象是通过一个代理类创建出来的。
换个角度看，代理模式也就是 实现了一个接口，并完成了一个方法，这个方法可以去做任何事情。

##### 7、外观模式（Facade Pattern）

目的：降低系统复杂度
优点：提高灵活性，安全性
缺点：违反了开闭原则、迪米特原则，改东西相对麻烦
实现:

```java
//接口  有个打开的动作
public interface Action {
    void open();
}
//灯 实现了  开灯
public class Lamp implements Action {
    @Override
    public void open() {
        System.out.println("开灯");
    }
}
//电视 实现了 打开电视
public class TV implements Action {

    @Override
    public void open() {
        System.out.println("开电视");
    }
}
//遥控器  可以控制灯 和电视
public class RemoteControl {

    Action lamp;
    Action tv;

    public RemoteControl(){
        lamp = new Lamp();
        tv = new TV();
    }

    public void openLamp(){
        lamp.open();
    }

    public void openTv(){
        tv.open();
    }
}

```

遥控器既可以开灯，又可以开电视。可见这个“遥控器”职责并不单一。也不用局限于接口，如果是功能聚合到一个类中，依然可以叫外观模式。想象成 前台、接待员
在android中，Context就用了外观模式，Context可以打开Activity，可打开Service，广播等等。

##### 8、装饰器模式（Decorator Pattern）

目的：解决扩展子类膨胀的问题,比如摊煎饼，可以摊煎饼前，煎个鸡蛋，摊煎饼后，撒点酱
优点：灵活扩展
缺点:过多的装饰，会很复杂
实现

```java
//一个煎饼接口
public interface Pancake {
    void pancake();
}
//牛肉煎饼
public class BeefPancake implements Pancake{

    @Override
    public void pancake() {
        System.out.println("牛肉煎饼");
    }
}
//工作人员
public class Worker {

    Pancake pancake;
    public Worker(){
        pancake = new BeefPancake();
    }

    public void makePancake(){
        System.out.println("煎鸡蛋");
        pancake.pancake();
        System.out.println("撒酱");
    }
}

//使用
Worker worker = new Worker();
worker.makePancake();

```

说白了，装饰模式，装饰东西，实际作用的前后装饰一些其他内容。
在android中，Context，ContextImpl也就是个装饰模式，我们肯定会在`startActivity()`前后做点业务操作。

##### 9、享元模式（Flyweight Pattern）

目的：为了解决大量创建相同对象，可能造成OOM
优点：减少重复创建对象，降低内存
缺点：提高了系统的复杂度，如果固定了一些对象，当被改变时候，会造成混乱
实现：

```java
//假设有个请求的类
public class Request {
}
//通过一个地方 去拿请求
public class HttpFactory {
    public static final HashMap<String,Request> requestMap = new HashMap<>();

    public Request getRequestList(String name) {
        Request request = (Request) requestMap.get(name);
        if(request==null){
            request = new Request();
            requestMap.put(name,request);
        }
        return request;
    }
}
//使用
HttpFactory factory = new HttpFactory();
Request request1 = factory.getRequestList("baidu");
//假设过了一会
Request request2 = factory.getRequestList("baidu");

```

相当于是缓存了一块地方，把对象放进去，需要对象的时候就从这里面取，如果相同需求，则会返回已有的对象。
在android中，获取Message，可以通过`Message.obtain()`去获取Message。在JVM中缓存了很多字符串。 可以说我们任何时候都在使用享元模式。

##### 10、组合模式（Composite Pattern）

目的：用来描述整体和部分的关系，比如在树中，一个结点可以是根节点，也可以是叶子节点
优点：调用简单，结点可以自由增加
缺点：在组合模式中，依赖的都是实现类，而不是接口，违反了依赖倒置原则
实现:

```java
//二叉树
public class ListNode {
    int data;
    ListNode left;
    ListNode right;
}

```

二叉树的结点，既可以是根节点，又可以是叶子节点。 在android中，View和ViewGroup的关系，ViewGroup既可以是一个View，又可以包含View。

##### 11、适配器模式（Adapter Pattern）

目的:解决两个不兼容接口的桥梁,兼容转换
优点：让两个没有关联的类一起运行，提高复用
缺点：使用过多，会让系统变的凌乱
实现：

```java
//定义适配器类
public class Adapter {
    public void getView(int i){
        System.out.println("给出View"+i);
    }
}
//ListView 继承了Adapter
public class ListView extends Adapter{

    public void show(){
        System.out.print("循环显示View");
        for(int i=0;i<3;i++){
            getView(i);
        }
    }
}
//GridView继承了Adapter
public class GridView extends Adapter{

    public void show(){
       ...
       getView(i);
    }
}

```

适配器模式可以用继承实现，这里没有更高的抽象，当然也可以把Adapter的内容抽象出去，仅仅演示，ListView、GridView适配了Adapter类。
在android中，ListView、RecyclerView都是用了适配器模式，ListView适配了Adapter，ListView只管ItemView，不管具体怎么展示，Adapter只管展示。就像读卡器，读卡器作为 内存和电脑 之间的 适配器。

##### 12、桥接模式（Bridge Pattern）

目的：将两个能够独立变化的类分开，不用继承，继承会造成类的爆炸增长
优点：抽象分离，易扩展，细节透明
缺点：会增加系统的设计难度
实现：

```java
//颜色接口
public interface Color {
    void draw(String box);
}
//红色 完成方法  红色的something
public class RedColor implements Color{
    @Override
    public void draw(String sth) {
        System.out.println("red "+ sth);
    }
}
//抽象类  盒子
public abstract class Box {
    Color color;
    public Box(Color color){
        this.color = color;
    }
    abstract void getBox();
}
//实现类 红色盒子
public class RedBox extends Box {

    public RedBox(Color color) {
        super(color);
    }

    @Override
    void getBox() {
        this.color.draw("box");
    }
}
//使用
RedColor redColor = new RedColor();
Box box = new RedBox(redColor);
box.getBox();

```

如果类存在两个维度的变化，比如颜色可能有红色、绿色，包可能有手提包，钱包。对于这两个维度的变化，适合用桥接。更直观的说，比如一个USB线左边可以插不同的手机，右边可以插不同的电源。
在android中,整个View的视图,View、Button、ViewGroup等等都是在View这个维度上的变化，都有onDraw()方法来实现不同的视图。另一个维度就是把View绘制到屏幕上。私以为，这RexBox相当于一个View，RedColor相当于绘制到屏幕上。

#### 行为型

##### 13、模板模式（Template Pattern）

目的：固定了一些方法，只要照着做就行,比如把大象放进冰箱，打开冰箱，放进冰箱，关闭冰箱
优点：行为由父类控制，便于维护
缺点：导致类增多，系统变大
实现:

```java
public abstract class BaseActivity {

    abstract void onCreate();
    abstract void onDestory();
}
public class HomeActivity extends BaseActivity {
    @Override
    void onCreate() {
    }

    @Override
    void onDestory() {
    }
}

```

它的主旨就是抽象出公共的方法，子类照着重写就行，行为由父类控制
这个。。。作为android开发者天天都在用。。。

##### 14、策略模式（Strategy Pattern）

目的：解决很多if else的情况
优点：可以避免多重判断条件，扩展性好
缺点：类会增多
实现:

```java
//假如RecyclerView 这样写
public class RecyclerView {

    private Layout layout;

    public void setLayout(Layout layout) {
        this.layout = layout;

        if(layout == "横着"){

        }else if(layout == "竖着"){

        }else if(layout=="格子"){

        }else{

        }  
        this.layout.doLayout();
    }
}
//这样写if就很多了
//排列的方式
public interface Layout {
    void doLayout();
}
//竖着排列
public class LinearLayout implements Layout{
    @Override
    public void doLayout() {
        System.out.println("LinearLayout");
    }
}
//网格排列
public class GridLayout implements Layout{
    @Override
    public void doLayout() {
        System.out.println("GridLayout");
    }
}
public class RecyclerView {
    private Layout layout;

    public void setLayout(Layout layout) {
        this.layout = layout;
        this.layout.doLayout();
    }
}


```

这里直接举了android中RecyclerView的例子，我们给RecyclerView选择布局方式的时候，就是选择一个策略。

##### 15、中介者模式（Mediator Pattern）

目的：解决对象与对象之间的耦合关系
优点：降低复杂度，各个类之间解耦
缺点：中介者会过于庞大不好维护
实现：

```java
//聊天室
public class ChatRoom {
    public static void showMessage(User user,String msg){
        System.out.println(user.name+":"+msg);
    }
}
//用户
public class User {
    String name;

    public User(String name){
        this.name = name;
    }

    public void sendMessage(String msg){
        ChatRoom.showMessage(this,msg);
    }
}
//使用
User h1 = new User("h1");
User h2 = new User("h2");

h1.sendMessage("hello");
h2.sendMessage("you too~");

```

这个聊天室就相当于个中介者，给两个人传递消息。如果聊天室新增功能，会导致聊天室的代码越来越多，不好维护。
在android中，无时无刻都在使用中介者，MVP 的 P ，MVC的 C ，MVVM 的 VM，你和我。？？？

##### 16、观察者模式（Observer Pattern）

目的：一个对象改变通知其他对象，保证协作
优点：观察者和被观察者是抽象耦合的，也就是说通过抽象方法，给具体的类通知
缺点：如果观察者有很多，被观察者发消息，会慢，如果不小心观察者和被观察者有依赖，会循环引用
实现：

```java
//抽象类 做作业
public abstract class DoWork {

    protected Teacher teacher;
    abstract void doHomeWork(int i);
}
//老师
public class Teacher {

    private List<Student> students = new ArrayList<>();

    private int index;

    public void attach(Student student){
        this.students.add(student);
    }

    public void dispatchHomeWork(int index){
        this.index = index;
        notifyAllStudent();
    }

    public void notifyAllStudent(){
        for (Student stu:students) {
            stu.doHomeWork(index);
        }
    }
}
//学生类
public class Student extends DoWork {

    public Student(Teacher teacher){
        this.teacher = teacher;
        this.teacher.attach(this);
    }

    @Override
    void doHomeWork(int i) {
        System.out.println(this+":做作业"+i);
    }
}
//使用
Teacher teacher = new Teacher();

new Student(teacher);
new Student(teacher);
new Student(teacher);

teacher.dispatchHomeWork(1);
teacher.dispatchHomeWork(3);

```

这个很好实，关键代码就是通过一个ArrayList保存观察着。
其实在java中，jdk已经实现好的观察者模式。点开源码就会发现他保存的是个向量

```java
public class Observable {
    private boolean changed = false;
    private Vector<Observer> obs;
}

```

##### 17、状态模式（State Pattern）

目的：对象依赖一个状态，对象可以根据状态改变自己的行为
优点：状态转换与逻辑合在一起，而不是通过if语句隔开
缺点：会使结构比较复杂，也会增加类的个数
实现：

```java
//状态接口
public interface State {

    void doAction(MediaPlayer activity);
}
//开始状态
public class OnStart implements State{
    @Override
    public void doAction(MediaPlayer activity) {
        System.out.println(activity+"：OnStart");
    }
}
//结束状态
public class OnStop implements State{

    @Override
    public void doAction(MediaPlayer activity) {
        System.out.println(activity+"：OnStop");
    }
}
//播放器
public class MediaPlayer {

    State state;

    public void setState(State state){
        this.state = state;
    }

    public State getState() {
        return state;
    }
}
//使用
MediaPlayer activity = new MediaPlayer();

OnStart onStart = new OnStart();
onStart.doAction(activity);

OnStop onStop = new OnStop();
onStop.doAction(activity);

```

定义了两个状态，开始和结束，让播放器 依赖这两个状态进行操作。
在android中，就比如播放器，依赖自身的状态,进行播放暂停操作。再比如Fragment,Fragment走自己的onCreate等方法，也是依赖Activity的生命周期状态进行操作。

##### 18、责任链模式（Chain of Responsibility Pattern）

目的：主要用来解耦，客户只要把消息发到责任链上，无需关注请求细节和传递过程
优点：解耦，简化操作
缺点：性能会有一点影响，调试不太方便
实现:

```java
//责任链接口
public interface Interceptor {
    String chain(String inData);
}
//缓存
public class CacheInterceptor implements Interceptor {

    @Override
    public String chain(String inData) {
        return inData += "加了缓存";
    }
}
//呼叫服务器
public class CallServerInterceptor implements Interceptor{

    @Override
    public String chain(String inData) {
        return inData += "呼叫了服务器";
    }
}
//把责任 集合起来
public class RealInterceptor {

    List<Interceptor> list = new ArrayList<>();
    public RealInterceptor(){
        list.add(new CacheInterceptor());
        list.add(new CallServerInterceptor());
    }

    public String request(String st){
        String result = "";
        for (int i=0;i<list.size();i++){
            result += list.get(i).chain(st);
        }
        return result;
    }
}
//使用
RealInterceptor realInterceptor = new RealInterceptor();
String result = realInterceptor.request("请求->");
System.out.println(result);

```

责任链就是想链条一样，也可以在中间增加或减少，像"击鼓传花"，一个个传递。
在android中，事件分发机制，父View接到事件，传递给子View。在第三方库okhttp中一个网络请求用的也是拦截器模式。

##### 19、备忘录模式（Memento Pattern）

目的：不破坏封装的前提下，捕获一个对象的内部状态，并保存，之后能根据状态恢复到原来的样子
优点：提供了一种可以恢复的机制，封装了信息，用户不需要过多关心细节
缺点：消耗资源，如果保存内容过多过大，会占用很多资源
实现:

```java
//Layer表示 一层
public class Layer {
    private String state;

    public Layer(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
//管理  层
public class Manager {

    private Layer mLayer;

    public Layer save(String state){
        return new Layer(state);
    }

    public void restore(Layer layer){
        mLayer = layer;
    }

    public Layer getmLayer() {
        return mLayer;
    }
}
//正真用这个层
public class PhotoShop {

    private List<Layer> layerList = new ArrayList<>();

    public void ctrl_S(Layer mLayer){
        layerList.add(mLayer);
    }

    public Layer ctrl_Z(int index){
        return layerList.get(index);
    }

}
//使用
PhotoShop ps = new PhotoShop();
Manager manager = new Manager();
ps.ctrl_S(manager.save("第一层"));
ps.ctrl_S(manager.save("第二层"));
manager.restore(ps.ctrl_Z(1));
System.out.println("当前是："+manager.getmLayer().getState() );
ps.ctrl_S(manager.save("第三层"));
ps.ctrl_S(manager.save("第四层"));
manager.restore(ps.ctrl_Z(3));
System.out.println("当前是："+ manager.getmLayer().getState());

```

就像Ps一样,Ctrl+S 保存一层，Ctrl+Z回退一层。就像玩游戏的存档一样
在android中，Activity的`onSaveInstanceState`保存数据在onCreate里 恢复数据

##### 20、迭代器模式（Iterator Pattern）

目的：遍历一个对象
优点：访问一个聚合数据，聚合数据不会暴露内部内容
缺点：会增加类的个数，增加系统复杂性
实现：

```java
//迭代接口
public interface Iterator {
    boolean hasNext();
    Object next();
}
//容器接口
public interface Container{
    Iterator getIterator();
}
//容器
public class NameContainer implements Container {

    public String[] names = {"name1" , "name2" ,"name3" , "name4"};

    @Override
    public Iterator getIterator() {
        return new NameIterator();
    }

    class NameIterator implements Iterator{

        int index;

        @Override
        public boolean hasNext() {
            if(index < names.length){
                return true;
            }
            return false;
        }

        @Override
        public Object next() {
            if(this.hasNext()){
                return names[index++];
            }
            return null;
        }
    }
}
//使用
NameContainer nameContainer = new NameContainer();
Iterator it = nameContainer.getIterator();
while(it.hasNext()){
    String name = (String) it.next();
    System.out.println(name);
}

```

Iterator接口，必须实现下一个 对象 和 是否有下一个对象。Container接口 需要返回一个实现Iterator接口的类。
在java中HashMap的内部类KeySet有Iterator，android中访问数据库有`Cursor`,都是是用了迭代器模式

##### 21、解释器模式（Interpreter Pattern）

目的：对于一些固定问法结构如xml，构建一个类解释它
优点：扩展性好、灵活、增加了新的表达方式
缺点：使用场景少，难维护,通常要用到递归
实现：

```java
//先看 使用
SelectInterpreter selectInterpreter = new SelectInterpreter();
selectInterpreter.interpreter("查t_name");
//解释器接口
public interface Interpreter {
    String interpreter(String sql);
}
//查询的 解释器
public class SelectInterpreter implements Interpreter {

    @Override
    public String interpreter(String sql) {
        // select * from t_user
        // select t_user
        if(sql.indexOf("查")==0){
            int start = sql.indexOf("查")+1;
            int end = sql.length();
            String tableName = sql.substring(start,end);
            System.out.println("查询表："+tableName);
        }else{
            System.out.println("sql error");
        }
        return null;
    }
}

```

sql是一种描述语言，通常sql查询一个表需要`select * from t_user`,这是数据库定义的语法。
现在由我来 解释，想要查询我的数据库，直接`查t_name`，就可以了。虽然有点扯，但应该最好理解了。
在android中，通常会定义xml布局，然后`setContentView(xml)`，把xml放进去，这个里面就用了解释器模式，通过`XmlResourceParser`等一些方法，把xml解释成对象。

##### 22、命令模式（Command Pattern）

目的：也是用来解耦的。通常请求者和实现者是一种耦合关系，但一些场合对行为记录、撤销、重做，这种请求和处理就不太适合在一起
优点：解耦，易扩展
缺点：命令类可能会变的很多
实现：

```java
//命令接口
public interface Order {
    void execute();
}
//买的命令
public class BuyStock implements Order{

    Stock stock ;

    public BuyStock(Stock stock){
        this.stock = stock;
    }

    @Override
    public void execute() {
        this.stock.buy();
    }
}
//卖的命令
public class SellStock implements Order {

    Stock stock ;

    public SellStock(Stock stock){
        this.stock = stock;
    }
    @Override
    public void execute() {
        this.stock.sell();
    }
}
//业务类  股票
public class Stock {

    public void buy(){
        System.out.println("buy");
    }

    public void sell(){
        System.out.println("sell");
    }
}
//经理人
public class Manager {

    private List<Order> list = new ArrayList<>();

    public void takeOrder(Order order){
        this.list.add(order);
    }

    public void placeOrder(){
        for (Order order:list) {
            order.execute();
        }
        list.clear();
    }
}
//使用

//经理人
Manager manager = new Manager();

//一个股票
Stock stock = new Stock();

//命令1  买股票
Order order1 = new BuyStock(stock);

//命令2  卖股票
Order order2 = new SellStock(stock);

manager.takeOrder(order1);
manager.placeOrder();

manager.takeOrder(order2);
manager.placeOrder();

```

4个类，一个买股票的类，一个卖股票的类，一个经理人，一个股票。买和卖相当于一条命令，也可以增加其他的命令。一句话，定义一条买股票的命令，把命令交给经理人，经理人去执行。 在android中，PackageManagerService用到了命令模式，实现对apk的解析、管理等操作。
私以为这个命令模式比较宽泛，将一个请求封装成一个对象，对这个请求有记录或者撤销等操作。比如线程池使用、okhttp一个请求用的Call，都可以视作命令。

##### 23、访问者模式（Visitor Pattern）

目的：解决稳定的数据结构和易变的操作耦合问题
优点：符合单一职责，易扩展
缺点：违反了迪米特原则、依赖倒置原则，依赖了具体实现类，不是依赖抽象
实现：

```java
//行为模式中最复杂的一个模式，先看代码
//抽象类 动作
public abstract class Action {
    abstract void accept(Visitor visitor);
}
//相机系统
public class CameraSystem extends Action {
    @Override
    void accept(Visitor visitor) {
        visitor.visitor(this);
    }
}
//图片系统
public class ImageSystem extends Action {

    @Override
    void accept(Visitor visitor) {
        visitor.visitor(this);
    }

    public int getSize(){
        return 10;
    }
}
//访问者 接口
public interface Visitor {
    void visitor(ImageSystem imageSystem);
    void visitor(CameraSystem cameraSystem);
}
//app1  实现 访问者接口
public class App1 implements Visitor {

    @Override
    public void visitor(ImageSystem imageSystem) {
        System.out.print("访问图片");
        System.out.print("一共"+ imageSystem.getSize());
    }

    @Override
    public void visitor(CameraSystem cameraSystem) {
        System.out.print("访问相机");
    }
}
//使用
App1 app1 = new App1();
ImageSystem imageSystem = new ImageSystem();
imageSystem.accept(app1);

```

看上去代码不是非常复杂，我写了一个比较好理解的代码，访问者肯定是访问内容
一个动作的抽象类，图片系统和相机系统完成了这个动作。App1 实现了访问者接口，表示App1可以 访问 图片系统和相机系统。
再换个例子，比如`棉花`和`纸`,在做衣服的工厂可以做成`毛衣`和`标签牌子`，在造钱的工厂,可以做成`纸币`和`包装袋`
访问者的目的是：稳定的数据结构 和 异变的操作 ，耦合问题，我觉着应该不用在解释了。

#### 总结

23种设计模式,分为创建型、结构型、行为型，都是围绕七大设计原则，当然有些设计模式牺牲了一些原则，换取更好的效果。
抛开设计模式来看，所有的代码，无非用到了，抽象类、接口、实现类，再加上接口和实现类的组合、实现类与实现类的组合而写成的代码。
设计模式是代码设计经验的总结,我们根据这些总结，开发出易于他人理解、可靠可重用的代码。

其它参考:[设计模式-菜鸟教程](https://www.runoob.com/design-pattern/design-pattern-tutorial.html)

