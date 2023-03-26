package com.cyril.account.home.di

import com.cyril.account.home.data.api.PersonalApi
import com.cyril.account.home.data.repository.PersonalRep
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
    fun providePersonalApi(client: Retrofit) =
        client.create(PersonalApi::class.java)

    @ViewModelScoped
    @Provides
    fun providePersonalRep(personalApi: PersonalApi) = PersonalRep(personalApi, Dispatchers.Main)
}