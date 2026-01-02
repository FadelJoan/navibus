package com.rafid.navibus.data.api

import com.rafid.navibus.data.model.OsrmResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface OsrmApiService {
    @GET("route/v1/driving/{coordinates}")
    fun getRoute(
        @Path("coordinates") coordinates: String
    ): Call<OsrmResponse>
}
