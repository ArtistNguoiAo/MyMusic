import android.app.*
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.zenx.mymusic.R

class MusicService : Service(), MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {

    private var mediaPlayer: MediaPlayer? = null
    private var currentSong: Song? = null
    private var playlist: Playlist? = null
    private val binder = MusicBinder()

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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
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
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(song.data)
                setOnCompletionListener(this@MusicService)
                setOnPreparedListener(this@MusicService)
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
        mediaPlayer?.release()
        mediaPlayer = null
    }
}