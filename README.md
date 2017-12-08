# ValueKit
使用 Kotlin 属性委托实现的基于文件的 Key-Value 持久化工具

## Usage

利用 Kotlin 的属性委托语法，可以使用更简单的配置来使用类似 SharedPreferences 的功能：

定义配置文件：

```kotlin
// 注解可以指定该配置文件的名称，不指定会使用默认配置目录
@ValueDir("config") 
object Config {

    // 启动次数
    var launchCount by IntegerValue

    // 用户名
    var username by StringValue
}
```

使用单例对象读写数据：
```kotlin
// 缓存数据
Config.username = "twiceYuan"

// 读取数据
tv_name.text = Config.username
```
