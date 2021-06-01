Room是Jetpack里提供的“数据库ORM(对象关系映射)框架“

假设你需要一张表，名字叫SleepNight。你要创建三个类：SleepDatabase、SleepDatabaseDao、SleepNight。

SleepDatabase 负责创建表、更新表结构

SleepDatabaseDao 负责执行增删改查

SleepNight 是一个bean，映射到table

1. 实体映射类SleepNight 

   ```kotlin
   // SleepNight类映射到表daily_sleep_quality_table
   @Entity(tableName = "daily_sleep_quality_table")
   data class SleepNight(
           // 主键
           @PrimaryKey(autoGenerate = true)
           var nightId: Long = 0L,
       
           // 字段映射
           @ColumnInfo(name="start_time_milli")
           val startTimeMilli: Long = System.currentTimeMillis(),
           @ColumnInfo(name="end_time_milli")
           var endTimeMilli: Long = startTimeMilli,
           @ColumnInfo(name = "quality_rating")
           var sleepQuality: Int = -1
   )
   ```

   这里用到了三个注解@Entity   、 @PrimaryKey  、  @ColumnInfo

2.  DAO

   SleepDatabaseDao里面执行SQL操作。

   ```kotlin
   @Dao
   interface SleepDatabaseDao {
   
       @Insert
       fun insert(night: SleepNight)
   
       @Update
       fun update(night: SleepNight)
   
       @Query("SELECT * from daily_sleep_quality_table WHERE nightId = :key")
       fun get(key: Long):SleepNight?
   
       @Query("DELETE FROM daily_sleep_quality_table")
       fun clear()
   
       @Query("SELECT * FROM DAILY_SLEEP_QUALITY_TABLE ORDER BY nightId desc limit 1")
       fun getTonight():SleepNight?
   
       @Query("SELECT * FROM DAILY_SLEEP_QUALITY_TABLE ORDER BY nightId DESC")
       fun getAllNights():LiveData<List<SleepNight>>
   }
   ```

   你会发现SleepDatabaseDao是一个接口，非常简洁，因为框架全帮我们做好了。

   1） 接口名上添加@Dao注解

   2） @Insert 、 @Update 分别对应插入和修改。其中 update 操作是根据主键匹配的。

   3） @Query表示查询，注解的参数是SQL语句。

   4）@Query注解不仅仅用来执行select， 实际上可以执行任何SQL语句。例子中的clear()方法用来清除整张表，我们用的就是@Query：

   ```
   @Query("DELETE FROM daily_sleep_quality_table")
   ```

   5）清除整张表为什么不用@Delete注解？因为@Delete需要参数指定删除哪一条数据：

   ```kotlin
   @Delete
   fun delete(night: SleepNight);
   ```

   6) 你需要注意一些SQLite自己的坑，比如：

   ```kotlin
   @Query("UPDATE items SET place = :new_place WHERE id IN (:ids)")
   fun updateItemPlaces(ids:List<Int>, new_place:String)
   ```

   当ids包含的id太多（超过999）时，SQLite 会抛异常：SQLiteException too many SQL variables (Sqlite code 1)

   7） 例子的最后一个方法返回的LiveData。LiveData的知识见上一篇。`Room`能保证`LiveData`是最新的，也就是说你只要显式查询一次，后面通过observer接收更新。

3. SleepDatabase

   1） SleepDatabase 定义成抽象类，并且要继承RoomDatabase：

   ```kotlin
   @Database(entities = [SleepNight::class],version = 1,exportSchema = false)
   abstract class SleepDatabase: RoomDatabase(){
   ```

   为什么定义成抽象类？因为room自动帮我们实现，不需要自己写实现代码。那为什么不定义成接口？因为还是有一些代码的（废话）

   entities指定数据库里包含哪些表；version 指定数据库版本，这个版本只能往上增。如果你修改了表的结构，version就要加一，否则table不会重建。学过安卓原生SQLiteDatabase的同学应该都能理解。 exportSchema很少用，指定为false即可。

   （schema 中文叫模式, 是数据库专业里的概念。模式中包含了schema对象，可以是**表**(table)、**列**(column)、**数据类型**(data type)、**视图**(view)、**存储过程**(stored procedures)、**关系**(relationships)、**主键**(primary key)、**外键(**foreign key)等，以及它们相互之间的关系，可以用一个可视化的图来表示）

   2） 用kotlin的伴生对象实现SleepDatabase的单例模式

   ```kotlin
   companion object{
       @Volatile
       private var INSTANCE:SleepDatabase?=null
   
       fun getInstance(context: Context):SleepDatabase{
           synchronized(this){
               var instance = INSTANCE
               if(instance == null){
                   instance = Room.databaseBuilder(
                           context.applicationContext,
                           SleepDatabase::class.java,
                           "sleep_history_database")
                           .fallbackToDestructiveMigration()//当表结构更新时的策略，此处为丢弃旧表重建，也就是原来的数据会丢失
                           .build()
                   INSTANCE = instance
               }
               return instance
           }
       }
   }
   ```

   fallbackToDestructiveMigration是指定表结构升级的策略。通常要把数据从旧表迁移到新的表。这里为了简单直接舍弃旧表。

   4) 给 SleepDatabase 声明dao对象：

   ```kotlin
   abstract class SleepDatabase: RoomDatabase(){
       abstract val sleepDatabaseDao:SleepDatabaseDao
   ```

     第一，可以声明多个dao。

    第二，声明为抽象的，因为room会自动帮我们实例化

4. 单元测试怎么写？

   建一个AS的工程，假设你已经知道androidTest和test目录的区别。我们在androidTest里面测试前面的代码。

   @RunWith(AndroidJUnit4::class) 、@Before 、 @After 、@Test这些属于单元测试的基本常识，不多说。

   在@Before里面初始化：

   ```
   @Before
   fun createDb() {
       val context = InstrumentationRegistry.getInstrumentation().targetContext
       db = Room.inMemoryDatabaseBuilder(context, SleepDatabase::class.java)
               // Allowing main thread queries, just for testing.
               .allowMainThreadQueries()
               .build()
       sleepDao = db.sleepDatabaseDao
   }
   ```

​       inMemoryDatabaseBuilder设置数据库是在内存中的，这样一旦单元测试结束，数据库立马销毁；

​       当“内存中的”数据库时，可以在主线程上面调用数据库操作：allowMainThreadQueries

​      在@After里面，关闭数据库：

```kotlin
@After
@Throws(IOException::class)
fun closeDb() {
    db.close()
}
```