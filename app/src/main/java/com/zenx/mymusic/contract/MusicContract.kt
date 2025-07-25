package com.zenx.mymusic.contract

import com.zenx.mymusic.model.Song

interface MusicContract {
    
    interface View {
        fun showSongs(songs: List<Song>)
        fun showCurrentSong(song: Song)
        fun updatePlaybackState(isPlaying: Boolean)
        fun updateProgress(progress: Int, duration: Int)
        fun showError(message: String)
    }
    
    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun loadSongs()
        fun playSong(song: Song)
        fun playPause()
        fun nextSong()
        fun previousSong()
        fun seekTo(position: Int)
        fun onDestroy()
    }
    
    interface Model {
        suspend fun getSongs(): List<Song>
    }
}