package com.example.colortooth.data


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface ClickDao {

    // Returns a list of all entries in database, ordered by step value, ascending
    @Query("SELECT * FROM click_table ORDER BY step ASC")
    fun getAscendingByStepDown(): Flow<List<ClickData>>

    // Returns a list of all entries in database, ordered by step value, descending
    @Query("SELECT * FROM click_table ORDER BY step DESC")
    fun getDescendingByStep(): Flow<List<ClickData>>

    // Returns a list of all entries in database, ordered by time values
    @Query("SELECT * FROM click_table ORDER BY time ASC")
    fun getOrderedByTime(): List<ClickData>

    // Returns a list of all entries that match the input color name
    @Query("SELECT * FROM click_table WHERE name = :name")
    fun findByColorName(name: String): List<ClickData>

    // Returns a list of all entries that match the input clear value
    @Query("SELECT * FROM click_table WHERE alpha_val = :value")
    fun findByClearValue(value: Int): List<ClickData>

    // Returns a list of all entries that match the input red value
    @Query("SELECT * FROM click_table WHERE red_val = :value")
    fun findByRedValue(value: Int): List<ClickData>

    // Returns a list of all entries that match the input green value
    @Query("SELECT * FROM click_table WHERE green_val = :value")
    fun findByGreenValue(value: Int): List<ClickData>

    // Returns a list of all entries that match the input green value
    @Query("SELECT * FROM click_table WHERE blue_val = :value")
    fun findByBlueValue(value: Int): List<ClickData>

    // Conflict strategy will replace old value with new value if there is a conflict
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ClickData)

    // Clears all entries from the table
    @Query("DELETE FROM click_table")
    suspend fun deleteAll()

    @Query("SELECT COUNT(step) FROM click_table")
    suspend fun countDB(): Int
}