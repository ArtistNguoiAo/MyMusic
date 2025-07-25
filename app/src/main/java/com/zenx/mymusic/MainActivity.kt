package com.zenx.mymusic

import com.zenx.mymusic.model.MusicModel
import MusicPresenter
import com.zenx.mymusic.model.Song
import com.zenx.mymusic.adapter.SongsAdapter
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zenx.mymusic.contract.MusicContract

class MainActivity : AppCompatActivity(), MusicContract.View {

    private lateinit var presenter: MusicContract.Presenter
    private lateinit var songsAdapter: SongsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var miniPlayer: View
    private lateinit var songTitle: TextView
    private lateinit var artistName: TextView
    private lateinit var playPauseButton: ImageButton

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupRecyclerView()

        val model = MusicModel(this)
        presenter = MusicPresenter(this, model)
        presenter.attachView(this)

        checkPermissionAndLoadSongs()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewSongs)
        miniPlayer = findViewById(R.id.miniPlayer)
        songTitle = findViewById(R.id.textViewSongTitle)
        artistName = findViewById(R.id.textViewArtist)
        playPauseButton = findViewById(R.id.buttonPlayPause)

        findViewById<ImageButton>(R.id.buttonPlayPause).setOnClickListener {
            presenter.playPause()
        }

        findViewById<ImageButton>(R.id.buttonNext).setOnClickListener {
            presenter.nextSong()
        }

        findViewById<ImageButton>(R.id.buttonPrevious).setOnClickListener {
            presenter.previousSong()
        }
    }

    private fun setupRecyclerView() {
        songsAdapter = SongsAdapter { song ->
            presenter.playSong(song)
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = songsAdapter
        }
    }

    private fun checkPermissionAndLoadSongs() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        } else {
            presenter.loadSongs()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                presenter.loadSongs()
            } else {
                Toast.makeText(this, "Permission required to access music files", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun showSongs(songs: List<Song>) {
        songsAdapter.updateSongs(songs)
    }

    override fun showCurrentSong(song: Song) {
        miniPlayer.visibility = View.VISIBLE
        songTitle.text = song.title
        artistName.text = song.artist
    }

    override fun updatePlaybackState(isPlaying: Boolean) {
        val iconRes = if (isPlaying) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }
        playPauseButton.setImageResource(iconRes)
    }

    override fun updateProgress(progress: Int, duration: Int) {
        // Update progress bar if you add one
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
        presenter.onDestroy()
    }
}