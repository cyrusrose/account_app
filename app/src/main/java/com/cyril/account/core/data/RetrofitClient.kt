package com.cyril.account.core.data

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

object RetrofitClient {
    private var retrofit: Retrofit? = null
    private val baseUrl: String = "http://10.0.2.2:8080/api/v1/"
    val mapper = ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(KotlinModule())

    fun get(): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .build()
        }

        return retrofit!!
    }
}