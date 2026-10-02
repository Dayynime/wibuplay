package com.dayynime.wibuplay

import android.app.Application
import com.dayynime.wibuplay.data.api.NetworkClient
import com.dayynime.wibuplay.data.local.AppDatabase
import com.dayynime.wibuplay.data.repository.AnimeRepository

class WibuplayApp : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { AnimeRepository(NetworkClient.apiService, database.appDao()) }

    override fun onCreate() {
        super.onCreate()
    }
}
