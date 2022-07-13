## 简单的宏定义

`#define 标识符 替换列表`

1. 替换列表可以是数，字符串字面量，标点符号，运算符，标识符，关键字，字符常量。
2. 替换列表是可以为空的
3. 第一个符号是标识符，后面全是替换列表。如`#define NN = 100  ;`,  故意在“=  100  ;“中间加了几个空格。那么只有NN 是标识符，“= 100  ;“整体都是被替换的。

示例：`#define N 100`

错误使用：

```c++
#define N = 100//不要等号

#define N 100;//不要分号
cout<<N<<endl;//因为分号，加分号就变成  cout<<100;<<endl;
```

正确使用1：

```c++
//1. 中间加多少空格，甚至tab符，是无关紧要的
//2. 可以加注释，注释不影响代码
#define  /*hehe */  N			1//haha
cout<<N<<endl;
```

正确使用2：

```c++
//相当于int i = 3;
#define N = 3
int main(){
	int i N;//N 替换成= 3，即int i = 3;
	cout<<i<<endl;
}
```

正确使用3：

```c++
#define BEGIN {
#define END }

int main()BEGIN
printf ("DEFINE----\n");
END
//BEGIN替换成{， END替换成}
```

定义一个死循环:

```c++
#define LOOP for(;;)

int main(){
	int i = 0;
	LOOP{
		cout<<++i<<endl;
	}
}

```

重新定义数据类型：

```c++
#define uint32 unsigned int
#define int32 int
#define int64 long long int

int main(){
	uint32 a = 0x7FFFFFFF;
	int32 b = 0x7FFFFFFF;
	cout<<(a+1)<<"   b="<<(b+1)<<endl;
    //输出：2147483648   b=-2147483648
}
```

这种代码C语言很常见，常用来做跨平台，增强可移植性。不过C++里更推荐用typedef

正确使用4：

```c++
#define __uint32 unsigned int
#define uint32 __uint32
#define int32 uint32
```

多行定义(换行的时候加上一个" \ ")：

```c++
#define __uint64 unsigned \
	long \
	long \
	int
int main(){
	__uint64 a = 2;
	cout<<sizeof a<<endl;
}
```

取消宏

`#undef 标识符`

### 利用宏定义解决头文件重复包含问题

```c++
#ifndef RS_REF_BASE_H
#define RS_REF_BASE_H

xxxxxx
    
#endif // RS_REF_BASE_H
```

## 带参数的define

像函数那样接受一些参数

```c++
#define max(x, y) x>y?x:y//括号(x , y)内可以加空格，但max和"("中间不能有空格，否则(x,y)不是参数，而是被替换的部分

int a = 5;
int max = max(3,4);
cout<<"max="<<max<<endl;
```

但是最好在每个参数上加个括号，像这样：`#define max(x,y) (x)>(y)?(x):(y)`。否则可能出乎意料：

```c++
#define multiply_2(x) x * 2
int main(){
	int ret = multiply_2(3+2);
	cout<<"ret="<<ret<<endl;
}
//希望的是3+2=5,5*2=10，输出10
//实际是替换成 3+2 * 2，结果是7。所以x外面要加括号：
#define multiply_2(x)  (x) * 2
```

最外层也要加个括号，否则：

```c++
#define add(x , y) x + y
int main(){
	int ret = add(3, 2) * 4;
	cout<<"ret="<<ret<<endl;
}
//希望的是（3+2）*4，实际替换成  3+ 2 *4
```

所以我们建议，**所有的层次都要加括号**。



define带参数，有点像模板函数，可以减少重复的相似代码。以下是安卓智能指针的源码，算一下不用define需要多少代码量：

```c++
//RefBase.h
#define COMPARE_WEAK(_op_)                                      \
inline bool operator _op_ (const sp<T>& o) const {              \
    return m_ptr _op_ o.m_ptr;                                  \
}                                                               \
inline bool operator _op_ (const T* o) const {                  \
    return m_ptr _op_ o;                                        \
}                                                               \
template<typename U>                                            \
inline bool operator _op_ (const sp<U>& o) const {              \
    return m_ptr _op_ o.m_ptr;                                  \
}                                                               \
template<typename U>                                            \
inline bool operator _op_ (const U* o) const {                  \
    return m_ptr _op_ o;                                        \
}

// Operators
COMPARE_WEAK(==)
COMPARE_WEAK(!=)
COMPARE_WEAK(>)
COMPARE_WEAK(<)
COMPARE_WEAK(<=)
COMPARE_WEAK(>=)
//等于COMPARE_WEAK(==)这一行代码就生成了4个函数
```



### 特殊单行定义：

```c++
#define strr(x) "x"//"x"是个字符串，那么里面的x是当成参数，还是普通x字符
//#define strr(x) " x "//或者哪怕前后加上空格
int main(){
	char* ret = strr(3);
	cout<<"ret="<<ret<<endl;
}
//输出
ret=x
```

所以，字符串内的参数是不会替换的。所以只能把参数拿到双引号外面：

```c++
#define strr(x)  "aa"x"bb"
char *ret = strr(4);//错误
然而这样就成了`char *ret = "aa"4"bb";` ,显然语法错的。除非参数刚好是字符串：
char *ret = strr("4");//正确，因为"aa""4""bb"是C++支持的连接字符串字面量
```
我们期望的是得到"aa4bb",参数是int，就需要用特殊符号:
```c++
#define strr(x)  "aa"#x"bb"
int main(){
	char *ret = strr(4);
	cout<<"ret="<<ret<<endl;//ret=aa4bb

	char *ret2 = strr(43s@q8);//你看参数都是什么玩意，但就是能正常输出
	cout<<"ret2="<<ret2<<endl;//ret2=aa43s@q8bb
}
```

几种用在宏定义里的 特殊符号：

* #@x , #@ 符号会将宏的参数加上单引号，变成一个字符字面量
*  \#x ,#符号会将宏的参数加上双引号，变成一个字符串字面量
* T_##x, ##符号是记号粘黏符号，将前后的字符粘黏起来

T_##x的例子：

```c++
#define A(x) T_##x
A(1);//x=1;
//替换为：T_1
//##符号是记号粘黏符号，将前后的字符粘黏起来。如果写成
#define A(x) T_ x
//那么中间多了空格。如果写成
#define A(x) T_x
//那么T_x是一个整体，而不是把x当参数，
//所以这里必须用##
```

比较一下，`T_##x`用的`##`，`"T_"#x`用一个井号。

总的来说，define宏定义有的时候会使代码变得难读和古怪，有时还会让IDE不能智能的分析语法，所以不建议那些奇怪的用法，除非确实能大大降低代码量。