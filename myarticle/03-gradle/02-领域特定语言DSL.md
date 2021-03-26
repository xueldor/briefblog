在介绍gradle之前，先介绍一下DSL的概念

领域特定语言，针对一个特定的领域，具有受限表达性的一种计算机程序语言

* 第一个是计算机程序设计语言，使用DSL来指挥计算机做事情
* 语言性（一种特定的语言）
* 受限的表达性，并不像同通用的设计语言那样具有广泛的能力
* 针对一个明确的领域。

正则表达式、SQL、AWK以及Struts的配置文件等都是DSL。

**为什么需要DSL？**

1、提高开发效率，通过DSL来抽象构建模型，抽取公共的代码，减少重复的劳动；

2、和领域专家沟通，领域专家可以通过DSL来构建系统的功能；

3、执行环境的改变，可以弥补宿主语言的局限性；

**DSL的处理可能包含步骤**

1、DSL脚本；

2、解析脚本；

3、语义模型；

4、生成代码或者执行模型

**为什么groovy适合构建DSL**

1、不需要class文件，可以直接执行脚本；

2、闭包等特性以及语法的简介，使用非常灵活；

3、可以和java系统无缝的整合；

4、Groovy自身不是 DSL。 Groovy官方已经发布了较多基于 Groovy书写的 DSL，比如 GANT， GORM， XMLBuilder， HtmlBuilder等等；

**使用groovy做DSL的例子**

例如这个：

```
ParseDsl.make {
        to "Nirav Assar"
        from "Barack Obama"
        body "How are things? We are doing well. Take care"
        idea "The economy is key"
        request "Please vote for me"
        xml
      }
```

那必须要有地方解析这个规则：

```groovy
import groovy.xml.MarkupBuilder;
/**
* 解析一个自定义的DSL文本的类
*/
class ParseDsl {
 
    String toText
    String fromText
    String body
    def sections = []
 
     
    def static make(closure) {
        ParseDsl memoDsl = new ParseDsl()
        closure.delegate = memoDsl
        closure()
    }
 
    def to(String toText) {
        this.toText = toText
    }
 
    def from(String fromText) {
        this.fromText = fromText
    }
 
    def body(String bodyText) {
        this.body = bodyText
    }
 
    def methodMissing(String methodName, args) {
        def section = new Section(title: methodName, body: args[0])
        sections << section
    }
 
    def getXml() {
        doXml(this)
    }
 
    def getHtml() {
        doHtml(this)
    }
 
    def getText() {
        doText(this)
    }
 
    private static doXml(ParseDsl memoDsl) {
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)
        xml.memo() {
            to(memoDsl.toText)
            from(memoDsl.fromText)
            body(memoDsl.body)
            for (s in memoDsl.sections) {
                "$s.title"(s.body)
            }
        }
        println writer
    }
 
    private static doHtml(ParseDsl memoDsl) {
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)
        xml.html() {
            head {
                title("Memo")
            }
            body {
                h1("Memo")
                h3("To: ${memoDsl.toText}")
                h3("From: ${memoDsl.fromText}")
                p(memoDsl.body)
                for (s in memoDsl.sections) {
                    p {
                        b(s.title.toUpperCase())
                    }
                    p(s.body)
                }
            }
        }
        println writer
    }
 
    private static doText(ParseDsl memoDsl) {
        String template = "Memo\nTo: ${memoDsl.toText}\nFrom: ${memoDsl.fromText}\n${memoDsl.body}\n"
        def sectionStrings =""
        for (s in memoDsl.sections) {
            sectionStrings += s.title.toUpperCase() + "\n" + s.body + "\n"
        }
        template += sectionStrings
        println template
    }
}
```

使用者不需要知道怎么解析的，甚至不需要学会groovy，只需要知道你定义的DSL的规则就可以了。

再举一个跟gradle更像的例子：

```groovy
onConsume = {
	rewward("Reward Description"){
		condition{
		}
		grant{
		}
	}
}
```

开发者解析这段脚本：

```groovy
binding.condition = { closure->
	closure.delegate = delegate
	binding.result = (closure() && binding.result)
}
binding.grant = {closure->
	closure.delegate = delegate

	if(binding.result)
		closure()
}
```



于是我们知道gradle到底是什么了。无非就是用groovy实现的一个框架，定义了一套完整的规则，用来构建、发布产品。开发者需要参考官网的文档，编写构建脚本。

