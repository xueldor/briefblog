# Font Awesome

*Font Awesome*是一个矢量图标库

将fontawesome-webfont.eot、fontawesome-webfont.svg、fontawesome-webfont.ttf、fontawesome-webfont.woff这四个文件拷贝到fonts目录，font-awesome.css拷贝到css目录。

新版本(4点几)class命名发生了很大变化，.icon改成了.fa，这里仍以3.2.1版本为例。

打开font-awesome.css，在最前面找到：

````css
@font-face {
  font-family: 'FontAwesome';
  src: url('../fonts/fontawesome-webfont.eot?v=3.2.1');
  src: url('../fonts/fontawesome-webfont.eot?#iefix&v=3.2.1') format('embedded-opentype'), url('../fonts/fontawesome-webfont.woff?v=3.2.1') format('woff'), url('../fonts/fontawesome-webfont.ttf?v=3.2.1') format('truetype'), url('../fonts/fontawesome-webfont.svg#fontawesomeregular?v=3.2.1') format('svg');
  font-weight: normal;
  font-style: normal;
}
````

注意个url，确保与你的几个文件路径相同。

这些矢量图标不能在文本中显示。点击此界面查阅：

https://www.bootcss.com/p/font-awesome/design.html