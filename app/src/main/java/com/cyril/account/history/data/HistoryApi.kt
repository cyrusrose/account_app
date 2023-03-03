package com.cyril.account.history.data

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.math.BigDecimal
import java.math.BigInteger
import java.util.UUID

interface HistoryApi {
    @GET("client/{id}/transaction")
    suspend fun getHistory(
        @Path("id") id: UUID,
        @Query("state") state: String?,
        @Query("via") via: String?,
    ): Response<List<HistoryResp>>

    @POST("transaction")
    suspend fun sendMoneyByPhone(
        @Query("money") money: BigDecimal,
        @Query("sender_account_id") senderAccountId: UUID,
        @Query("phone") phone: String
    ): Response<Void>

    @POST("transaction")
    suspend fun sendMoneyByClientNo(
        @Query("money") money: BigDecimal,
        @Query("sender_account_id") senderAccountId: UUID,
        @Query("client_no") clientNo: BigInteger,
        @Query("ssn") clientSsn: BigInteger
    ): Response<Void>

    @POST("transaction")
    suspend fun sendMoneyByCard(
        @Query("money") money: BigDecimal,
        @Query("sender_account_id") senderAccountId: UUID,
        @Query("card_no") cardNo: BigInteger
    ): Response<Void>

    @POST("transaction")
    suspend fun sendMoney(
        @Query("money") money: BigDecimal,
        @Query("sender_account_id") senderAccountId: UUID,
        @Query("receiver_account_id") receiverAccountId: UUID
    ): Response<Void>
}