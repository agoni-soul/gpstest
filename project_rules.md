# 项目开发规范（Android）

## 添加flutter module配置

- 版本号：
    - flutter sdk: 3.38.5
    - dart: 3.10.4
    - flutter plugin: 88.2.0
    - gradle-wrapper: 8.13-all
    - kotlin: 1.8.10
    - gradle tools: 8.1.1
    - jdk: 17

## 技术栈约束

- 语言：仅使用Kotlin（禁止Java）；
- UI框架：必须使用Jetpack Compose（版本1.4.3），禁止XML布局；
- 架构：采用MVVM + 分层架构（data/domain/ui）；
- 网络：使用Retrofit 2.9.0 + OkHttp 4.10.0，配合Coroutines和Flow；
- 本地存储：使用Room 2.5.2，禁止直接操作SQLite；
- 依赖注入：使用Hilt 2.44；
- 其他：必须使用AndroidX组件，禁止support库。

## 代码风格

- 命名：
    - 类名：UpperCamelCase（如LoginScreen、UserRepository）；
    - 函数/变量：lowerCamelCase（如getUserInfo、userName）；
    - 常量：UPPER_SNAKE_CASE（如MAX_RETRY_COUNT = 3）；
    - 资源：前缀+功能（如string/login_title、compose/LoginScreen.kt）；
- 注释：
    - 类和公共函数必须添加KDoc注释（包含功能、参数、返回值）；
    - 复杂逻辑需添加行内注释（// 处理网络错误重试）；
- 格式：
    - 缩进4空格，每行不超过120字符；
    - 函数体不超过30行，超过需拆分；
    - 优先使用Kotlin特性（如空安全、扩展函数、密封类）。

## 项目结构

- 包名根目录：com.example.myapp
- 模块划分：
    - app（主模块）
    - core（核心库：Base类、工具类）
    - feature：按功能拆分（feature:login、feature:home）
- 分层结构（以feature:login为例）：
    - ui：Compose界面（LoginScreen.kt）、ViewModel（LoginViewModel.kt）
    - domain：用例（LoginUseCase.kt）、实体（LoginUser.kt）
    - data：
        - remote：API接口（LoginService.kt）
        - local：本地存储（LoginDao.kt）
        - repository：实现（LoginRepositoryImpl.kt）
- 资源存放：
    - 字符串：res/values/strings_login.xml（按功能拆分）
    - 图片：res/drawable/login_*.png

## 上下文关联

- 所有ViewModel必须继承core.viewmodel.BaseViewModel（含loading、error状态管理）；
- 网络请求必须通过core.network.ApiResponse处理（统一封装成功/失败/异常）；
- Compose组件必须使用core.theme.MyAppTheme包裹，遵循主题规范。