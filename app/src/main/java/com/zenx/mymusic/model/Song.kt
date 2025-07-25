package com.zenx.mymusic.model

/**
 * Data class representing a song from the API
 * @property id Unique identifier for the song
 * @property title The name/title of the song
 * @property data The URL of the song (YouTube URL in this case)
 * @property artist The artist name (defaults to "Unknown Artist")
 * @property album The album name (defaults to "Unknown Album")
 * @property duration The duration of the song in milliseconds (defaults to 0)
 * @property albumArt Optional URL to the album art
 */
data class Song(
    val id: Long,
    val name: String,
    val url: String,
    val artist: String = "Unknown Artist",
    val album: String = "Unknown Album",
    val duration: Long = 0,
    val albumArt: String? = null
) {
    // For backward compatibility with existing code
    val title: String
        get() = name
        
    val data: String
        get() = url
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