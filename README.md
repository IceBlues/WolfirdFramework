# Wolfird - 简洁规范的玩法开发框架
![banner](https://img1.imgtp.com/2023/05/20/ufE0q6Q8.png)
![开源协议](https://img.shields.io/github/license/WolfLink-DevTeam/WolfirdFramework?style=for-the-badge)
![Stars](https://img.shields.io/github/stars/WolfLink-DevTeam/WolfirdFramework?style=for-the-badge)
![Last Commit](https://img.shields.io/github/last-commit/WolfLink-DevTeam/WolfirdFramework?style=for-the-badge)
[![Star History Chart](https://api.star-history.com/svg?repos=WolfLink-DevTeam/WolfirdFramework&type=Date)](https://star-history.com/#WolfLink-DevTeam/WolfirdFramework&Date)
### 简介
&emsp;&emsp;这是一个面向快速上手、敏捷开发的 Minecraft Bukkit 插件玩法开发框架，使用了 MongoDB 与 IOC，对Bukkit提供的一些原生功能进行了二次封装，以便更快地进行子插件的开发。
### 主类
&emsp;&emsp;子插件主类不再直接继承 JavaPlugin，而是从 AddonPlugin、ModePlugin、SystemPlugin 中选择一个继承。
AddonPlugin 描述的是一个小功能拓展插件；ModePlugin 描述的是一个游戏模式拓展插件，例如 UHC 模式、猎人游戏模式等；SystemPlugin 描述逇一个大型玩法系统拓展插件。
```java
// 这是一个示例模块插件的主类
public final class WolfirdTestAddon1 extends AddonPlugin {
	// 具体方法...
}
```
### 事件监听器
&emsp;&emsp;不再需要直接实现 Listener 接口，而是继承 WolfirdListener 抽象类。
WolfirdListener 提供了比 Bukkit 封装的 Listener 更加方便的注册和取消注册的方式，只需要通过 setEnabled 改变其状态即可，另外事件监听方法仍然需要使用 @EventHandler 进行标注。
如果需要在监听器中使用调度器执行延迟任务或周期任务等，请不要使用 Bukkit.getScheduler().runTask() 等方法，而是使用 WolfirdListener 内封装的相关方法。
```java
// 这是一个示例事件监听器
public class TestListener extends WolfirdListener {
    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        System.out.println(e.getPlayer().getName());
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        System.out.println(e.getBlock().getType().name());
    }
}

class Test {
    // 示例
    void test() {
        // 启用监听器
        wolfirdListener.setEnabled(true);
        // 禁用监听器
        wolfirdListener.setEnabled(false);
    }
}
```
### 指令
&emsp;&emsp;只需要实现 WolfirdCommand 抽象类，之后调用插件主类(AddonPlugin、ModePlugin、SystemPlugin)的registerCommand()方法即可注册，不需要再自己实现setExecutor、编写配置文件之类的。
```java
public class CmdTest extends WolfirdCommand {
    public CmdTest() {
        // 是否需要权限 | 是否允许控制台身份执行 | 是否允许玩家身份执行 | 指令模板 | 指令帮助信息
        super(false, true, true, "wolfird test {1} abab {www2} {abab3}","带有接收参数的示例指令");
        // 指令模板会从左到右自动解析，随便那个 { } 括号里面写什么都可以，只是占位符罢了，但是{ }括号不能丢
    }
    @Override
    protected void execute(CommandSender sender, String[] args) {
        System.out.println(args[0]); // 在模板中对应的 {1} 的位置
        System.out.println(args[1]); // 在模板中对应的 {www2} 的位置
        System.out.println(args[2]); // 在模板中对应的 {abab3} 的位置
    }
}
class Test {
    // 示例
    void test() {
        CmdTest test = new CmdTest();
        // 注册指令
        MyPlugin.getInstance().registerCommand(test);
        // 取消注册指令
        MyPlugin.getInstance().unregisterCommand(test);
    }
}

```
### 配置文件
&emsp;&emsp;主要的配置文件都是存放在 MongoDB 中的，开发者需要创建子插件独立的配置文件时只需要继承 BaseConfig 类并创建单例，之后需要给定默认配置清单，包括配置路径、配置值，然后就可以调用封装好的 load() update() save() 方法便捷完成配置文件的修改和持久化操作。

```java
public class FrameworkConfig extends BaseConfig {
    public FrameworkConfig() {
        super("wolfird_config");
    }
}
class Test {
    void test() {
        FrameworkConfig config = new FrameworkConfig();
        /*
         从数据库中加载数据，
         一般是在加载插件时调用该方法
         */
        config.load();
        // 获取运行时配置
        config.get(ConfigProjection.MONGO_URL);
        // 修改运行时配置，Mongo数据库中的数据不受影响
        config.update(ConfigProjection.MONGO_DB_NAME,"abab");
        /*
          一般是在卸载插件时才需要调用该方法
          将运行时配置保存到数据库中
         */
        config.save();
    }
}
```
### 数据库
&emsp;&emsp;数据库部分主要指的是POJO映射，框架封装了一个 EntityRepository<E> 类，泛型类型E作为其实体类。该实体类必须有以下特点：

1. 由 @Data 注解标注，或手动实现其所有的get、set、hash等基本数据方法
2. 由 @MongoTable 注解标注，该注解需要指定一个值作为该实体类对应的数据库表单名称
3. 实体类中有且只能有唯一元素被@PrimaryKey标注，该元素将被作为主键并创建索引，值唯一
4. 实体类中只能有基本数据类型，如果涉及到复杂类型会报错

&emsp;&emsp;使用时需要自行创建 EntityRepository<E> 对象。

```java
// 定义表单名称
@MongoTable(name = "test_entity_table")
class Cat {
    // 定义主键
    @PrimaryKey
    String name;
    int age;
    String color;
}

class Test {
    // 示例
    void test() {
        // 定义实体仓库
        EntityRepository<Cat> repo = new EntityRepository<>(Cat.class);
        // 从数据库中根据主键查询实例
        Cat cat = repo.findByPrimaryKey("小白");
        // 向数据库中插入实例数据
        repo.insert(new Cat("小黑",1,"黑色"));
    }
}
```
### 日志
&emsp;&emsp;提供了便捷的 SubPluginNotifier 封装，继承自 BaseNotifier，前缀名称通过 plugin.yml 中的 prefix 配置项进行设置，可以带有颜色，如 §9Wolfird。通过子插件主类的 notifier 成员变量获取消息通知器。

```java
class Test {
    void test() {
        // 可以通过子插件主类获取，不建议手动创建
        SubPluginNotifier notifier = new SubPluginNotifier("子插件名称");
        // 更多方法建议查看源码
        notifier.info("这是一条普通信息");
        notifier.warn("这是一条警告信息");
        notifier.error("这是一条错误信息");
    }
}
```