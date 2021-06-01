1. LiveData是什么

   LiveData是一个抽象类，持有可被观察的数据。MutableLiveData是他的子类。例如：

   ```kotlin
   var myName: MutableLiveData<String> = MutableLiveData()
   ```

   原本myName是一个String，但是myName跟UI控件的显示是应该绑定的，怎么保证更新了myName一定通知到UI呢？于是用MutableLiveData<String>代替String，在LiveData上实现观察者模式。

   然后在UI里面：

   ```kotlin
   myName.observe(this,  Observer {
       str ->
       println("xxxxx $str")
   })
   myName.value = "ccc"
   ```

   其实就是一个观察者模式，当改变了持有的对象时，回调所有的Observer。

   看起来我们自己实现LiveData也不难。然而：

2. LiveData没有内存泄露，观察者和Lifecycle对象绑定，能在销毁时自动解除注册

3. LiveData只有当观察者的生命周期处于活跃状态时才会去通知观察者

   ```kotlin
   override fun onCreate(savedInstanceState: Bundle?) {
   .......
       myName.value = "aaa"
       myName.observe(this,  Observer {//此处this是一个activity
           str ->
           println("xxxxx $str")
       })
       myName.value = "bbb"
       myName.observe(this,  Observer{
           str:String ->
           println("yyyy $str")
       })
       myName.value = "ccc"
       myName.value = "ddd"
   }
   override fun onStart() {
       super.onStart()
       println("onStart")
       myName.value = "eee"
   }
   ```

   Log如下：

   2021-05-21 17:28:19.289 18975-18975/com.example.android.trackmysleepquality I/System.out: onStart
   2021-05-21 17:28:19.291 18975-18975/com.example.android.trackmysleepquality I/System.out: xxxxx eee
   2021-05-21 17:28:19.291 18975-18975/com.example.android.trackmysleepquality I/System.out: yyyy eee

   你会发现，1. 通知一定在onStart后面，activity如果不是活跃的，那么通知没有意义

                  2. onStart后，只收到最新的通知。
                     3. 支持多个observer

4. 必须通过setValue()和postValue()改变数据。比方说：

   ```kotlin
   var myList: MutableLiveData<List<Int>> = MutableLiveData()
   myList.observe(this,  Observer {
       list ->
       println("hahaha $list")
   })
   var list = mutableListOf<Int>()
   myList.value = list //会通知
   list.add(1)//不会通知
   list.add(2)
   ```

   MutableLiveData持有list对象，list内部自己改变，是不可能有通知的。这个应该好理解。

5. MediatorLiveData是`LiveData`的另一个子类，用于：

   有多个`LiveData`，想要同时监听这两个数据源，只要他们之中有一个数据源更新则接收到通知。

   相当于一个中介把不同的消息源合并起来。

   ```java
   //1.第一个数据源
   LiveData liveData1 = ...;
   //第二个数据源
   LiveData liveData2 = ...;
   
   //2.中间商
   MediatorLiveData liveDataMerger = new MediatorLiveData<>();
   liveDataMerger.addSource(liveData1, value -> liveDataMerger.setValue(value)//数据源1更新后通知中间商更新);
   liveDataMerger.addSource(liveData2, value -> liveDataMerger.setValue(value)//数据源2更新后通知中间商更新);
   
   //3.通过改变数据源1或者2的数据，中间商接收到更新了
   liveDataMerger.observe(this, new Observer<String>() {
       @Override
       public void onChanged(String s) {
           Log.i("hellokai", "mediatorLiveData, onChanged:" + s);
       }
   });
   ```

   如果数据源变化频繁，我们想要只检测前10个数据变化，之后取消数据观察，我们可以用中间商这么做：

```java
liveDataMerger.addSource(liveData1, new Observer() {
    private int count = 1;
    
    @Override public void onChanged(@Nullable Integer s) {
      count++;
      //1.当数据源1更新后，通知中间商进行更新
      liveDataMerger.setValue(s);
      //2.满足条件后，我们进行取消
      if (count > 10) {
          liveDataMerger.removeSource(liveData1);
      }
    }
});
```

