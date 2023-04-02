package com.cyril.account.history.data

import com.cyril.account.core.utils.RetrofitUtils.mapper
import com.cyril.account.home.data.response.CurrencyResp
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
}