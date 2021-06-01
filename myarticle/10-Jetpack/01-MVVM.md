1. build.gradle中引入lifecycle

   ```groovy
   dependencies {
       implementation 'androidx.lifecycle:lifecycle-extensions:2.0.0'
   }
   ```

2. 继承ViewModel类

   ```kotlin
   class GameViewModel : ViewModel() {
      init {
          Log.i("GameViewModel", "GameViewModel created!")
      }
   }
   ```

   ViewModel这个类在lifecycle包里。

3. ViewModel在fragment detached或activity finish的时候销毁，销毁时调用onCleared

   ```kotlin
   override fun onCleared() {
      super.onCleared()
      Log.i("GameViewModel", "GameViewModel destroyed!")
      //释放资源
   }
   ```

   onCleared继承自ViewModel类。

   我们知道fragment是依附于activity的。假设我先移除fragment，过一会再finish activity，那么到底是activity finish时执行onCleared呢？还是移除fragment时执行呢？往下看

4. UI controllers指的是Activity或者fragment，在里面引用ViewModel（后面简称VM）。

   **重要：**用 `ViewModelProvider`创建ViewModel,而不是直接new对象。因为：

   1. fragment经常重新创建（比如旋转屏幕），而ViewModel不要重建。如果直接new，那么每次fragment重建时，ViewModel会跟着重建，ViewModel里面保存的数据都要重新赋值。

   2. ViewModelProvider管理ViewModel的作用域（activity或fragment），在UI controllers的生命周期的某个阶段去调用ViewModel里的方法（比如前面说的onCleared）

      ```kotlin
      Log.i("GameFragment", "Called ViewModelProviders.of")
      viewModel = ViewModelProviders.of(this).get(GameViewModel::class.java)
      //of(this) this是GameFragment。此VM的scope是GameFragment，于是GameFragment退出时自动调用onCleared释放资源
      //过去使用MVP模式的时候，我们需要手动调用P的释放资源的方法，容易忘记。现在不需要了。
      
      //新一点版本不用ViewModelProviders：
      viewModel = ViewModelProvider(this, viewModelFactory).get(GameViewModel::class.java)
      ```

   当这个viewModel所依附的fragment或activity销毁时，viewModel也随之销毁（并执行onCleared）。重新进入fragment或activity时，viewModel也重新创建新的对象。但是旋转、配置改变等动作，只重新创建fragment或activity，viewModel对象还是原来的。

5. 不要在VM里面引用fragment或activity。因为fragment 经常重新创建，而VM对象更持久。过去解决旋转屏幕问题，我们使用[`onSaveInstanceState()`](https://developer.android.com/guide/components/activities/activity-lifecycle#save-simple-lightweight-ui-state-using-onsaveinstancestate) 保存数据，现在不需要了因为数据放在VM里面。

6. UI Controller中可以有逻辑代码，但仅限于处理UI的逻辑，不应该包含数据。常常有两种情况1） 界面交互导致数据改变；2）数据发生改变需要同步UI

   1）刷新界面的代码仍然在fragment里面，但是更新数据的代码全部到了ViewModel里。有时你要写类似这样的代码：

   ```kotlin
   //fragment里面
   button.setOnClickListener { doIt() }
   
   private fun doIt() {
   	viewModel.doIt()//更新viewModel里的数据
   	updateUI()//同时更新界面。保证数据和界面显示一致
   }
   private fun updateUI() {
       binding.wordText.text = viewModel.word
   }
   //viewModel里面
   fun doIt() {
       word--
   }
   ```

   你看，VM里面不会引用fragment

7. 默认通过ViewModelProviders创建ViewModel，ViewModel构造函数是没有参数的。如果ViewModel的构造函数需要参数，则需要继承ViewModelProvider.Factory：

   ```kotlin
   class ScoreViewModelFactory(private val finalScore:Int): ViewModelProvider.Factory {
       override fun <T : ViewModel?> create(modelClass: Class<T>): T {
           if(modelClass.isAssignableFrom(ScoreViewModel::class.java)){
               return ScoreViewModel(finalScore) as T
           }
           throw IllegalArgumentException("Unknown ViewModel class")
       }
   }
   ```

   这段代码作为样板，直接拷到你的代码里即可

8. 关于ViewModel有几个实例的问题

   ```kotlin
   viewModel = ViewModelProviders.of(this, viewModelFactory).get(ScoreViewModel::class.java)
   println("ScoreViewModel: $viewModel")
   viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(ScoreViewModel::class.java)
   println("ScoreViewModel: $viewModel")
   viewModel = ViewModelProvider(this, viewModelFactory).get(ScoreViewModel::class.java)
   println("ScoreViewModel: $viewModel")
   ```

   ```shell
   2021-05-31 15:30:18.630 5910-5910/com.example.android.guesstheword I/System.out: ScoreViewModel: com.example.android.guesstheword.screens.score.ScoreViewModel@59fb5d1
   
   2021-05-31 15:30:18.632 5910-5910/com.example.android.guesstheword I/System.out: ScoreViewModel: com.example.android.guesstheword.screens.score.ScoreViewModel@5ff6337
   
   2021-05-31 15:30:18.633 5910-5910/com.example.android.guesstheword I/System.out: ScoreViewModel: com.example.android.guesstheword.screens.score.ScoreViewModel@59fb5d1
   ```

   通过打印的日志中，ScoreViewModel的hashcode，我们知道当ViewModelProvider的第一个参数（ViewModelStoreOwner）传进去的是同一个activity或fragment时，返回的viewModel对象是同一个；而当ViewModelStoreOwner不同时，返回的是一个新的viewModel对象。

   **启示：**假如一个activity下面有多个fragment，需要这些的fragment和activity共享一个viewModel，那么在fragment里获取viewModel时，ViewModelProvider第一个参数传入activity，则获取的都是activity的viewModel，里面的数据可共享。不过更标准的方法是使用委托 `by activityViewModels`：

   ```kotlin
   val shareModel: ScoreViewModel by activityViewModels{
       ScoreViewModelFactory(ScoreFragmentArgs.fromBundle(requireArguments()).score)
   }
   println("ScoreViewModel : $shareModel")
   ```

   需要factory时才传参数。不需要时，直接`val shareModel: ScoreViewModel by activityViewModels`

   **另:** 一定是val shareModel 而不是var shareModel,  因为'Lazy<ScoreViewModel>'未提供setValue,只读。

9. 小结

   | **UI controller**                               | **ViewModel**                                                |
   | ----------------------------------------------- | ------------------------------------------------------------ |
   | UI controller 通常是fragment。命名为XXXFragment | ViewModel命名为XXXViewModel                                  |
   | 不包含任何需要界面展示的数据                    | 包含界面展示的数据.                                          |
   | 包含展示数据、用户事件的代码                    | 包含处理数据的代码                                           |
   | 当配置改变时，销毁重建                          | 只有当关联的UI组件移除时才会销毁。activity是finish的时候；fragment是detached的时候。<br />当关联的UI组件是同一个时，ViewModelProvider返回的是同一个viewModel;否则是不同的对象。 |
   | 包含views.                                      | 永远不要引用activity、fragment、view                         |
   | 引用`ViewModel`.                                | 一个VM对象跟一个UI controller关联，但是不直接引用 UI controller. |

10. 深度好文，告诉你为什么要使用MVVM模式https://juejin.cn/post/6955491901265051661

11. ViewModel 四种集成方式https://zhuanlan.zhihu.com/p/143346337