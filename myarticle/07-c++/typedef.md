## typedef普通用法

```c++
typedef unsigned int uint33;
typedef uint33  _uint33;//好多平台已经定义过uint32,所以这里为了避免错误，故意写成uint33

int main(){
	_uint33 i = 3;
	cout<<i<<endl;

	typedef int vector[10];
	vector v1,v2;//v1 v2的类型是int[10]
	v1[1] = 2;
	cout<<v1[1]<<endl;

	typedef char strings[80];
	strings s1,s2="define type";
	cout<<s2<<endl;

	typedef short int array[20];
	array a={25,36,19,48,44,50};
}
```

总结就是，正常的声明，前面加一个typedef，变量名就成了类型别名。

## typedef 函数指针

**函数指针**

```c
int(*p)(int, int);
```

这个语句就定义了一个指向函数的指针变量 p。首先它是一个指针变量，所以要有一个`“*”`，即`（*p）；`其次前面的 int 表示这个指针变量可以指向返回值类型为 int 型的函数；后面括号中的两个 int 表示这个指针变量可以指向有两个参数且都是 int 型的函数。所以合起来这个语句的意思就是：定义了一个指针变量 p，该指针变量可以指向返回值类型为 int 型，且有两个整型参数的函数。

所以p 的类型为 `int(*)(int，int)`。

这里把变量名去掉，就是他的类型，也就是，把`int(*p)(int, int)`里的p去掉，就是类型 `int(*)(int，int)`。

使用方法：

```c++
void Func(int x) // 声明一个函数
{
    printf("%d",x);
}
void (*p) (int); // 定义一个函数指针
p = Func; // 将Func函数的首地址赋给指针变量p
(*p)(a, b);  // 通过函数指针调用Func函数
p(a, b);//same as (*p)(a, b)
```

正常声明和定义变量是：

```c++
char* pa;
```

所以typedef定义`char*`的别名就是：

```c++
typedef char* pa;
```

也就是声明前面加typedef，那么原来的变量名就成了类型的别名。

按照这个思路，怎么定义typedef 函数指针，就明显了。

```c++
void (*p) (int);// 这是定义一个函数指针
typedef void (*FnP) (int);//FnP是这个函数指针的类型的别名
FnP p;//相当于void (*p) (int)
p = Func;//给指针赋值，让p指向Func函数
FnP p2 = Func;//声明的同时初始化
(*p2)(4);

//对比以下等价代码：
void (*p3)(int) = Func;
p3(3);
//等价，但一个繁琐，另一个增加阅读障碍。
```

如果`typedef void (*FnP) (int);`去掉星号，就是：

```c++
typedef void (FnP) (int);
FnP* p = Func;//FnP*而不是FnP
```

问：以下代码各自是什么意思？

```c++
//file 1
typedef State st;
//file 2
typedef State (CAsmAction::*st)()
```

答：file 1，State是一个类，定义它的别名是st。

file 2， st是 CAsmAction类 的一个成员函数指针， 指向的函数的返回值是 State类对象，没有参数。

### 用途

有时，函数的声明特别复杂: 返回值是一个函数指针；参数也是函数指针；并且作为参数的函数类型，本身也有参数，参数依然是一个函数指针。

以linux里的signal函数为例：

```c++
void (*signal(int ,void(*)(int)))(int);
```

第一，返回值不是void, 而是`void (*)(int)`

第二，signal的参数不是(int), 而是`(int ,void(*)(int))`。最后的(int)是返回函数类型的部分。

第三，signal的第二个参数又是函数，和返回值同类型。

之所以这么难阅读，因为几个函数的声明套在一起。我们可以用typedef一层层剥开。先看一下“man signal”（Linux系统shell里执行man signal）:

```shell
SYNOPSIS
       #include <signal.h>

       typedef void (*sighandler_t)(int);

       sighandler_t signal(int signum, sighandler_t handler);
```

给`void (*)(int)`创建一个别名（sighandler_t）后，原来的声明立马变得简洁，符合我们常规的阅读习惯。

如果要反向分析`sighandler_t signal(int signum, sighandler_t handler)`表示的原型，最初的格式，把“sighandler_t”换成“void (*xxx)(int)”， 然后把里面的xxx换成`signal(int signum, sighandler_t handler)`即可，迭代多次。

## C语言typedef结构体

起到简化结构体声明的作用，C++虽然仍然支持，但是没必要了。

```c++
typedef struct hard{
	float volume;
	double price;
} hard;//用typedef定义别名。hard是"struct hard"的别名。
hard h;
struct hard h2;//如果不用typedef的话。C++不需要前面的struct关键字
```

## 其它

《C++ Primer》中文版（第五版）P584 ，有

```c++
typedef typename std::vector<T>::size_type size_type;
```

为什么typedef后面要加上typename关键字？P593页下半部分解释：C++语言默认情况下，假定通过作用域运算符访问的名字不是类型，所以当我们要访问的是类型时候，必须显示的告诉编译器这是一个类型，通过关键字typename来实现这一点。

意思就是，模板类型在实例化之前，编译器并不知道`vector<T>::size_type`是什么东西，事实上一共有三种可能：

* 静态数据成员 
* 静态成员函数 
* 嵌套类型

默认假定size_type是成员，为了告诉编译器size_type是类型，前面需要加typename。

```c++
template <typename T>
typename T::value_type top(const T& c){
    return typename T::value_type();
}
```

## typedef与#define

typedef只是对已经存在的类型增加一个类型名，而没有创造新的类型。

typedef与#define有相似之处，但事实上二者是不同的，#define是在 预编译 时处理，它只能做简单的字符串替换，而typedef是在 编译时 处理的。它并不是做简单的字符串替换，而是采用如同 定义变量 的方法那样来 声明 一个类型。