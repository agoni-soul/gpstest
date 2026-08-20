package com.haha.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * @auther: haha
 * @Date:   2026/7/9
 * @Detail:
 */
@Database(entities = [User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    /**
     * 提供获取[androidx.room.Dao]实例的抽象方法
     */
    abstract fun userDao(): UserDao

    companion object {
        /**
         * 1. 使用单例模式防止多次创建数据库实例
         */
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            // 2. 使用 Elvis 运算符 ?: 替代 if-null 检查
            // 如果 INSTANCE 不为空，直接返回它（带智能类型转换，无需 !!）
            // 如果为空，执行后面的代码块
            return INSTANCE ?: synchronized(this) { // 3. 使用专门的锁对象，避免锁整个 Class
                // 4. 在同步块内部再次使用 ?: 检查，并赋值
                INSTANCE ?: Room.databaseBuilder( // 显式指定泛型避开推断坑
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build().also { INSTANCE = it } // 5. 使用 also 在构建完成后赋值给 INSTANCE
            }
        }
    }
}