package com.cyril.account.rest.user

import com.cyril.account.rest.user.client.ClientNoResp
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface UserApi {
    @GET("user_with_client")
    suspend fun getUserWithClient(@Query("login") login: String, @Query("pw") password: String): Response<UserResp?>

    @GET("client_nos")
    suspend fun getClientNos(@Query("ssn") ssn: String? = null): Response<List<ClientNoResp>>
}