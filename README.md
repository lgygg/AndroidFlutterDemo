# AndroidFlutterDemo
Flutter as module base Gradle version 7.4 and Android Gradle Plugins version 7.3

# 1.项目结构

Test(project)
  --app(module)
  --my_flutter(module)
  --crash(module)
# 2.配置project下的setting.gradle

## 1)加上如下代码
```
// 加入如下代码
setBinding(new Binding([gradle: this]))
evaluate(new File(settingsDir.parent, "/Test/my_flutter/.android/include_flutter.groovy"))//my_flutter是flutter模块项目名称

include ':my_flutter'
```

## 2)FAIL_ON_PROJECT_REPOS改为PREFER_PROJECT

如下：

```
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
    }
}
```

# 3.在app的build.gradle依赖flutter

注意，虽然模块名是my_flutter,但是依赖的名字要写成flutter

```
implementation project(':flutter')
```


