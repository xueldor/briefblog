## 目的

替代findViewById

## 方案

会为该模块中的每个xml文件生成一个绑定类，绑定类的实例包含这个‘布局xml’具有 ID 的所有视图的直接引用。

## 如何启用

```groovy
android {
        ...
        viewBinding {
            enabled = true
        }
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

- `<inlude>` 的子布局不包含 `<merge>`标签

  需要为`<include>` 分配一个 ID，使用该 ID 来访问包含布局中的视图

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

- 如果被include的布局文件使用merge标签

  使用merge的目的是消除过多的嵌套，子布局里没有根视图了。

  其实跟前面是一样的：`binding.appbar.toolbar`。这里只是想说一下，

  1. 当布局文件里使用merge时，没有必要使用getRoot()。实际上getRoot()方法返回的可能是外层的一个view，也可能是本身布局的root。

  2. 当布局文件里使用merge时，对应绑定类的inflate方法只有一个方法：inflate(LayoutInfalte, parentView)。而不使用merge时，inflate方法有两个：

     inflate(LayoutInfalte)和inflate(LayoutInfalte, parentView，isAttachToParent).

     注意，这三个方法参数各不一样。

  如果activity的布局文件直接使用了merge，那么这样写：

  ```kotlin
  var content : ViewGroup = findViewById(android.R.id.content)
  var binding:ActivityVideoBinding  = ActivityVideoBinding.inflate(layoutInflater, content)
  //请注意，不需要setContentView，inflate时已经自动attach了。
  ```

  因为子布局也是一个独立的xml布局文件，也会生成绑定类，假设子布局叫tab_indicator.xml, 这时如果你执行：

  ```kotlin
  var bind2 = TabIndicatorBinding.bind(binding.root)
  println(binding.tabIndicator == bind)
  println(bind.btnGap === binding.tabIndicator.btnGap)
  打印：
  System.out: false
  System.out: true
  ```

也就是说，这两个bind对象不是一个对象，但是通过bind对象获取到的view是一样的。有点奇怪。