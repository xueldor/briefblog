```javascript
<script type="text/x-mathjax-config">
    MathJax.Hub.Config({
    tex2jax: { inlineMath: [['$','$'], ['\\(','\\)']], processClass: 'math', processEscapes: true },
    TeX: {
        equationNumbers: { autoNumber: ['AMS'], useLabelIds: true },
        extensions: ['extpfeil.js', 'mediawiki-texvc.js'],
        Macros: {bm: "\\boldsymbol"}
    },
    'HTML-CSS': { linebreaks: { automatic: true } },
    SVG: { linebreaks: { automatic: true } }
});
</script>
<script src="https://mathjax.cnblogs.com/2_7_5/MathJax.js?config=TeX-AMS-MML_HTMLorMML&amp;v=20200504"></script>
```

## MathJax简介

MathJax是一款运行在浏览器中的开源的数学符号渲染引擎，使用MathJax可以方便的在浏览器中显示数学公式，不需要使用图片。目前，MathJax可以解析Latex、MathML和ASCIIMathML的标记语言。(Wiki)

## 引入MathJax

在页脚处，引入官方的cdn

```
<script src="//www.90168.org cdn.mathjax.org/mathjax/latest/MathJax.js?config=TeX-AMS-MML_HTMLorMML"></script>
```

官方cdn的js在国内访问慢，所以我们一般引入的是国内的公共资源cdn提供的js,这里特别感谢BootCDN

```
<script src="//cdn.bootcss.com/mathjax/2.7.0/MathJax.js?config=TeX-AMS-MML_HTMLorMML"></script>
```

但这个js还是会调用到 cdn.mathjax.org 里的一些配置js文件，我们最好在head内加一个dns-prefetch，用于网页加速，了解更多可以访问我另外一篇文章：[here](https://www.linpx.com/p/small-practice-of-prefetching-dns.html)

```
<link rel="dns-prefetch" href="//cdn.mathjax.org" />
```

### 外联config说明

我们引入MathJax，发现链接后面多了个`?config=TeX-AMS-MML_HTMLorMML`

这个多出来的东西其实是告诉MathJax，我们要用到的叫`TeX-AMS-MML_HTMLorMML.js`的配置文件，其用来控制显示数学公式的HTMl显示输出

这个配置文件其实也可以通过cdn获取，官方例子如下

```
<script type="text/javascript"
   src="https://cdn.mathjax.org/mathjax/latest/MathJax.js?config=TeX-AMS_HTML,http://myserver.com/MathJax/config/local/local.js">
</script>
```

可惜BootCDN并没有提供这个js，`MathJax.js`也用到其他js,这些js都来自官方的cdn里，所以这也是解释了上面为什么我们需要对官方cdn进行加速

下面是官方更详细的`TeX-AMS-MML_HTMLorMML`配置文件的说明

> This configuration file is the most general of the pre-defined configurations. It loads all the important MathJax components, including the TeX and MathML preprocessors and input processors, the AMSmath, AMSsymbols, noErrors, and noUndefined TeX extensions, both the native MathML and HTML-with-CSS output processor definitions, and the MathMenu and MathZoom extensions.
>
> In addition, it loads the mml Element Jax, the TeX and MathML input jax main code (not just the definition files), as well as the toMathML extension, which is used by the Show Source option in the MathJax contextual menu. The full version also loads both the HTML-CSS and NativeMML output jax main code, plus the HTML-CSS mtable extension, which is normally loaded on demand.

更多配置文件信息请看：here

http://docs.mathjax.org/en/la...

### 内联config说明

与会同时，官方其实还提供了一个能让我们内联一个配置选项的功能

很简单就是使用`<script></script>`标签对，但注意的是需要声明类型`type="text/x-mathjax-config"`。要想让这个内联配置生效就得放在[光棍影院](http://www.bsck.org/)`MathJax.js`之前，例子如下

```
<script type="text/x-mathjax-config">
MathJax.Hub.Config({
});
</script>
<script type="text/javascript" src="path-to-MathJax/MathJax.js"></script>
```

其中`MathJax.Hub.Config()`里的配置选项是本篇文章的重点

## 识别公式

我们可以通过`MathJax.Hub.Config()`中`tex2jax`去实现公式识别

官方例子，如下

```
<script type="text/x-mathjax-config">
MathJax.Hub.Config({
    tex2jax: {
        inlineMath: [ ['$','$'], ["\\(","\\)"] ],
        displayMath: [ ['$$','$$'], ["\\[","\\]"] ]
    }
});
</script>
<script type="text/javascript" src="path-to-MathJax/MathJax.js"></script>
```

其中`inlineMath`识别的单行内的数学公式,我们可以通过`$ ... $`或`\( ... \)`去识别这种数学公式

效果如下:

When ane0, there are two solutions to (ax2+bx+c=0)

那么`displayMath`就是识别整个独立段落的数学公式并且居中显示,我们可以通过`$$ ... $$`或`\[ ... ])新视觉影院6080`去识别这种数学公式

效果如下:

 

x=−bpmsqrtb2−4acover2a

 

在中文世界里，我们往往喜欢用()或者[]去备注或者圈住重要的字段，所以在一般情况下我们并不需要`\( ... \)`和`\[ ... ])`去识别公式

但也会有遇到两个`$$`的情况造成误伤，别急，先看完，你就知道怎么解决了

## 区域选择性识别

### 约束识别范围

我们的数学公式通常是在文章里，那么如何实现只在文章的标签对里面去做公式识别，如下

```
var mathId = document.getElementById("post-content");
MathJax.Hub.Config({
    tex2jax: {
        inlineMath: [ ['$','$'], ["\\(","\\)"] ],
        displayMath: [ ['$$','$$'], ["\\[","\\]"] ]
    }
});
MathJax.Hub.Queue(["Typeset",MathJax.Hub,mathId]);
```

默认情况下,`MathJax.Hub.Queue(["Typeset",MathJax.Hub])`是对整个DOM树进行识别的

我们要约束识别范围，官方文档告诉我们`MathJax.Hub.Queue`的第三个参数就是识别范围，上面的代码就是告诉我们要在id为`post-content`的标签内去做公式识别

### 避开特殊标签和Class

还有其他方法吗？

有，那就是避开一些特殊的标签或者Class,如下

```
MathJax.Hub.Config({
    tex2jax: {
        inlineMath:  [ ["$", "$"] ],
        displayMath: [ ["$$","$$"] ],
        skipTags: ['script', 'noscript', 'style', 'textarea', 'pre','code','a'],
        ignoreClass:"class1"
    }
});
MathJax.Hub.Queue(["Typeset",MathJax.Hub]);
```

其中`skipTags`用来避开一些特殊的标签，这里避开是`script`,`noscript`,`style`,`textarea`,`pre`,`code`,`a`的标签内

`ignoreClass`用来避开标签内声明的CSS Class属性，这里避开的是带有`class="class1"`的标签内

如果我们不想让mathjax识别评论里的公式就可以用`ignoreClass`

如果有多个Class需要避开，我们可以通过 `|` 来区分，写成`ignoreClass: "class1|class2"`基于可以了

### 更多

获取更多`tex2jax`的配置信息访问：[here](http://docs.mathjax.org/en/latest/options/tex2jax.html)

## 美化数学公式

### 去掉蓝框

![img](00-MathJax/bVI3S9)

上图所示的是，点击该公式时周围有一圈蓝色的边框，我们可以通过添加CSS去掉，如下

```
.MathJax{outline:0;}
```

如果要改变字体大小，如下

```
.MathJax span{font-size:15px;}
```

### 扩展功能

为了更好实现美化数学公式，我们需要扩展一下`MathJax.Hub.Config()`，如下

```
MathJax.Hub.Config({
    extensions: ["tex2jax.js"],
    jax: ["input/TeX", "output/HTML-CSS"],
    tex2jax: {
        inlineMath:  [ ["$", "$"] ],
        displayMath: [ ["$$","$$"] ],
        skipTags: ['script', 'noscript', 'style', 'textarea', 'pre','code','a'],
        ignoreClass:"class1"
    },
    "HTML-CSS": {
    }
});
MathJax.Hub.Queue(["Typeset",MathJax.Hub]);
```

我们可以再`HTML-CSS`添加可用字体，如下

```
"HTML-CSS": {
    availableFonts: ["STIX","TeX"]
}
```

我们要关闭下图的公式右击菜单

![img](00-MathJax/bVI3Tb)

也是在`HTML-CSS`添加设置，如下

```
"HTML-CSS": {
    showMathMenu: false
}
```

### 去掉加载信息

`Mathjax.js`在加载的时候，我们可以再网页左下角看到加载情况，可以直接在`MathJax.Hub.Config()`里配置去掉，如下

```
MathJax.Hub.Config({
    showProcessingMessages: false,
    messageStyle: "none"
});
```

## 整理

这里我整理两份可以整合到主题的代码，请根据自己的需要修改，我用的是第一份

整理一

```
<script type="text/x-mathjax-config">
MathJax.Hub.Config({
    showProcessingMessages: false,
    messageStyle: "none",
    extensions: ["tex2jax.js"],
    jax: ["input/TeX", "output/HTML-CSS"],
    tex2jax: {
        inlineMath:  [ ["$", "$"] ],
        displayMath: [ ["$$","$$"] ],
        skipTags: ['script', 'noscript', 'style', 'textarea', 'pre','code','a'],
        ignoreClass:"comment-content"
    },
    "HTML-CSS": {
        availableFonts: ["STIX","TeX"],
        showMathMenu: false
    }
});
MathJax.Hub.Queue(["Typeset",MathJax.Hub]);
</script>
<script src="//cdn.bootcss.com/mathjax/2.7.0/MathJax.js?config=TeX-AMS-MML_HTMLorMML"></script>
```

整理二

```
<script type="text/x-mathjax-config">
var mathId = document.getElementById("post-content");
MathJax.Hub.Config({
    showProcessingMessages: false,
    messageStyle: "none",
    extensions: ["tex2jax.js"],
    jax: ["input/TeX", "output/HTML-CSS"],
    tex2jax: {
        inlineMath:  [ ["$", "$"] ],
        displayMath: [ ["$$","$$"] ],
        skipTags: ['script', 'noscript', 'style', 'textarea', 'pre','code','a'],
        ignoreClass:"comment-content"
    },
    "HTML-CSS": {
        availableFonts: ["STIX","TeX"],
        showMathMenu: false
    }
});
MathJax.Hub.Queue(["Typeset",MathJax.Hub,mathId]);
</script>
<script src="//www.bsck.org cdn.bootcss.com/mathjax/2.7.0/MathJax.js?config=TeX-AMS-MML_HTMLorMML"></script>
```

## 修复与Instantclick的冲突

代码如下

适用于整理一的代码

```
<script data-no-instant>
InstantClick.on('change', function(isInitialLoad){
    if (isInitialLoad === false) {
        if (typeof MathJax !== 'undefined'){
            MathJax.Hub.Queue(["Typeset",MathJax.Hub]);
        }
    }
});
InstantClick.init();
</script>
```

适用于整理二的代码

 

```
<script data-no-instant>
InstantClick.on('change', function(isInitialLoad){
    if (isInitialLoad === false) {
        if (typeof MathJax !== 'undefined'){
            var mathId = document.getElementById("post-content");
            MathJax.Hub.Queue(["Typeset",MathJax.Hub,mathId]);
        }
    }
});
InstantClick.init();
</script>
```