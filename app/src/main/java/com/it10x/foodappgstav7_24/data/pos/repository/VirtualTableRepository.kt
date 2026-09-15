package com.it10x.foodappgstav7_24.data.pos.repository

import android.util.Log
import com.it10x.foodappgstav7_24.data.pos.dao.CartDao
import com.it10x.foodappgstav7_24.data.pos.dao.KotItemDao
import com.it10x.foodappgstav7_24.data.pos.dao.VirtualTableCounterDao
import com.it10x.foodappgstav7_24.data.pos.dao.VirtualTableDao
import com.it10x.foodappgstav7_24.data.pos.entities.VirtualTableCounterEntity
import com.it10x.foodappgstav7_24.data.pos.entities.VirtualTableEntity
import java.util.UUID

class VirtualTableRepository(
    private val virtualDao: VirtualTableDao,
    private val cartDao: CartDao,
    private val kotDao: KotItemDao,
    private val counterDao: VirtualTableCounterDao
) {

    // =========================================================
    // CREATE / GET CURRENT TAKEAWAY / DELIVERY TABLE
    // =========================================================

    suspend fun createNew(type: String): VirtualTableEntity {

        val prefix = when (type) {
            "TAKEAWAY" -> "TW"
            "DELIVERY" -> "DL"
            else -> throw IllegalArgumentException(
                "Invalid virtual table type: $type"
            )
        }

        // -----------------------------------------------------
        // Get latest table for this order type
        // -----------------------------------------------------

        val currentTable = virtualDao.getLatestByType(type)

        // -----------------------------------------------------
        // CASE 1: No table exists
        // -----------------------------------------------------

        if (currentTable == null) {

            return createNext(type, prefix)
        }

        // -----------------------------------------------------
        // CASE 2: Current table is NEW
        //
        // Reuse it.
        //
        // TW30 NEW -> TW30
        // DL30 NEW -> DL30
        // -----------------------------------------------------

        if (currentTable.status == "NEW") {
            return currentTable
        }

        // -----------------------------------------------------
        // CASE 3: Current table already used
        //
        // RUNNING / COMPLETED -> create next
        //
        // TW30 -> TW31
        // DL30 -> DL31
        // -----------------------------------------------------

        if (
            currentTable.status == "RUNNING" ||
            currentTable.status == "COMPLETED"
        ) {
            return createNext(type, prefix)
        }

        // -----------------------------------------------------
        // Fallback
        // -----------------------------------------------------

        return currentTable
    }


    // =========================================================
    // CREATE NEXT VIRTUAL TABLE
    // =========================================================

    private suspend fun createNext(
        type: String,
        prefix: String
    ): VirtualTableEntity {

        val nextNumber =
            (counterDao.getLastNumber(type) ?: 0) + 1

        counterDao.upsert(
            VirtualTableCounterEntity(
                orderType = type,
                lastNumber = nextNumber
            )
        )

        val now = System.currentTimeMillis()

        val newTable = VirtualTableEntity(
            id = "${prefix}_${UUID.randomUUID()}",
            tableName = "$prefix$nextNumber",
            orderType = type,
            status = "NEW",
            createdAt = now,
            updatedAt = now
        )

        virtualDao.insert(newTable)

        return newTable
    }


    // =========================================================
    // MARK COMPLETED + CREATE NEXT TABLE
    // =========================================================

    suspend fun markCompleted(
        tableId: String,
        tableName: String,
        orderType: String
    ): VirtualTableEntity? {

        if (
            orderType != "TAKEAWAY" &&
            orderType != "DELIVERY"
        ) {
            return null
        }

        // -----------------------------------------------------
        // Complete current table
        // -----------------------------------------------------


        val updatedRows = virtualDao.markCompleted(
            tableName = tableName,
            orderType = orderType,
            time = System.currentTimeMillis()
        )


//        val deletedRows = virtualDao.deleteCompletedTable(
//            tableName = tableName,
//            orderType = orderType
//        )

        // -----------------------------------------------------
        // IMPORTANT:
        // Directly create the NEXT table.
        //
        // Do NOT call createNew() here because createNew()
        // is designed to REUSE a NEW table.
        // -----------------------------------------------------

        val prefix = when (orderType) {
            "TAKEAWAY" -> "TW"
            "DELIVERY" -> "DL"
            else -> return null
        }

        return createNext(
            type = orderType,
            prefix = prefix
        )
    }


    // =========================================================
    // MARK TW/DL TABLE AS RUNNING
    // =========================================================

    suspend fun markRunningIfNew(
        tableId: String,
        orderType: String
    ) {

        if (
            orderType != "TAKEAWAY" &&
            orderType != "DELIVERY"
        ) {
            return
        }

        virtualDao.markRunningIfNew(
            tableId = tableId,
            time = System.currentTimeMillis()
        )
    }


    // =========================================================
    // SYNC CART COUNT
    // =========================================================

    suspend fun syncCartCount(
        tableId: String
    ) {

        val count =
            cartDao.getCartCountForTable(tableId) ?: 0

        virtualDao.setCartCount(
            tableId = tableId,
            count = count,
            time = System.currentTimeMillis()
        )
    }


    // =========================================================
    // SYNC BILL DATA
    // =========================================================

    suspend fun syncBillData(
        tableId: String
    ) {

        val billCount =
            kotDao.getBillQtyCount(tableId) ?: 0

        val billAmount =
            kotDao.sumDoneAmount(tableId) ?: 0.0

        virtualDao.setBillData(
            tableId = tableId,
            count = billCount,
            amount = billAmount,
            time = System.currentTimeMillis()
        )
    }


    // =========================================================
    // SYNC KITCHEN COUNT
    // =========================================================

    suspend fun syncKitchenCount(
        tableId: String
    ) {

        val count =
            kotDao.getKitchenCountForTable(tableId) ?: 0

        virtualDao.setKitchenCount(
            tableId = tableId,
            count = count,
            time = System.currentTimeMillis()
        )
    }


    // =========================================================
    // REMOVE IF EMPTY
    // =========================================================

    suspend fun removeIfEmpty(
        tableId: String
    ) {

        val table =
            virtualDao.getById(tableId) ?: return

        if (
            table.cartCount == 0 &&
            table.billCount == 0
        ) {
            virtualDao.deleteById(tableId)
        }
    }
}