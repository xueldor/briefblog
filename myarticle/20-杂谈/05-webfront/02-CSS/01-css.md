# CSS精要
## 三种引入方式
1. 行内样式表  
标签的style属性。  
```html
<p style="color: red;"></p>
````

2. 内部样式表
一般放在head中间  
```html
<style>
  div {
      color: red;
  }
</style>
```
3. 外部样式表
新建文件，后缀为css，比如 style.css 。文件里直接写样式，不需要style标签。  
html里面引入：
```html
<link rel="stylesheet" href="style.css">
```

## emmet语法
1. 空白html文件，敲一个！号，回车，直接生成骨架。

2.  直接写标签名，按tab建，直接生成。

3. 生成多个标签，标签名加*num。比如  div*10  ,生成10个div。

4. 父子关系用  > 。比如  ul>li,生成:
```html
<ul>
        <li></li>
</ul>
```
5. 兄弟关系用+号，如比 div+p,生成
```html
   <div></div>
    <p></p>
```
6. 类名用点（.）,id用井号(#),比如p#page.today, 生成：
```html
  <p id="page" class="today"></p>
```
7. $表示自增，比如div.today$*3, 生成：
```html
    <div class="today1"></div>
    <div class="today2"></div>
    <div class="today3"></div>
```
8. 花括号内表示内容，比如div{text},生成：
```html
<div>text</div>
```
9. 省略标签名，则生成的是div标签。比如直接#div回车，则生成
`<div id="eee"></div>`

10. 生成css语句，只要输入首字母缩写就可以了。比如输入 fsi ，回车生成：
`font-style: italic;`

## 选择器
### 基础选择器
1. 标签选择器（使用较多）
```css
p {

}
```
2. 类选择器(s使用最多)
```css
.today {

}
```
3. id选择器（一般和js搭配使用）  
```css
#monday {

}
```
4. 通配符选择器（特殊情况使用）  
选出所有元素

```css
* {

}
```
一个典型的使用场景是取消所有元素的默认边距：
```css
* {
          margin: 0;
          padding: 0;
}
```
### 复合选择器
1. 后代选择器，可以是间接后代(子、孙、重孙都可以)。用一个空格：
```css
div  .tag {

}
```
表示选择div标签里面的带有class为tag的标签。  
2. 子选择器  
与后代选择器的区别是，必须是直接后代（亲儿子）。用一个大于号。
```css
div>p {

}
```
3. 并集选择器
用逗号表示，表示“选择xxx和xxx”，把每组都赋予样式。
```css
div,
p  .pig{
    color: red;
}
```
把页面内的div和‘class是pig的p’都赋予红色显示属性。

### 伪类选择器
用冒号。
1. 链接伪类  
a:link /*未访问的链接*/  
a:visited  /*访问过的链接*/  
a:hover  /*鼠标悬浮于其上的链接*/  
a:active  /*鼠标按下但是未弹起的链接*/  
必须按照上面的顺序写。  
a链接在浏览器中有默认样式，所以实际项目中总是需要单独指定。通常在项目中我们只指定a和a:hover  
```css
a {
    color: gray;
}
a:hover {
    color: red;
}
```
实际使用时，不一定非要写成a:hovor的形式, 如果a标签有一个class叫class='aclass',那么写成`.aclass:hovor`是一样的。  
实际上:hovor可以用在所有元素上，只不过用的最多的就是链接。  

2. focus伪类
选择获的焦点的标签。主要用在input标签上面。 
```css
input:focus {
    background-color: yellow;
}
```

## 显示模式
1. 块元素
h1～h6、p、div、ul、ol、li等。  
特点是：  
 1） 独占一行；  
 2）高、宽、内外边距都可以控制;   
 3） 默认宽度是父容器的宽度  
 4）里面可以嵌套任何元素  
 
2. 行内元素(又称内联元素)
a、strong、b、em、i、del、s、ins、u、span等。
特点是：  
1） 不会独占一行，也就是一行可以显示多个行内元素  
2） 直接设置高度、宽度 是无效的  
3） 默认宽度是里面的内容的宽度  
4） 里面只能放文本或行内元素，不能放块元素。   
5） 特例是链接`<a>`元素，链接里面不能放链接 ，链接里面可以放块级元素，但最好还是转换一下块级模式。   

3. 行内块元素
同时具有块元素和行内元素的特点。
img、input、td等元素。  
1） 一行可以显示多个(行内元素特点)，但是之间会有缝隙。  
2） 宽、高、内外边距都可以控制（块级元素特点）  

4. 显示模式转换（重要）  
> 转为块元素： display: block;  
> 转为行内元素： display: inline;  
> 转为行内块： display: inline-block;  




## 文字显示有关的
1. font-family(字体)、font-size（大小）、font-weight(粗细)、font-style（倾斜）  
复合写法是：`font: italic 700 16px 'Microsoft yahei''`，用复合写法时，顺序不可颠倒，font-size和font-family必不可少。  

2.  文本颜色color、文本对齐text-align、text-decoration(下划线、上划线、删除线)、text-indent（首行缩进）  
text-indent建议用em作为单位，因为em表示一个字符的宽度，可以自适应字体大小。
3.  line-height (行高)，表示一行的高度。在文字上方分配一段高度，下方分配一段高度，再加上文字自身高度，即为行高。   
当使用复合写法时，行高在font-size后面用“/”分隔：
font: 12px/24px   或 font: 12px/1.5  
后者表示font-size的1.5倍。  

小技巧： 指定行高和元素的高度相等，即可使文字垂直居中。原理很简单，不难领悟。  

## 背景图片
```css
        background-color: yellow;/* yellow  ,  #FF0000   , rgb(),rgba()等形式*/
        background-image: url(xxxx);
        background-position: top left;/*百分比、方位、xy*/
        background-repeat: no-repeat;
        background-attachment: fixed;/*当内容滚动时背景固定*/
        background: black url(xxx) repeat-x  fixed center top /*复合写法，没有特定顺序*/
```

## 三大特性
1.  层叠性
当一个元素的同一个式样定义了多次，在其它方面的优先级都相同的情况下，后面定义的覆盖前面定义的。你可以理解为代码从上往下执行，后面的把上面的顶替掉了。  
2. 继承性
某些样式，子元素会继承父亲的样式。有一些元素有默认样式(比如a标签默认有一个下划线)，那么对于这个样式，显然不会继承父元素的样式。这种默认样式，可以理解为浏览器自动添加了css样式定义，根据层叠性，一旦我们自己定义了样式，就可以替代默认样式了。  
3.  优先级
当不同的选择器定义了同样的样式时，根据优先级决定最终应用的样式。优先级如下：  
!important   >   行内样式  >  ID选择器  >  类选择器和伪类选择器  >  标签选择器  >  继承的样式和通配符(*)定义的样式  

总的来说，遵守“针对性越强，则优先级越高”的规律，不过浏览器内部是根据权重来决定的。  
继承的优先级权重是0,哪怕是!important继承过来也是0.  
如果优先级相同，则执行层叠性。   

**复合选择器权重叠加**  
!important :   无穷大  
行内样式：  1,0,0,0   
id选择器： 0,1,0,0  
类(伪类)选择器: 0,0,1,0   
元素选择器 ： 0,0,0,1   
继承或*  :  0,0,0,0  

叠加后的权重 ：  
* div ul li      ---------> 0,0,0,3
* div div div div div div div div div div   -------> 0,0,0,10
* .nav ul li --------->  0,0,1,2
* a:hovor  ----> 0,0,1,1  (a标签是0,0,0,1 ;  :hovor伪类是0,0,1,0   。相加可的)
* .nav a -------> 0,0,1,1
注意，权重的相同位相加，但永远不会进位。所以无论多少个标签选择器的权重相加，也不可能超过类选择器。  



important 用法如下：  
background: red !important;//样式定义后面，分号前面。  

## 规范
div {//选择器跟花括号之间加一个空格  
    color: red;//冒号后面加一个空格  
}  

div,//并集选择器之间分行  
p {  

}
vs code 在设置里设置保存文件时自动格式化。

