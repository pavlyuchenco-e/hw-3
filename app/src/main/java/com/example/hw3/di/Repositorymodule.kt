package com.example.hw3.di

import com.example.hw3.data.IShowRepository
import com.example.hw3.data.ShowRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindShowRepository(
        impl: ShowRepository
    ): IShowRepository
}