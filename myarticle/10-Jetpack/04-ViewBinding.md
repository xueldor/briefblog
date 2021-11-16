## 目的

替代findViewById

## 方案

会为该模块中的每个xml文件生成一个绑定类，绑定类的实例包含这个‘布局xml’具有 ID 的所有视图的直接引用。

## 如何启用

```groovy
android {
        ...
    //Android Studio 4.0版本以上
    buildFeatures {
        viewBinding = true
    }

    //Android Studio 3.6
//    viewBinding {
//        enabled = true
//    }
    }
} 
```

如果想在生成绑定类时忽略某个布局文件，将 `tools:viewBindingIgnore="true"` 属性添加到相应布局文件的**根视图**中：

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:viewBindingIgnore="true"
    tools:context=".MainActivity">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Hello World!"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

## 如何用

 XML 布局文件生成一个绑定类，类名是以xml布局文件名去掉下换线后，单词首字母大写加上Binding命名的。如activity_main.xml生成的类ActivityMainBinding.

### Activity中使用

请在 Activity 的 onCreate() 方法中执行以下步骤：

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    //关键代码
    binding = ActivityMainBinding.inflate(getLayoutInflater());
    View rootView = binding.getRoot();
    setContentView(rootView);
    //如何使用
    binding.textView.setText("这是修改的");
    binding.button.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d("Main","点击了按钮");
        }
    });

}
```

### Fragment 中使用

1. 请在 Fragment 的 onCreateView()方法中执行以下步骤：

```java
@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container,
                         Bundle savedInstanceState) {
    //关键代码,跟在activity中相比，多传了container和false参数
    binding = FragmentMyBinding.inflate(inflater, container, false);
    return binding.getRoot();
}
@Override
public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    //如何使用
    binding.textView.setText("这是Fragment");
    binding.button.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d("Fragment", "点击了按钮");
        }
    });
}
```

2. Fragment 对象的存在时间比其视图的生命周期长，请务必在 Fragment 的 onDestroyView() 方法中清除对绑定类实例的所有引用，以免发生内存泄漏：

```java
  @Override
  public void onDestroy() {
      super.onDestroy();
      binding = null;
  }
```

### 在 RecyclerView adapter 中使用

假设 `RecyclerView` item 的布局文件是row_payment.xml，

```kotlin
class PaymentAdapter(private val paymentList: List<PaymentBean>) : RecyclerView.Adapter<PaymentAdapter.PaymentHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentHolder {
        val itemBinding = RowPaymentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PaymentHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: PaymentHolder, position: Int) {
        val paymentBean: PaymentBean = paymentList[position]
        holder.bind(paymentBean)
    }

    override fun getItemCount(): Int = paymentList.size

    class PaymentHolder(private val itemBinding: RowPaymentBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(paymentBean: PaymentBean) {
            itemBinding.tvPaymentInvoiceNumber.text = paymentBean.invoiceNumber
            itemBinding.tvPaymentAmount.text = paymentBean.totalAmount
        }
    }
}
```

观察几种场合的使用，其实都一样，就是用自动生成的绑定类去inflate,得到绑定类对象和view对象；通过getRoot()得到根布局视图对象设置到容器里。

## 处理`include`、`merge`标签

首先要知道两个知识点。

第一个知识点： 每个自动生成的Binding类都有两种方法：`inflate`和`bind`,`inflate`方法里面执行`inflater.inflate()`,也就是把xml解析成View对象，而bind方法里面只是new Binding类，并调用findViewById把子view赋值给 Binding类 的成员。这些成员根据xml里的id生成，也就是一个view(比如button)，你声明了id属性，就会在对应的 Binding类 里面生成一个成员字段。

所以，1. 调用bind,一定是在对应view已经渲染出来之后，否则无法findViewById。

2. 每次调用bind，都会返回一个新的Binding类，但是里面的view对象都是一样的，都是xml渲染出来的那个view。由于这个Binding类只用来引用view，不存储其它数据，所以我们多次调用bind是没问题的(尽管没必要)
3. inflate方法里面会自动调用bind，返回绑定类对象，所以通常我们不需要关注bind方法

第二个知识点：`include`标签上面可以指定id，子布局的根视图上面也可以指定id。显然inflate出来后，只会存在一个id。所以规则是，优先使用`include`设置的id。inflate出来的view，实际id是include标签的id，但是如果include标签没有声明id，则view的id是子布局的根视图指定的id。通过Android Studio的`Layout Inspecor`工具实时抓取view结构，可以证明以上结论。不过如果搞不清楚，可以把两个id声明为一致。

生成的binding类在`build\generated\data_binding_base_class_source_out\debug\out\`下面，源码并不复杂，最好的方法就是去看这个源码，而不是看别人的教程，因为教程不管怎么啰嗦都不可能把所有点讲透。但不管如何，我这里要总结一下，作为我的学习成果。

了解这两点，可以自然而然推理出ViewBinding的使用方法：

- `<inlude>` 的子布局不包含 `<merge>`标签

  你的xml对应一个binding类，然后在此xml中include了另一个xml。子xml的根元素不是“merge”标签。

  这种情况，需要为`<include>` 分配一个 ID，子布局的根元素不需要id，因为inflate后，会用外面"include"的id代替“子布局的根元素”的id。使用该 ID 来访问包含布局中的视图

  ```xml
  //main_layout.xml
  <androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
      xmlns:app="http://schemas.android.com/apk/res-auto"
      android:layout_width="match_parent"
      android:layout_height="match_parent">
  
      <include
          android:id="@+id/appbar"
          layout="@layout/app_bar"
          app:layout_constraintTop_toTopOf="parent" />
      
  </androidx.constraintlayout.widget.ConstraintLayout>
  ```

  ```kotlin
  val binding: MainLayoutBinding = MainLayoutBinding.inflate(layoutInflater)
  setContentView(binding.root)
  //include标签的id是appbar
  setSupportActionBar(binding.appbar.toolbar)
  ```

  如果你非要给子布局的根元素指定id，也没关系，那么子布局的绑定类里面，就会有一个根据此id生成的字段，此字段引用的view对象，跟你在外面通过“include 标签的id”引用的对象是一样的。

- 如果被include的布局文件使用merge标签

  使用merge的目的是消除过多的嵌套，子布局里没有根视图了，根标签用merge代替，里面的view直接塞到外层布局的容器里。

  万变不离其宗，跟前面总思路是一样的，只是需要注意一下，

  1. `<include>`标签的ID是给子布局的根view 的，既然根标签变成了merge，也就没法应用这个id，这个id是无效的，但是生成绑定类的时候，工具是不知道的，依然会根据id生成成员字段，然后通过findViewById给字段赋值，显然这一步会报错，因为实际的view树上没有这个id。报错信息类似：

     ```shell
   Caused by: java.lang.NullPointerException: Missing required view with ID: com.hsae.ccs20.xxx:id/tab_indicatorInclude
             at com.hsae.ccs20.xxx.databinding.ActivityVideoBinding.bind(ActivityVideoBinding.java:71)
     ```
   所以，不要给include标签设置id。如果你的代码里这样写了，没有报错，只是因为运气好，子布局里刚好有一个同名的id，不代表你的代码没有问题。
  
  2. 由于include标签没有Id,故无法通过外层布局文件的Binding类引用到子布局里的view。需要调用子布局绑定类的bind方法来获取。
  
     ```xml
     //main.xml
     <include
         layout="@layout/tab_indicator"/>
     
     //tab_indicator.xml
     <merge xmlns:android="http://schemas.android.com/apk/res/android">
     </merge>
     
     //MainActivity.kotlin
     var bind = TabIndicatorBinding.bind(binding.root)//参数是子布局的parent view，只要在这个view上调用findViewById()能访问到子布局的view就行。
     ```
  
     如果你不想这么做，希望如前面一样通过外层xml的绑定类访问到子布局，也是有一些奇技淫巧的。你可以把include的id设置成“include”标签的parent的id。这样就会在绑定类里面生成对子布局绑定类的引用。但是“parent” view也有一个字段，显然名称会冲突，ViewBinding会自动重命名。所以不要这么做。
  
  3. getRoot()返回的是bind(binding.root)参数传进来的view对象。
  
  4. 当布局文件里使用merge时，对应绑定类的inflate方法只有一个方法：inflate(LayoutInfalte, parentView)。而不使用merge时，inflate方法有两个：
  
     inflate(LayoutInfalte)和inflate(LayoutInfalte, parentView，isAttachToParent).
  
   注意，这三个方法参数各不一样。
  
  5. 若还有其它疑难，请看源码，自然一目了然。

如果activity的布局文件直接使用了merge，那么这样写：

  ```kotlin
  var content : ViewGroup = findViewById(android.R.id.content)
  var binding:ActivityVideoBinding  = ActivityVideoBinding.inflate(layoutInflater, content)
  //请注意，不需要setContentView，inflate时已经自动attach了。
  ```

