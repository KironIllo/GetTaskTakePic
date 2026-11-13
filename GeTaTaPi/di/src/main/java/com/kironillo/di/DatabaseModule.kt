package com.kironillo.di

import android.content.Context
import androidx.room.Room
import com.kironillo.data.local.database.AppDatabase
import com.kironillo.data.local.database.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "getatapi_database",
        ).build()
    }

    @Provides
    @Singleton
    fun provideTaskDao(
        database: AppDatabase,
    ): TaskDao = database.taskDao()
}
