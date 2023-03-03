package com.cyril.account.core.data

import com.cyril.account.core.data.response.ClientNoResp
import com.cyril.account.core.data.response.UserResp
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface UserApi {
    @GET("user_with_client")
    suspend fun getUserWithClient(@Query("login") login: String, @Query("pw") password: String): Response<UserResp?>

    @GET("client_nos")
    suspend fun getClientNos(@Query("ssn") ssn: String? = null): Response<List<ClientNoResp>>
}