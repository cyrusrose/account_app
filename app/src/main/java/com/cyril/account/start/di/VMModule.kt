package com.cyril.account.start.di

import com.cyril.account.core.data.UserApi
import com.cyril.account.core.data.UserRep
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
    fun provideUserApi(client: Retrofit) =
        client.create(UserApi::class.java)

    @ViewModelScoped
    @Provides
    fun provideUserRep(userApi: UserApi) = UserRep(userApi, Dispatchers.IO)
}