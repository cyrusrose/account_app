package com.cyril.account.history.di

import com.cyril.account.history.data.HistoryApi
import com.cyril.account.history.data.HistoryRep
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.Dispatchers
import retrofit2.Retrofit

@Module
@InstallIn(ViewModelComponent::class)
class VMModule {
    @ViewModelScoped
    @Provides
    fun provideHistApi(client: Retrofit) =
        client.create(HistoryApi::class.java)

    @ViewModelScoped
    @Provides
    fun provideHistoryRep(histApi: HistoryApi) = HistoryRep(histApi, Dispatchers.Default)
}