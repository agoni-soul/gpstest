package com.soul.room

import android.content.Context
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * @auther: haha
 * @Date:   2026/7/9
 * @Detail:
 */
class RoomTest {
    fun test(context: Context) {
        val db = AppDatabase.getInstance(context).userDao()
        GlobalScope.launch {
            val user = User(name = "soul", email = "123456", age = 18, extraContent = "")
            val user1 = User(name = "haha", email = "123456", age = 17, extraContent = "")
            val user2 = User(name = "hehe", email = "123456", age = 19, extraContent = "")
            db.insertUser(user)
            db.insertUser(user1)
            db.insertUser(user2)
            val users = db.getAllUsers().first()
            println(users)
        }
    }
}

