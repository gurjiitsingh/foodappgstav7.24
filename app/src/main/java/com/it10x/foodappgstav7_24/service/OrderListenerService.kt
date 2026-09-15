package com.it10x.foodappgstav7_24.service

import android.app.*
import android.content.*
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.firebase.FirebaseApp
import com.it10x.foodappgstav7_24.R
import com.it10x.foodappgstav7_24.printer.AutoPrintManager
import com.it10x.foodappgstav7_24.data.online.repository.OrdersRepository
import com.it10x.foodappgstav7_24.printer.PrinterManager
//online orders
class OrderListenerService : Service() {

    private lateinit var listener: ServiceRealtimeOrdersListener
    private var isReceiverRegistered = false

    private val stopSoundReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            android.util.Log.e("STOP_SOUND", "Broadcast received")
            listener.stopRingtone()
            android.util.Log.e("STOP_SOUND", "Ringtone STOP requested")
        }
    }

    override fun onCreate() {
        super.onCreate()

        android.util.Log.e(
            "ONLINE_ORDER_FLOW",
            "1️⃣ OrderListenerService.onCreate() START"
        )

        if (FirebaseApp.getApps(this).isEmpty()) {

            android.util.Log.e(
                "ONLINE_ORDER_FLOW",
                "❌ FirebaseApp NOT initialized - stopping service"
            )

            stopSelf()
            return
        }

        android.util.Log.d(
            "ONLINE_ORDER_FLOW",
            "2️⃣ FirebaseApp is initialized"
        )

        val printerManager = PrinterManager.getInstance(this)

        android.util.Log.d(
            "ONLINE_ORDER_FLOW",
            "3️⃣ PrinterManager created"
        )

        val ordersRepo = OrdersRepository()

        android.util.Log.d(
            "ONLINE_ORDER_FLOW",
            "4️⃣ OrdersRepository created"
        )

        val autoPrint = AutoPrintManager(
            printerManager = printerManager,
            ordersRepository = ordersRepo
        )

        android.util.Log.d(
            "ONLINE_ORDER_FLOW",
            "5️⃣ AutoPrintManager created"
        )

        listener = ServiceRealtimeOrdersListener(
            context = application,
            autoPrintManager = autoPrint
        )

        android.util.Log.d(
            "ONLINE_ORDER_FLOW",
            "6️⃣ ServiceRealtimeOrdersListener created"
        )

        listener.startListening()

        android.util.Log.e(
            "ONLINE_ORDER_FLOW",
            "7️⃣ ServiceRealtimeOrdersListener.startListening() CALLED"
        )

        val filter = IntentFilter("STOP_RINGTONE")

        if (Build.VERSION.SDK_INT >= 33) {

            registerReceiver(
                stopSoundReceiver,
                filter,
                Context.RECEIVER_NOT_EXPORTED
            )

        } else {

            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(
                stopSoundReceiver,
                filter
            )
        }

        isReceiverRegistered = true

        android.util.Log.d(
            "ONLINE_ORDER_FLOW",
            "8️⃣ STOP_RINGTONE receiver registered"
        )

        startForeground(
            99,
            buildNotification()
        )

        android.util.Log.e(
            "ONLINE_ORDER_FLOW",
            "9️⃣ OrderListenerService FOREGROUND STARTED"
        )
    }
    private fun buildNotification(): Notification {
        val channelId = "orders_monitor"
        val channelName = "Order Monitoring"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Order Monitoring Active")
            .setContentText("Listening for new orders…")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {

        if (isReceiverRegistered) {
            try {
                unregisterReceiver(stopSoundReceiver)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isReceiverRegistered = false
        }

        if (::listener.isInitialized) {
            listener.stopListening()
        }

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}