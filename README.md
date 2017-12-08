# ValueKit
使用 Kotlin 属性委托实现的基于文件的 Key-Value 持久化工具

## Usage

添加依赖
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compile 'com.github.twiceyuan:ValueKit:master-SNAPSHOT'
}
```

利用 Kotlin 的属性委托语法，可以使用更简单的配置来使用类似 SharedPreferences 的功能：

初始化（建议在 Application#onCreate）：

```kotlin
ValueKit.init(this)
```

定义配置文件：

```kotlin
// 注解可以指定该配置文件的名称，不指定会使用默认配置目录
@ValueDir("config") 
object Config {

    // 启动次数
    var launchCount by IntegerValue

    // 用户名
    var username by StringValue
    
    // 存储一个个人信息
    var person by ObjectValue<Person>()
}

// Person 定义
data class Person(
        val name: String,
        val email: String,
        val accessTime: MutableList<Long> = ArrayList()
) : Serializable
```

使用单例对象读写数据：
```kotlin
// 缓存数据
Config.username = "twiceYuan"

// 读取数据
tv_name.text = Config.username

// 读取对象信息
Config.person?.let {
    it.accessTime.add(System.currentTimeMillis())

    // 更新需要重新赋值
    Config.person = it
}
```

## 支持类型

类型名        | 委托对象
-------------|--------
Boolean      | BooleanValue
Integer      | IntegerValue
String       | StringValue
Long         | LongValue
Double       | DoubleValue
Serializable | ObjectValue<T: Serializable>

