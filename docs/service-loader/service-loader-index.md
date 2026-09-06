# 组件化 ServiceLoader 笔记索引

记录时间：2026-09-06  
包目录：`docs/service-loader/`

本包以**当前仓库已落地的优化实现**为准。主文档含整体流程与优化前后对比；其余篇保留问答底稿。

| 文档                                                            | 内容                                          |
|---------------------------------------------------------------|---------------------------------------------|
| [优化后设计与流程](service-loader-optimized-design.md)                | 编译 / 打包 / 运行三期、`getService` 逐步拆解、优化前后对比与流程图 |
| [getService 底层实现](service-loader-getservice-impl.md)          | 优化前实现备忘（固定 `ServiceInit_`、魔法默认 key）         |
| [JDK SPI 与 IServiceLoader](service-loader-jdk-vs-iservice.md) | 定义、作用、区别                                    |
| [原框架与改动方案对照](service-loader-old-vs-new.md)                    | 条目对照底稿；方案已落地，图与总结见主文档                       |

主文档入口：`ServiceLoaderHelper.getService(IUserService::class.java)`。  
示例：`@IServiceLoader(interfaces = [IUserService::class], defaultImpl = true)`。
