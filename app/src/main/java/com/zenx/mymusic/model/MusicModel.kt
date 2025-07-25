package com.zenx.mymusic.model

import android.content.Context
import android.util.Log
import com.zenx.mymusic.contract.MusicContract
import com.zenx.mymusic.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MusicModel(private val context: Context) : MusicContract.Model {
    
    override suspend fun getSongs(): List<Song> {
        Log.d("TrungLQ", "getSongs() called")
        return try {
            Log.d("TrungLQ", "Making API call...")
            val response = withContext(Dispatchers.IO) {
                try {
                    ApiClient.instance.getSongs()
                } catch (e: Exception) {
                    Log.e("TrungLQ", "API call failed: ${e.message}", e)
                    throw e
                }
            }
            
            Log.d("TrungLQ", "Response code: ${response.code()}")
            Log.d("TrungLQ", "Response message: ${response.message()}")
            Log.d("TrungLQ", "Response headers: ${response.headers()}")
            Log.d("TrungLQ", "Response body: ${response.body()}")
            
            if (!response.isSuccessful) {
                Log.e("TrungLQ", "API call not successful: ${response.errorBody()?.string()}")
                return emptyList()
            }
            
            val songs = response.body() ?: emptyList()
            Log.d("TrungLQ", "Successfully fetched ${songs.size} songs")
            songs
            
        } catch (e: Exception) {
            Log.e("TrungLQ", "Error in getSongs: ${e.message}", e)
            emptyList()
        }
    }
}