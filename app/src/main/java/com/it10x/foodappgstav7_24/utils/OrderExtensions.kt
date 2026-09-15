package com.it10x.foodappgstav7_24.utils

import com.it10x.foodappgstav7_24.data.online.models.OrderMasterData

fun OrderMasterData.createdAtMillis(): Long {
    return createdAt?.toDate()?.time ?: createdAtMillis
}