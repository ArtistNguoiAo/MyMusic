package com.zenx.mymusic.presenter

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.zenx.mymusic.contract.MusicContract
import com.zenx.mymusic.model.Playlist
import com.zenx.mymusic.model.Song
import com.zenx.mymusic.service.MusicService

class MusicPresenter(private val context: Context, private val model: MusicContract.Model) : MusicContract.Presenter {

    private var view: MusicContract.View? = null
    private var musicService: MusicService? = null
    private var playlist: Playlist? = null
    private val handler = Handler(Looper.getMainLooper())
    private var progressRunnable: Runnable? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            playlist?.let { musicService?.setPlaylist(it) }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
        }
    }

    override fun attachView(view: MusicContract.View) {
        this.view = view
        bindMusicService()
        startProgressUpdates()
    }

    override fun detachView() {
        this.view = null
        stopProgressUpdates()
    }

    override fun loadSongs() {
        try {
            val songs = model.getSongs()
            playlist = Playlist(songs)
            view?.showSongs(songs)
            musicService?.setPlaylist(playlist!!)
        } catch (e: Exception) {
            view?.showError("Failed to load songs: ${e.message}")
        }
    }

    override fun playSong(song: Song) {
        playlist?.let { pl ->
            val index = pl.songs.indexOf(song)
            if (index != -1) {
                pl.currentIndex = index
                musicService?.playSong(song)
                view?.showCurrentSong(song)
            }
        }
    }

    override fun playPause() {
        musicService?.playPause()
        updatePlaybackState()
    }

    override fun nextSong() {
        musicService?.nextSong()
        playlist?.getCurrentSong()?.let { song ->
            view?.showCurrentSong(song)
        }
        updatePlaybackState()
    }

    override fun previousSong() {
        musicService?.previousSong()
        playlist?.getCurrentSong()?.let { song ->
            view?.showCurrentSong(song)
        }
        updatePlaybackState()
    }

    override fun seekTo(position: Int) {
        musicService?.seekTo(position)
    }

    override fun onDestroy() {
        context.unbindService(serviceConnection)
        stopProgressUpdates()
    }

    private fun bindMusicService() {
        val intent = Intent(context, MusicService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        context.startService(intent)
    }

    private fun updatePlaybackState() {
        val isPlaying = musicService?.isPlaying() ?: false
        view?.updatePlaybackState(isPlaying)
    }

    private fun startProgressUpdates() {
        progressRunnable = object : Runnable {
            override fun run() {
                musicService?.let { service ->
                    val progress = service.getCurrentPosition()
                    val duration = service.getDuration()
                    view?.updateProgress(progress, duration)
                }
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(progressRunnable!!)
    }

    private fun stopProgressUpdates() {
        progressRunnable?.let { handler.removeCallbacks(it) }
    }
}