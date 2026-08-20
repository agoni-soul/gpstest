package com.haha.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * @auther: haha
 * @Date:   2026/7/9
 * @Detail: 使用 [suspend] 关键字让 [DAO] 方法支持协程，也可以让方法返回 [Flow] 或 [androidx.lifecycle.LiveData] 以实现响应式更新
 */
@Dao
interface UserDao {
    /**
     * 插入数据，冲突时替换旧数据
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    /**
     * 更新数据
     */
    @Update
    suspend fun updateUser(user: User)

    /**
     * 删除数据
     */
    @Delete
    suspend fun deleteUser(user: User)

    /**
     * 查询所有用户，按名字排序，返回 Flow 以观察数据变化
     */
    @Query("SELECT * FROM users ORDER BY user_name ASC")
    fun getAllUsers(): Flow<List<User>>

    /**
     * 带参数的查询
     */
    @Query("SELECT * FROM users WHERE age > :minAge")
    suspend fun getUsersOlderThan(minAge: Int): List<User>
}