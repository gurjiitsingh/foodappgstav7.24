package com.it10x.foodappgstav7_24.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_24.data.PrinterRole
import com.it10x.foodappgstav7_24.data.online.models.OrderMasterData
import com.it10x.foodappgstav7_24.data.online.models.OrderProductData
import com.it10x.foodappgstav7_24.data.online.models.formattedTime
import com.it10x.foodappgstav7_24.data.online.repository.OrdersRepository
import com.it10x.foodappgstav7_24.printer.PrinterManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.it10x.foodappgstav7_24.printer.FirestorePrintMapper
import com.it10x.foodappgstav7_24.printer.ReceiptFormatter
import com.it10x.foodappgstav7_24.data.pos.AppDatabaseProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.it10x.foodappgstav7_24.data.mapper.OnlineOrderMapper
import com.it10x.foodappgstav7_24.data.online.models.CategorySaleData
//import com.it10x.foodappgstav7_24.data.online.models.createdAtMillis
import java.util.Calendar

class OnlineOrdersViewModel(
    private val printerManager: PrinterManager
) : ViewModel() {

    private val repo = OrdersRepository()


    suspend fun getOrderItems(orderId: String): List<OrderProductData> {
        return repo.getOrderProducts(orderId)
    }

    private val _orderItems = MutableStateFlow<Map<String, List<OrderProductData>>>(emptyMap())
    val orderItems: StateFlow<Map<String, List<OrderProductData>>> = _orderItems

    fun loadOrderItems(orderId: String) {
        viewModelScope.launch {
            val items = repo.getOrderProducts(orderId)
            _orderItems.value = _orderItems.value + (orderId to items)
        }
    }
    val pageIndex = MutableStateFlow(0)
    private val _orders = MutableStateFlow<List<OrderMasterData>>(emptyList())
    val orders: StateFlow<List<OrderMasterData>> = _orders

    // -----------------------------
// CATEGORY SALES STATE
// -----------------------------
    private val _categorySales = MutableStateFlow<List<CategorySaleData>>(emptyList())
    val categorySales: StateFlow<List<CategorySaleData>> = _categorySales

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val limit = 10

    // -----------------------------
    // PRINT ORDER (KITCHEN + BILLING)
    // -----------------------------

    private fun loadOrders(page: Int) {

        viewModelScope.launch {

            try {

                _loading.value = true

                pageIndex.value = page

                val offset = page * limit

                val pagedOrders = repo.getPagedOrders(
                    limit = limit,
                    offset = offset
                )

                _orders.value = pagedOrders
                    .sortedByDescending { it.createdAtMillis }

            } catch (e: Exception) {

                Log.e("PAGED_ORDERS", "Load failed", e)

            } finally {

                _loading.value = false
            }
        }
    }
    fun printOrder(order: OrderMasterData) {

        //   Log.d("PRINT_SOURCE", "🔥 OrdersViewModel.printOrder CALLED")
        viewModelScope.launch {

            val items = repo.getOrderProducts(order.id)
            Log.e("PRINT", "Order No. ${order.srno}")
            if (items.isEmpty()) {
                Log.e("PRINT", "No items for order ${order.srno}")
                return@launch
            }

            //BILL PRINT ONLINE ORDER WHEN BUTTON PRESSED
            val printOrder = FirestorePrintMapper.map(order, items)
            printerManager.printTextNew(PrinterRole.BILLING, printOrder, kotNumberText = "", stewardName = "")

            kotlinx.coroutines.delay(2_000)


            //KITCHEN PRINT ONLINE ORDER WHEN BUTTON PRESSED
            val kotItems = OnlineOrderMapper.toKotItems(items)

            printerManager.printTextKitchen(
                PrinterRole.KITCHEN,
                tableName = order.srno.toString(),
                sessionKey = order.srno.toString(),
                orderType = "Online order",
                kotItems,
                kotNumber = "",
            ){
                Log.d("PRINT", "Kitchen print success=$it")
            }


        }
    }


    fun printOrderFromHistory(orderId: String, role: String) {

        viewModelScope.launch {

            _loading.value = true

            try {

                Log.d("ONLINE_PRINT", "Print requested for orderId=$orderId")

                // 🔥 Get order from already loaded list
                val order = _orders.value.find { it.id == orderId }

                if (order == null) {

                    Log.e("ONLINE_PRINT", "Order NOT FOUND for orderId=$orderId")
                    return@launch
                }

                // 🔥 Get Firestore items
                val items = repo.getOrderProducts(orderId)

                if (items.isEmpty()) {

                    Log.e("ONLINE_PRINT", "No items for orderId=$orderId")
                    return@launch
                }

                // -----------------------------
                // BILL PRINT
                // -----------------------------

                if (role == "bill") {

                    val printOrder = FirestorePrintMapper.map(order, items)

                    printerManager.printTextNew(
                        PrinterRole.BILLING,
                        printOrder,
                        kotNumberText = "", stewardName = ""
                    )
                }

                // -----------------------------
                // KITCHEN PRINT
                // -----------------------------

                if (role == "kitchen") {

                    val kotItems = OnlineOrderMapper.toKotItems(items)

                    printerManager.printTextKitchen(
                        PrinterRole.KITCHEN,
                        tableName = order.srno.toString(),
                        sessionKey = order.srno.toString(),
                        orderType = "Online order",
                        kotItems,
                        kotNumber = "",
                    ) {
                        Log.d("ONLINE_PRINT", "Kitchen print success=$it")
                    }
                }

            } catch (e: Exception) {

                Log.e("ONLINE_PRINT", "Print failed", e)

            } finally {

                _loading.value = false
            }
        }
    }

    fun loadFirstPage() {
        viewModelScope.launch {

            _loading.value = true

            pageIndex.value = 0

            repo.resetPagination()

            _orders.value = repo.getFirstPage(limit.toLong())
                .sortedByDescending { it.createdAtMillis }

            _loading.value = false
        }
    }

    fun loadNextPage() {
        viewModelScope.launch {

            _loading.value = true

            val newOrders = repo.getNextPage(limit.toLong())

            if (newOrders.isNotEmpty()) {
                pageIndex.value++
                _orders.value = newOrders.sortedByDescending { it.createdAtMillis }
            }

            _loading.value = false
        }
    }

    fun loadPrevPage() {
        viewModelScope.launch {

            if (pageIndex.value == 0) return@launch

            _loading.value = true

            val prevOrders = repo.getPrevPage(limit.toLong())

            if (prevOrders.isNotEmpty()) {
                pageIndex.value--
                _orders.value = prevOrders.sortedByDescending { it.createdAtMillis }
            }

            _loading.value = false
        }
    }


    // -----------------------------
    // HELPERS
    // -----------------------------
    private fun totalLine(label: String, value: Double): String {
        if (value == 0.0) return "" // skip zero values
        val left = label.padEnd(14)
        val right = formatAmount(value).padStart(18)
        return left + right
    }
    private fun formatAmount(value: Double?): String =
        "%.2f".format(value ?: 0.0)

    private fun toDouble(value: Any?): Double =
        when (value) {
            is Double -> value
            is Long -> value.toDouble()
            is Int -> value.toDouble()
            is Float -> value.toDouble()
            is String -> value.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }

    private fun btSafe(text: String, max: Int): String {
        return text
            .replace(Regex("[^A-Za-z0-9 ]"), "") // remove Unicode
            .trim()
            .take(max)
    }


    fun searchOrdersByDate(startMillis: Long, endMillis: Long) {

        viewModelScope.launch {

            _loading.value = true

            val orders = repo.searchOrdersByDate(
                startMillis = startMillis,
                endMillis = endMillis,
                limit = 50
            )

            _orders.value = orders
                .sortedByDescending { it.createdAtMillis }

            _loading.value = false
        }
    }

    fun buildDayRange(selectedDate: Long): Pair<Long, Long> {

        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = selectedDate

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val start = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)

        val end = calendar.timeInMillis

        return Pair(start, end)
    }


    // -----------------------------
// POS HISTORY
// -----------------------------
    fun loadPosHistoryFirstPage() {
        loadOrders( 0)

    }

    fun loadPosHistoryNextPage() {
        loadOrders(pageIndex.value + 1)
    }

    fun loadPosHistoryPrevPage() {

        if (pageIndex.value == 0) return

        loadOrders(pageIndex.value - 1)
    }

    fun searchPOSOrdersByDate(startMillis: Long, endMillis: Long) {

        viewModelScope.launch {

            _loading.value = true

            val orders = repo.searchPOSOrdersByDate(
                startMillis = startMillis,
                endMillis = endMillis,
                limit = 100
            )

            _orders.value = orders
                .sortedByDescending { it.createdAtMillis }

            _loading.value = false
        }
    }

    // -----------------------------
// CATEGORY SALES REPORT
// -----------------------------
    fun loadCategorySales(startMillis: Long, endMillis: Long) {

        viewModelScope.launch {

            _loading.value = true

            val result = repo.getCategorySalesByDate(
                startMillis = startMillis,
                endMillis = endMillis
            )

            _categorySales.value = result

            _loading.value = false
        }
    }


}
