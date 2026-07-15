package com.soul.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * @auther: haha
 * @Date:   2026/7/9
 * @Detail: 创建一个数据类，并用 [@Entity] 注解，[androidx.room.Room] 会为其在数据库中创建对应的表
 */
@Entity(tableName = "users")
class User(
    // 主键，autoGenerate = true 表示自动生成
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // ColumnInfo 可自定义列名
    @ColumnInfo(name = "user_name")
    val name: String,

    val email: String,

    val age: Int,

    // 注解可用于标记不需要持久化的字段
    @Ignore
    val extraContent: String
)
