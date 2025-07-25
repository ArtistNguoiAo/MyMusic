package com.zenx.mymusic.model

import android.content.Context
import android.util.Log
import com.zenx.mymusic.contract.MusicContract
import com.zenx.mymusic.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MusicModel(private val context: Context) : MusicContract.Model {
    
    override suspend fun getSongs(): List<Song> {
        return try {
            val response = withContext(Dispatchers.IO) {
                ApiClient.instance.getSongs()
            }
            Log.d("TrungLQ", "getSongs: ${response.body()}")
            
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}