package com.cyril.account.home.data.api

import com.cyril.account.home.data.response.AccountResp
import com.cyril.account.home.data.response.CurrencyResp
import com.cyril.account.home.data.response.PersonalResp
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import java.math.BigDecimal
import java.util.*

interface PersonalApi {
    @GET("client/{id}/personal")
    suspend fun getPersonal(@Path("id") id: UUID): Response<List<PersonalResp>>

    @DELETE("client/{id}/personal/{persId}")
    suspend fun delPersonal(@Path("id") id: UUID, @Path("persId") persId: UUID): Response<Void>

    @PUT("client/{id}/default_personal/{persId}")
    suspend fun changeDefault(@Path("id") id: UUID, @Path("persId") persId: UUID): Response<Void>

    @POST("personal")
    suspend fun addCard(
        @Query("client_id") clientId: UUID,
        @Query("account_id") accountId: UUID,
        @Query("code") code: Int,
        @Query("money") money: BigDecimal?,
        @Query("sender_account_id") senderAccountId: UUID?
    ): Response<Void>

    @GET("account")
    suspend fun getAccounts(): Response<List<AccountResp>>

    @GET("currency/{code}")
    suspend fun getCurrency(@Path("code") code: Int): Response<CurrencyResp>

    @GET("currency")
    suspend fun getCurrencies(): Response<List<CurrencyResp>>

    @GET("currency/convert")
    suspend fun convert(
        @Query("from_code") fromCode: Int,
        @Query("to_code") toCode: Int,
        @Query("sum") sum: BigDecimal
    ): Response<BigDecimal>
}