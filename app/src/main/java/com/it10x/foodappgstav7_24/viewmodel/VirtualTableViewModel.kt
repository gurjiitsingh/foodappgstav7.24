package com.it10x.foodappgstav7_24.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

import com.it10x.foodappgstav7_24.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_24.data.pos.entities.VirtualTableCounterEntity
import com.it10x.foodappgstav7_24.data.pos.entities.VirtualTableEntity

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

import java.util.Calendar
import java.util.Locale
import java.util.UUID


class VirtualTableViewModel(
    app: Application
) : AndroidViewModel(app) {

    companion object {
        const val STATUS_NEW = "NEW"
        const val STATUS_RUNNING = "RUNNING"
        const val STATUS_COMPLETED = "COMPLETED"
    }
    private val db =
        AppDatabaseProvider.get(app)

    private val dao =
        db.virtualTableDao()
    private val counterDao =
        db.virtualTableCounterDao()

    private val cartDao =
        db.cartDao()

    private val kotDao =
        db.kotItemDao()


    // =========================================================
    // SELECTED ORDER TYPE
    // =========================================================

    private val selectedType =
        MutableStateFlow<String?>(null)


    // =========================================================
    // OBSERVE VIRTUAL TABLES
    // =========================================================

    val tables: StateFlow<List<VirtualTableEntity>> =
        selectedType
            .filterNotNull()
            .flatMapLatest { type ->

                dao.observeByType(type)
                    .onStart {
                        emit(emptyList())
                    }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )


    // =========================================================
    // SET ORDER TYPE
    // =========================================================

    fun setOrderType(type: String) {

        selectedType.value = type
    }


    private fun getTodayDate(): String {
        return SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        ).format(System.currentTimeMillis())
    }

    fun checkAndResetForNewDay() {
        viewModelScope.launch {

            val today = getTodayDate()

            val takeawayCounter =
                counterDao.get("TAKEAWAY")

            val deliveryCounter =
                counterDao.get("DELIVERY")

            val savedDate = when {
                takeawayCounter?.resetDate?.isNotEmpty() == true ->
                    takeawayCounter.resetDate

                deliveryCounter?.resetDate?.isNotEmpty() == true ->
                    deliveryCounter.resetDate

                else ->
                    ""
            }

            // Same day -> DO NOTHING
            if (savedDate == today) {
                return@launch
            }

            // =====================================================
            // NEW DAY
            // =====================================================

            // Delete all old virtual tables.
            // At this point only NEW/RUNNING tables should exist.
            dao.deleteAllVirtualTables()

            // Reset TW and DL counters to zero
            // and save today's date.
            counterDao.resetCounters(today)

            // If counter rows do not exist yet, create them.
            if (takeawayCounter == null) {
                counterDao.upsert(
                    VirtualTableCounterEntity(
                        orderType = "TAKEAWAY",
                        lastNumber = 0,
                        status = "NEW",
                        resetDate = today
                    )
                )
            }

            if (deliveryCounter == null) {
                counterDao.upsert(
                    VirtualTableCounterEntity(
                        orderType = "DELIVERY",
                        lastNumber = 0,
                        status = "NEW",
                        resetDate = today
                    )
                )
            }
        }
    }
    // =========================================================
// CREATE / REUSE TAKEAWAY / DELIVERY
// =========================================================

    suspend fun createNew(type: String): VirtualTableEntity {

        val prefix = when (type) {
            "TAKEAWAY" -> "TW"
            "DELIVERY" -> "DL"
            else -> throw IllegalArgumentException(
                "Invalid virtual table type: $type"
            )
        }

        // ---------------------------------------------------------
        // GET LATEST TABLE FOR THIS ORDER TYPE
        // ---------------------------------------------------------

        val currentTable =
            dao.getLatestByType(type)

        // ---------------------------------------------------------
        // CASE 1:
        // NO TABLE EXISTS
        //
        // First order:
        // DELIVERY -> DL1
        // TAKEAWAY -> TW1
        // ---------------------------------------------------------

        if (currentTable == null) {

            val nextNumber =
                (counterDao.getLastNumber(type) ?: 0) + 1

            counterDao.upsert(
                VirtualTableCounterEntity(
                    orderType = type,
                    lastNumber = nextNumber
                )
            )

            val now = System.currentTimeMillis()

            val newTable =
                VirtualTableEntity(
                    id = "${prefix}_${UUID.randomUUID()}",
                    tableName = "$prefix$nextNumber",
                    orderType = type,
                    status = "NEW",
                    createdAt = now,
                    updatedAt = now
                )

            dao.insert(newTable)

            return newTable
        }

        // ---------------------------------------------------------
        // CASE 2:
        // CURRENT TABLE IS NEW / EMPTY
        //
        // DO NOT INCREMENT
        //
        // Example:
        // DL1 NEW
        //
        // DL -> TW -> DL
        //
        // Still DL1
        // ---------------------------------------------------------

        if (currentTable.status == "NEW") {
            return currentTable
        }

        // ---------------------------------------------------------
        // CASE 3:
        // CURRENT TABLE IS ALREADY USED
        //
        // RUNNING or COMPLETED
        //
        // Create next number.
        //
        // DL1 RUNNING    -> DL2
        // DL1 COMPLETED  -> DL2
        // ---------------------------------------------------------

        if (
            currentTable.status == "RUNNING" ||
            currentTable.status == "COMPLETED"
        ) {

            val nextNumber =
                (counterDao.getLastNumber(type) ?: 0) + 1

            counterDao.upsert(
                VirtualTableCounterEntity(
                    orderType = type,
                    lastNumber = nextNumber
                )
            )

            val now = System.currentTimeMillis()

            val newTable =
                VirtualTableEntity(
                    id = "${prefix}_${UUID.randomUUID()}",
                    tableName = "$prefix$nextNumber",
                    orderType = type,
                    status = "NEW",
                    createdAt = now,
                    updatedAt = now
                )

            dao.insert(newTable)

            return newTable
        }

        // ---------------------------------------------------------
        // FALLBACK
        // ---------------------------------------------------------

        return currentTable
    }
    // =========================================================
    // START OF TODAY
    // =========================================================

    private fun getStartOfToday(): Long {

        val cal =
            Calendar.getInstance()

        cal.set(
            Calendar.HOUR_OF_DAY,
            0
        )

        cal.set(
            Calendar.MINUTE,
            0
        )

        cal.set(
            Calendar.SECOND,
            0
        )

        cal.set(
            Calendar.MILLISECOND,
            0
        )

        return cal.timeInMillis
    }


    // =========================================================
    // DELETE OLD TABLES
    // =========================================================

    fun deleteOldTables(
        type: String
    ) {

        viewModelScope.launch {

            val startOfToday =
                getStartOfToday()

            dao.deleteOldTables(
                type,
                startOfToday
            )
        }
    }


    fun logAllTableStatuses() {

        viewModelScope.launch {

            val tables = dao.getAllTables()


        }
    }
    // =========================================================
    // DELETE
    // =========================================================

    fun delete(
        id: String
    ) {

        viewModelScope.launch {

            dao.deleteById(id)
        }
    }
}
