package it.paoloinfante.rowerplus.repositories

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class RepositoryModule {
    @Provides
    @Singleton
    fun provideUsbServiceRepository(): UsbServiceRepository {
        return UsbServiceRepository()
    }
}