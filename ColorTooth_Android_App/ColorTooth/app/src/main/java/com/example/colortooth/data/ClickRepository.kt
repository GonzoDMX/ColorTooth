package com.example.colortooth.data

import androidx.annotation.WorkerThread
import com.example.colortooth.data.ClickDao
import com.example.colortooth.data.ClickData
import kotlinx.coroutines.flow.Flow

class ClickRepository(private val clickDao: ClickDao) {

    // Flow ensures queries to database are performed on a different thread than main/UI
    val allClicks: Flow<List<ClickData>> = clickDao.getDescendingByStep()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    // suspend tells the compiler this must be run from a coroutine
    suspend fun insert(entry: ClickData) {
        clickDao.insert(entry)
    }

    @WorkerThread
    // suspend tells the compiler this must be run from a coroutine
    suspend fun delete() {
        clickDao.deleteAll()
    }

    @WorkerThread
    // suspend tells the compiler this must be run from a coroutine
    suspend fun count(): Int {
        return clickDao.countDB()
    }
}