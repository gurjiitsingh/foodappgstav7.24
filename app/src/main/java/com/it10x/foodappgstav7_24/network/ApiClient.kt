package com.it10x.foodappgstav7_24.network

import com.it10x.foodappgstav7_24.data.models.ClientResponse

object ApiClient {

    suspend fun getClientConfig(clientId: String): ClientResponse {
        return RetrofitInstance.api.getClientConfig(clientId)
    }
}