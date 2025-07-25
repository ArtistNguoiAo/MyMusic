data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val data: String, // File path
    val albumArt: String? = null
)

data class Playlist(
    val songs: List<Song>,
    var currentIndex: Int = 0
) {
    fun getCurrentSong(): Song? = if (songs.isNotEmpty()) songs[currentIndex] else null
    
    fun hasNext(): Boolean = currentIndex < songs.size - 1
    
    fun hasPrevious(): Boolean = currentIndex > 0
    
    fun next(): Song? {
        return if (hasNext()) {
            currentIndex++
            getCurrentSong()
        } else null
    }
    
    fun previous(): Song? {
        return if (hasPrevious()) {
            currentIndex--
            getCurrentSong()
        } else null
    }
}