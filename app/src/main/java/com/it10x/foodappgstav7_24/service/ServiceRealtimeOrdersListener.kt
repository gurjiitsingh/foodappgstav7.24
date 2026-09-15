package com.it10x.foodappgstav7_24.service

import android.content.Context
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.util.Log

import com.google.firebase.Timestamp
import com.it10x.foodappgstav7_24.data.online.models.OrderMasterData
import com.it10x.foodappgstav7_24.data.online.repository.RealtimeOrdersRepository
import com.it10x.foodappgstav7_24.printer.AutoPrintManager
//********************** PRINT AND RING OLINE ORDERS
class ServiceRealtimeOrdersListener(
    private val context: Context,
    private val autoPrintManager: AutoPrintManager
) {

    private val repo = RealtimeOrdersRepository()

    private var ringtone: Ringtone? = null

    private val listeningStartedAt = Timestamp.now()

    private val ringingLock = Any()

    fun startListening() {

        Log.d(
            "ONLINE_ORDER",
            "🔥 Online order listener STARTED"
        )

        repo.startListening { order ->

            Log.d(
                "ONLINE_ORDER",
                "📥 Firestore order received: " +
                        "id=${order.id}, " +
                        "source=${order.source}, " +
                        "srno=${order.srno}"
            )

            // =====================================================
            // 1. ONLY WEB ORDERS SHOULD TRIGGER RING / AUTO PRINT
            // =====================================================

            if (order.source != "WEB") {

                Log.d(
                    "ONLINE_ORDER",
                    "⛔ Ignoring non-WEB order: " +
                            "id=${order.id}, " +
                            "source=${order.source}"
                )

                return@startListening
            }

            // =====================================================
            // 2. IGNORE ALREADY PRINTED ORDERS
            // =====================================================

            if (order.printed == true) {

                Log.d(
                    "ONLINE_ORDER",
                    "⛔ Order already printed: ${order.id}"
                )

                return@startListening
            }

            // =====================================================
            // 3. CHECK CREATED AT
            // =====================================================

            val createdAt = order.createdAt

            if (createdAt == null) {

                Log.d(
                    "ONLINE_ORDER",
                    "⛔ Order has no createdAt: ${order.id}"
                )

                return@startListening
            }

            // =====================================================
            // 4. IGNORE ORDERS THAT EXISTED BEFORE LISTENER STARTED
            // =====================================================

            if (createdAt.seconds <= listeningStartedAt.seconds) {

                Log.d(
                    "ONLINE_ORDER",
                    "⏭ Old WEB order ignored: ${order.id}"
                )

                return@startListening
            }

            // =====================================================
            // 5. NEW WEB ORDER
            // =====================================================

            Log.e(
                "ONLINE_ORDER",
                "🚨 NEW WEB ORDER: ${order.id}"
            )

            // =====================================================
            // 6. RING BELL
            // =====================================================

            playRingtone()

            // =====================================================
            // 7. AUTO PRINT
            // =====================================================

            Log.d(
                "ONLINE_ORDER",
                "🖨 Sending WEB order to AutoPrintManager: ${order.id}"
            )

            autoPrintManager.onNewOrder(order)
        }
    }

    private fun playRingtone() {

        synchronized(ringingLock) {

            if (ringtone?.isPlaying == true) {

                Log.d(
                    "ONLINE_ORDER",
                    "🔔 Bell already ringing"
                )

                return
            }

            Log.e(
                "ONLINE_ORDER",
                "🔔 START RINGING"
            )

            val alarmUri =
                RingtoneManager.getDefaultUri(
                    RingtoneManager.TYPE_ALARM
                )

            ringtone =
                RingtoneManager
                    .getRingtone(
                        context,
                        alarmUri
                    )
                    .apply {

                        audioAttributes =
                            AudioAttributes.Builder()
                                .setUsage(
                                    AudioAttributes.USAGE_ALARM
                                )
                                .setContentType(
                                    AudioAttributes.CONTENT_TYPE_SONIFICATION
                                )
                                .build()

                        isLooping = true

                        play()
                    }
        }
    }

    fun stopRingtone() {

        synchronized(ringingLock) {

            Log.d(
                "ONLINE_ORDER",
                "🔕 STOP RINGING"
            )

            try {
                ringtone?.stop()
            } catch (e: Exception) {
                Log.e(
                    "ONLINE_ORDER",
                    "Error stopping ringtone",
                    e
                )
            }

            ringtone = null
        }
    }

    fun stopListening() {

        Log.d(
            "ONLINE_ORDER",
            "🛑 Online order listener STOPPED"
        )

        repo.stopListening()

        stopRingtone()
    }
}