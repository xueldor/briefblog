### 动态对象

动态对象是Gradle的一项核心设计。Gradle中的属性和方法，如果类本身没有，部分来自于Groovy中的Delegate(闭包有一个delegate属性), 部分归功于动态对象。关于动态对象的设计接口有如下：

```java
public interface DynamicObjectAware {
    DynamicObject getAsDynamicObject();
}
 
public interface ExtensionAware {
    ExtensionContainer getExtensions();
}
 
public interface IConventionAware {
    ConventionMapping getConventionMapping();
}
 
public interface HasConvention {
    Convention getConvention();
}

public interface DynamicObject {
    xxxxxxxx....
}
```

举一个例子，这个例子来自https://blog.csdn.net/liao_hb/article/details/90107631：

```groovy
dependencies {
    compile 'org.codehaus.groovy:groovy-all:2.4.7' 
    compile 'org.scala-lang:scala-library:2.11.1'
}
```

问题来了，IDE可以提示dependencies在Project里，是Project的一个方法`void dependencies(Closure configureClosure);`,但是compile在哪里？既然compile不在Project里面，又为什么能够调用？该闭包的delegate，owner，this都是指向Project的实现对象。

groovy编译器对本例的翻译片段：

```java
callsite.name = "dependencies"
callsite.callCurrent(this, new _run_closure(this, this));
```

继续跟踪这段代码，发现Script把调用责任推卸给了自己的MetaClass。MetaClass在第一篇讲groovy时讲过了。最终会反射调用MetaClass的宿主类也就是Script的methodMissing方法：

```java
public Object methodMissing(String name, Object params) {
	return getDynamicTarget().invokeMethod(name, (Object[])params);
}
```

其中getDynamicTarget()返回的是Project接口实现类的动态对象(ExtensibleDynamicObject)，它实现了DynamicObject接口。

最终，ExtensibleDynamicObject在Project的实现类上找到了 如下方法：

```java
public void dependencies(Closure configureClosure) {
	ConfigureUtil.configure(configureClosure, getDependencies());
}
```

getDependencies() 返回的是一个DependencyHandler接口类型的实现类对象，继续跟踪下去:

```java
public static <T> T configure(@Nullable Closure configureClosure, T target) {
        if (configureClosure == null) {
            return target;
        }
 
        if (target instanceof Configurable) {
            ((Configurable) target).configure(configureClosure);
        } else {
            configureTarget(configureClosure, target, new ConfigureDelegate(configureClosure, target));
        }
 
        return target;
    }
	
    private static <T> void configureTarget(Closure configureClosure, T target, ConfigureDelegate closureDelegate) {
        if (!(configureClosure instanceof GeneratedClosure)) {
            new ClosureBackedAction<T>(configureClosure, Closure.DELEGATE_FIRST, false).execute(target);
            return;
        }
		
	// Hackery to make closure execution faster, by short-circuiting the expensive property and method lookup on Closure
        Closure withNewOwner = configureClosure.rehydrate(target, closureDelegate, configureClosure.getThisObject());
        new ClosureBackedAction<T>(withNewOwner, Closure.OWNER_ONLY, false).execute(target);
    }
```

```java
public void execute(T delegate) {
        if (closure == null) {
            return;
        }
 
        try {
            if (configureableAware && delegate instanceof Configurable) {
                ((Configurable) delegate).configure(closure);
            } else {
                Closure copy = (Closure) closure.clone();//copy就是上面的withNewOwner的副本
                copy.setResolveStrategy(resolveStrategy);//就是上面的Closure.OWNER_ONLY
                copy.setDelegate(delegate); //
                if (copy.getMaximumNumberOfParameters() == 0) {//doCall的参数个数是否等于0
                    copy.call();
                } else {
                    copy.call(delegate); //反射调用闭包的doCall方法,调用的闭包类是脚本中的某一个groovy编译器为我们生成好的Closure的子类，例如：_run_closure2
                }//这个例子中我们没有用到delegate参数，即groovy为我们生成的DefaultDependencyHandler的子类
            }
        } catch (groovy.lang.MissingMethodException e) {
            if (Objects.equal(e.getType(), closure.getClass()) && Objects.equal(e.getMethod(), "doCall")) {
                throw new InvalidActionClosureException(closure, delegate);
            }
            throw e;
        }
    }
```

跟踪到了闭包_run_closure的doCall方法 ：

```java
public Object doCall(Object it) {
    CallSite acallsite1[] = $getCallSiteArray();
    try {
        //acallsite1[0] = "compile"
        acallsite1[0].callCurrent(this, "org.codehaus.groovy:groovy-all:2.4.7");
    } catch (Throwable throwable) {
        throwable.printStackTrace();
    }
    try {
        //acallsite1[1] = "compile"
        return acallsite1[1].callCurrent(this, "org.scala-lang:scala-library:2.11.1");
    } catch (Throwable throwable) {
        throwable.printStackTrace();
    }
    return null;
}
```

闭包中CallSite把调用责任推卸给了自己的MetaClass,这属于是Groovy的内容，在此略过，我们debug一下知道会调到ClosureMetaClass里面，

```java
/**invokeOnOwner = true;
owner就是上面ConfigureDelegate类型的参数；
invokeOnDelegate = fasle;
delegate就是上面闭包的代理设置，它是DefaultDependencyHandler类型
methodName = "compile"
Object[] arguments = new Object[]{"org.codehaus.groovy:groovy-all:2.4.7"}
**/
invokeOnDelegationObjects(invokeOnOwner, owner, invokeOnDelegate, delegate, methodName, arguments){
    ......
    GroovyObject go = (GroovyObject) owner;
    try {
        return go.invokeMethod(methodName, args);
    }catch (MissingMethodException mme) {
       ......
     }
    ......

```

ConfigureDelegat是GroovyObject的子类所以实现了public Object invokeMethod(String name, Object paramsObj)方法,上面的代码可知ConfigureDelegate包装有target，也就是DependencyHandler类型的子类实现，由gradle的类生成器动态地为我们生成 。

ConfigureDelegat一上来就走DefaultDependencyHandler实现类的动态对象接口，即通过其getAsDynamicObject方法获取动态对象，并在其动态对象身上查找方法，其动态对象是一个复合对象(CompositeDynamicObject类型)，也就是复合多个动态对象，因此逐一遍历查找，其中一个的查找就调用至如下：

```java
/**
metaClass : groovy.lang.MetaClassImpl@f6de586[class org.gradle.api.internal.artifacts.dsl.dependencies.DefaultDependencyHandler_Decorated]
bean : DefaultDependencyHandler_Decorated
name = "compile"
Object[] arguments = new Object[]{"org.codehaus.groovy:groovy-all:2.4.7"}**/
protected Object invokeOpaqueMethod(MetaClass metaClass, String name, Object[] arguments) {
    return metaClass.invokeMethod(bean, name, arguments);
}
```

最终会反射调用MetaClass的宿主类也就是DefaultDependencyHandler_Decorated类（gradle的类生成器动态生成，继承自DefaultDependencyHandler）的methodMissing方法。

```java
public Object methodMissing(String name, Object args) {
        Object[] argsArray = (Object[]) args;
        Configuration configuration = configurationContainer.findByName(name);
        if (configuration == null) {
            throw new MissingMethodException(name, this.getClass(), argsArray);
        }
 
        List<?> normalizedArgs = CollectionUtils.flattenCollections(argsArray);
        if (normalizedArgs.size() == 2 && normalizedArgs.get(1) instanceof Closure) {
            return doAdd(configuration, normalizedArgs.get(0), (Closure) normalizedArgs.get(1));
        } else if (normalizedArgs.size() == 1) {
            return doAdd(configuration, normalizedArgs.get(0), null);
        } else {
            for (Object arg : normalizedArgs) {
                doAdd(configuration, arg, null);
            }
            return null;
        }
    }
 
    private Dependency doAdd(Configuration configuration, Object dependencyNotation, Closure configureClosure) {
        if (dependencyNotation instanceof Configuration) {
            Configuration other = (Configuration) dependencyNotation;
            if (!configurationContainer.contains(other)) {
                throw new UnsupportedOperationException("Currently you can only declare dependencies on configurations from the same project.");
            }
            configuration.extendsFrom(other);
            return null;
        }
 
        Dependency dependency = create(dependencyNotation, configureClosure);
        configuration.getDependencies().add(dependency);
        return dependency;
    }
```

完毕！