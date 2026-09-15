package com.it10x.foodappgstav7_24.data.pos.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.it10x.foodappgstav7_24.data.pos.entities.VirtualTableCounterEntity

@Dao
interface VirtualTableCounterDao {



    @Query("""
    UPDATE virtual_table_counters
    SET lastNumber = 0,
        resetDate = :date
""")
    suspend fun resetCounters(
        date: String
    )

    @Query("""
        SELECT * 
        FROM virtual_table_counters 
        WHERE orderType = :orderType
        LIMIT 1
    """)
    suspend fun get(
        orderType: String
    ): VirtualTableCounterEntity?


    @Query("""
        SELECT lastNumber
        FROM virtual_table_counters
        WHERE orderType = :orderType
        LIMIT 1
    """)
    suspend fun getLastNumber(
        orderType: String
    ): Int?


    @Upsert
    suspend fun upsert(
        counter: VirtualTableCounterEntity
    )


    @Query("""
        UPDATE virtual_table_counters
        SET lastNumber = :number
        WHERE orderType = :orderType
    """)
    suspend fun updateNumber(
        orderType: String,
        number: Int
    )


    // =========================================================
    // CHECK WHETHER CURRENT VIRTUAL TABLE IS USED
    // =========================================================

    @Query("""
        SELECT status
        FROM virtual_tables
        WHERE orderType = :orderType
        ORDER BY createdAt DESC
        LIMIT 1
    """)
    suspend fun getLatestTableStatus(
        orderType: String
    ): String?
}