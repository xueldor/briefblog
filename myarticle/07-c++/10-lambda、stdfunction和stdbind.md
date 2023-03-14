lambda 来源于函数式编程的概念，也是现代编程语言的一个特点，，C# 3.5 和 Java 8 中就引入了 lambda 表达式。

从 C++11 开始，C++ 有三种方式可以创建/传递一个可以被调用的对象：

- 函数指针
- 仿函数（Functor）
- Lambda 表达式

### 函数指针

函数指针是从 C 语言老祖宗继承下来的东西，比较原始，功能也比较弱：

1. 无法直接捕获当前的一些状态，所有外部状态只能通过参数传递（不考虑在函数内部使用 static 变量）。

2. 使用函数指针的调用无法 inline（编译期无法确定这个指针会被赋上什么值）。

```c++
// 一个指向有两个整型参数，返回值为整型参数的函数指针类型
int (*)(int, int);

// 通常我们用 typedef 来定义函数指针类型的别名方便使用
typedef int (*Plus)(int, int);

// 从 C++11 开始，更推荐使用 using 来定义别名
using Plus = int (*)(int, int);
```

### 仿函数

```C++
class Plus {
 public:
  int operator()(int a, int b) {
    return a + b;
  }   
};

Plus plus; 
std::cout << plus(11, 22) << std::endl;   // 输出 33
```

`plus(11, 22)`看起来像是调用一个名为`plus`的函数。但其实plus是一个类的对象。只不过实现了运算符重载`operator()`。

这种对象的使用看上去像一个函数，就称为“伪函数”。

### Lambda 表达式

Lambda 表达式在表达能力上和仿函数是等价的。编译器一般也是通过自动生成类似仿函数的代码来实现 Lambda 表达式的。上面的例子，用 Lambda 改写如下：

```c++
auto Plus = [](int a, int b) { return a + b; };
```

#### 使用场景

```c++
void doActionA() {
    int list1[10] = {1,2,3,4,5,6,7,8,9,10};
    int list2[10] = {11,12,12,14,15,16,17,18,19,20};

    for (int i = 0; i < 10; i++) {
        cout<<list1[i]<<"\t";
    }
    cout<<endl;
    for (int i = 0; i < 10; i++) {
        cout<<list2[i]<<";";
    }
    cout<<endl;
}
doActionA();
```

在一段逻辑中，需要打印两个数组。我们发现两个for循环是重复代码，都是遍历元素；而循环体里面的打印则稍微有区别，所以我们会把共通的抽出来，用一个函数专门做循环，传一个函数指针做业务。

```c++
void foreach(int arr[], int size, void fun(int)){
    for (int i = 0; i < size; i++) {
        fun(arr[i]);
    }
    cout<<endl;
}
void log1(int val) {
    cout<<val<<"\t";
}
void log2(int val) {
    cout<<val<<";";
}
void doActionB() {
    int list1[10] = {1,2,3,4,5,6,7,8,9,10};
    int list2[10] = {11,12,12,14,15,16,17,18,19,20};

    foreach(list1, 10 ,log1);//以log1的方式打印
    foreach(list2, 10 ,log2);//以log2的方式打印

}
doActionB();

输出：
1	2	3	4	5	6	7	8	9	10	
11;12;12;14;15;16;17;18;19;20;
```

代码运行自然没问题，但是从设计的角度，有几个缺陷。

1. foreach函数或许其它地方也会用到，放外面没问题；
2. log1和log是没必要抽出来封装一个函数的，但是foreach必须要传递一个函数指针，作为回调。所以只好定义两个函数：log1，log2
3. log1和log2函数一定是只有doActionB里面才用的，放外面，平白多了两个函数定义。如果能把log1和log2的定义放到doActionB里面就好了；甚至我希望不要定义log1和log2。
4.  然而，C++中不能嵌套定义函数。

这种情况可以用lambda改造如下：

```c++
void foreach(int arr[], int size, void (*fun)(int)){
    for (int i = 0; i < size; i++) {
        fun(arr[i]);
    }
    cout<<endl;
}
void doActionC() {
    int list1[10] = {1,2,3,4,5,6,7,8,9,10};
    int list2[10] = {11,12,12,14,15,16,17,18,19,20};

    foreach(list1, 10 ,[](int a){
        cout<<a<<"\t";
    });
    
    foreach(list2, 10 ,[](int a){
        cout<<a<<";";
    });
}

调用doActionC输出如下：
1	2	3	4	5	6	7	8	9	10	
11;12;12;14;15;16;17;18;19;20;
```

这样的代码看起来更自然一点，不再需要莫名其妙的新增两个函数。

我发现，两个打印的区别，只是间隔符不一样，我声明一个separator变量，进一步抽取共同代码：

```c++
void doActionD() {
    int list1[10] = {1,2,3,4,5,6,7,8,9,10};
    int list2[10] = {11,12,12,14,15,16,17,18,19,20};

    char separator;

    auto log = [&separator](int a){
        cout<<a<<separator;
    };

    separator = '\t';
    foreach(list1, 10 , log);

    separator = ',';
    foreach(list2, 10 ,log);

}
```

我把lambda表达式赋给log变量，此lambda捕获了变量separator。然后，当我想使用制表符分隔打印时，就

` separator = '\t';`, 然后调用foreach；当我想使用逗号时，就`separator = ',';`, 然后调用foreach。

但是这里出现了编译错误：

```txt
 error: cannot convert ‘f7::doActionD()::<lambda(int)>’ to ‘void (*)(int)’ for argument ‘3’ to ‘void f7::foreach(int*, int, void (*)(int))’
         foreach(list1, 10 , log);
```

这里说，lambda不能赋给函数指针？？？前面不是可以赋值吗？

***无状态Lambda(即没有捕获)可以转换为函数指针*。**

这里lambda捕获了变量`separator`，所以是有状态，不能转成函数指针类型， **std::function** 和 **std::bind**解决此问题。

#### std::function

std::function可以hold住任何可以通过“()”来调用的对象，包括：

- 普通函数
- 成员函数
- 函数指针
- 伪函数（即具有operator()成员函数的类的对象）
- lambda
- std::bind（见下文）后的结果

C++11中，std::function和std::bind统一了可调用对象的各种操作。

进一步改造如下：

```c++
void foreach2(int arr[], int size, std::function<void(int)> fun){
    for (int i = 0; i < size; i++) {
        fun(arr[i]);
    }
    cout<<endl;
}
void doActionD() {
    int list1[10] = {1,2,3,4,5,6,7,8,9,10};
    int list2[10] = {11,12,12,14,15,16,17,18,19,20};

    char separator;

    auto log = [&separator](int a){
        cout<<a<<separator;
    };

    separator = '\t';
    foreach2(list1, 10 , log);

    separator = ',';
    foreach2(list2, 10 ,log);
}

打印：
1	2	3	4	5	6	7	8	9	10	
11,12,12,14,15,16,17,18,19,20,
```

我们可以养成习惯： 凡是参数是作为回调的，一律使用std::function。

接下来继续改造。我们用using 来定义别名，增加可读性

```c++
using Callback = std::function<void(int)>;//别名
void foreach3(int arr[], int size, Callback fun){
    for (int i = 0; i < size; i++) {
        fun(arr[i]);
    }
    cout<<endl;
}

void doActionE() {
    int list1[10] = {1,2,3,4,5,6,7,8,9,10};
    int list2[10] = {11,12,12,14,15,16,17,18,19,20};

    char separator;

    Callback log = [&separator](int a){//知道类型名是Callback，而且很简洁，也就不必使用auto
        cout<<a<<separator;
    };

    separator = '\t';
    foreach3(list1, 10 , log);

    separator = ',';
    foreach3(list2, 10 ,log);
}
打印：
1	2	3	4	5	6	7	8	9	10	
11,12,12,14,15,16,17,18,19,20,
```

#### std::bind

可将std::bind函数看作一个通用的函数适配器，它接受一个可调用对象(如：函数名)，生成一个新的可调用对象来“适应”原对象的参数列表。

```c++
double my_divide(double x, double y) {
    return x/y;
}
auto fn_half = std::bind(my_divide,_1,2);  
std::cout << fn_half(10) << '\n';                        // 5
```

my_divide函数有两个参数，通过std::bind, 转成只有一个参数的可调用对象，my_divide的第二个参数固定为2，于是，`fn_half(10)`就等价于`my_divide(10, 2)`。

还用前面的例子，再改造一下.

```c++
void doActionF() {
    int list1[10] = {1,2,3,4,5,6,7,8,9,10};
    int list2[10] = {11,12,12,14,15,16,17,18,19,20};

    //这里我不捕获separator了，而是作为lambda的参数
    auto log = [](int a, char separator){
        cout<<a<<separator;
    };

    //log有两个参数，显然不能直接传给foreach3，需要用std::bind变成一个参数
    Callback call = std::bind(log, std::placeholders::_1, '\t');
    foreach3(list1, 10 , call);

    Callback call2 = std::bind(log, std::placeholders::_1, ',');
    foreach3(list2, 10 ,call2);
}
 doActionF();//调用

//执行
1	2	3	4	5	6	7	8	9	10	
11,12,12,14,15,16,17,18,19,20,
```

#### 捕获变量的作用域

```c++
Callback testScope() {
    char separator = ',';

    Callback log = [&separator](int a){//按引用捕获，separator是局部变量，虽然被捕获，但是执行这个lambda时已经释放
        cout<<a<<separator;
    };

    return log;
}

 Callback log = testScope();
log(3);
cout<<"这里插入一行"<<endl;
log(3);//separator应该释放掉了

执行：
3,这里插入一行
3
```

观察结果，第二个log(3)没有把separator打印出来，因为separator是局部变量，testScope执行完就释放掉了。跟函数不能返回引用是一个道理。

这种情况就不应该按引用捕获，而是按值捕获。

全局变量大概没有此问题，但是全局变量貌似也没有捕获的必要。



至此，应该已经大致掌握有关知识了。关于这些知识的细枝末节，下文贴一部分比较重要的。

### lambda 细节

其基本格式如下：

```c++
[捕捉列表] (参数) mutable -> 返回值类型 {函数体}
```

#### 捕获列表

lambda 表达式可以通过捕获列表捕获一定范围内的变量：

* [] 不捕获任何变量。
* [&] 捕获外部作用域中所有变量，并作为引用在函数体中使用（按引用捕获）。
* [=] 捕获外部作用域中所有变量，并作为副本在函数体中使用（按值捕获）。
* [=，&foo] 按值捕获外部作用域中所有变量，并按引用捕获 foo 变量。
* [bar] 按值捕获 bar 变量，同时不捕获其他变量。
* [this] 捕获当前类中的 this 指针，让 lambda 表达式拥有和当前类成员函数同样的访问权限。如果已经使用了 & 或者 =，就默认添加此选项。捕获 this 的目的是可以在 lamda 中使用当前类的成员函数和成员变量。this 指针只能按值捕获 [this] ，不能按引用捕获 [&this] 。
* [x, &y] 显式按值捕获x，按引用捕获y

#### 参数

参数列表与普通函数的参数列表一致。如果不需要传递参数，可以联连同 `()` 一同**省略**。

#### mutable

`mutable` 可以取消 Lambda 的常量属性，因为 Lambda 默认是 const 属性，multable 仅仅是让 Lamdba 函数体修改值传递的变量，但是修改后并不会影响外部的变量

#### ->返回类型

`->返回类型`如果是 `void` 时，可以连 `->` 一起**省略**。如果返回类型很明确，可以省略，让编译器自动推倒类型。

#### 函数体

函数体和普通函数一样，除了可以使用参数之外，还可以使用捕获的变量。

通过上面呢规则说明，可以得出，最简单的 Lambda 函数可以是如下：

```c++
[] {}
```

