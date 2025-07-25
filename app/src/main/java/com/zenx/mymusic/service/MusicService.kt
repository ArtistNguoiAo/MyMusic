package com.zenx.mymusic.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.zenx.mymusic.R
import com.zenx.mymusic.model.Playlist
import com.zenx.mymusic.model.Song

class MusicService : Service(), MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {

    private var mediaPlayer: MediaPlayer? = null
    private var currentSong: Song? = null
    private var playlist: Playlist? = null
    private val binder = MusicBinder()
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var audioFocusGranted = false
    
    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                // Resume playback
                mediaPlayer?.setVolume(1.0f, 1.0f)
                if (!isPlaying()) {
                    playPause()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Lost focus for an unbounded amount of time: stop playback and release resources
                stopSelf()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Lost focus for a short time, but we have to stop playback
                if (isPlaying()) {
                    playPause()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Lower the volume, keep playing
                mediaPlayer?.setVolume(0.1f, 0.1f)
            }
        }
    }

    companion object {
        const val CHANNEL_ID = "MusicPlayerChannel"
        const val NOTIFICATION_ID = 1
        const val ACTION_PLAY_PAUSE = "ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "ACTION_NEXT"
        const val ACTION_PREVIOUS = "ACTION_PREVIOUS"
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> playPause()
            ACTION_NEXT -> nextSong()
            ACTION_PREVIOUS -> previousSong()
        }
        return START_STICKY
    }

    fun setPlaylist(playlist: Playlist) {
        this.playlist = playlist
    }

    fun playSong(song: Song) {
        currentSong = song
        try {
            // Release any previous media player
            mediaPlayer?.release()
            
            // Request audio focus
            if (!requestAudioFocus()) {
                Log.e("MusicService", "Failed to get audio focus")
                return
            }
            
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(song.data)
                setOnCompletionListener(this@MusicService)
                setOnPreparedListener(this@MusicService)
                setOnErrorListener { _, what, extra ->
                    Log.e("MusicService", "MediaPlayer error: what=$what, extra=$extra")
                    stopSelf()
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("MusicService", "Error playing song", e)
            stopSelf()
        }
    }
    
    private fun requestAudioFocus(): Boolean {
        audioManager?.let { am ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(audioFocusChangeListener)
                    .build()
                
                val result = am.requestAudioFocus(audioFocusRequest!!)
                audioFocusGranted = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
                return audioFocusGranted
            } else {
                @Suppress("DEPRECATION")
                val result = am.requestAudioFocus(
                    audioFocusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
                )
                audioFocusGranted = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
                return audioFocusGranted
            }
        }
        return false
    }

    fun playPause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                it.start()
            }
            updateNotification()
        }
    }

    fun nextSong() {
        playlist?.next()?.let { song ->
            playSong(song)
        }
    }

    fun previousSong() {
        playlist?.previous()?.let { song ->
            playSong(song)
        }
    }

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false

    fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0

    fun getDuration(): Int = mediaPlayer?.duration ?: 0

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    override fun onCompletion(mp: MediaPlayer?) {
        nextSong()
    }

    override fun onPrepared(mp: MediaPlayer?) {
        mp?.start()
        updateNotification()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Music Player",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Music Player Controls"
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun updateNotification() {
        currentSong?.let { song ->
            val playPauseIcon = if (isPlaying()) {
                android.R.drawable.ic_media_pause
            } else {
                android.R.drawable.ic_media_play
            }

            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(song.title)
                .setContentText(song.artist)
                .setSmallIcon(R.drawable.ic_music_note)
                .addAction(android.R.drawable.ic_media_previous, "Previous",
                    createPendingIntent(ACTION_PREVIOUS))
                .addAction(playPauseIcon, "Play/Pause",
                    createPendingIntent(ACTION_PLAY_PAUSE))
                .addAction(android.R.drawable.ic_media_next, "Next",
                    createPendingIntent(ACTION_NEXT))
                .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2))
                .build()

            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, MusicService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release media player
        mediaPlayer?.release()
        mediaPlayer = null
        
        // Abandon audio focus
        audioManager?.let { am ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { request ->
                    am.abandonAudioFocusRequest(request)
                }
            } else {
                @Suppress("DEPRECATION")
                am.abandonAudioFocus(audioFocusChangeListener)
            }
        }
        
        // Stop foreground service and remove notification
        stopForeground(true)
        stopSelf()
    }
}