# 组件化 ServiceLoader 笔记索引

记录时间：2026-09-06  
包目录：`docs/service-loader/`

本包汇总三次问答，各成一篇，图与源文件同目录。

| 对应问题  | 文档                                                            | 内容                                                         |
|-------|---------------------------------------------------------------|------------------------------------------------------------|
| 第 1 问 | [getService 底层实现](service-loader-getservice-impl.md)          | `@IServiceLoader` → APT → `ServiceLoaderHelper.getService` |
| 第 2 问 | [JDK SPI 与 IServiceLoader](service-loader-jdk-vs-iservice.md) | 定义、作用、区别                                                   |
| 第 4 问 | [原框架与改动方案对照](service-loader-old-vs-new.md)                    | 优化后设计与原来的逐项差异                                              |

第 1、2 问按**当前仓库实现**写（固定类名 `ServiceInit_`、魔法默认 key）。  
第 4 问记录后续改动方案：模块化 Init、插件聚合、`ServiceRecord` 去重。
