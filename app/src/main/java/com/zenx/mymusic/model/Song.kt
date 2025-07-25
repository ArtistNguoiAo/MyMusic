package com.zenx.mymusic.model

import com.google.gson.annotations.SerializedName

/**
 * Data class representing a song from the API
 * @property id Unique identifier for the song
 * @property name The name/title of the song
 * @property url The YouTube URL of the song
 */
data class Song(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("url")
    val url: String
) {
    // For backward compatibility with existing code
    val title: String
        get() = name
        
    val data: String
        get() = url
        
    // Default values for compatibility
    val artist: String = "Unknown Artist"
    val album: String = "Unknown Album"
    val duration: Long = 0
    val albumArt: String? = null
}

data class Playlist(
    val songs: List<Song>,
    var currentIndex: Int = 0
) {
    val size: Int get() = songs.size
    val isEmpty: Boolean get() = songs.isEmpty()
    
    fun getCurrentSong(): Song? = songs.getOrNull(currentIndex)
    
    fun hasNext(): Boolean = currentIndex < songs.size - 1
    
    fun hasPrevious(): Boolean = currentIndex > 0
    
    fun next(): Song? {
        if (songs.isEmpty()) return null
        currentIndex = (currentIndex + 1) % songs.size
        return getCurrentSong()
    }
    
    fun previous(): Song? {
        if (songs.isEmpty()) return null
        currentIndex = (currentIndex - 1 + songs.size) % songs.size
        return getCurrentSong()
    }
    
    fun getSongAt(index: Int): Song? = songs.getOrNull(index)
    
    fun indexOf(song: Song): Int = songs.indexOf(song)
    
    fun contains(song: Song): Boolean = songs.contains(song)
}