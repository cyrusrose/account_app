package com.cyril.account.rest.history

import com.cyril.account.rest.RetrofitClient.mapper
import com.cyril.account.rest.personal.CurrencyResp
import com.cyril.account.rest.user.client.ClientResp
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class HistoryResp(
    val id: UUID,
    val money: BigDecimal,
    val currency: CurrencyResp,
    val via: String,
    val title: String,
    val titleRu: String?,
        @JsonDeserialize(using = LocalDateTimeDeserializer::class)
        @JsonSerialize(using = LocalDateTimeSerializer::class)
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    val time: LocalDateTime

) {
    override fun toString(): String {
        return mapper.writeValueAsString(this)
    }

//    companion object {
//        const val both = "both"
//        const val to = "to"
//        const val from = "from"
//        const val change = "change"
//    }
}