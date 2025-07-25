package com.zenx.mymusic.adapter

import com.zenx.mymusic.model.Song
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zenx.mymusic.R

class SongsAdapter(private val onSongClick: (Song) -> Unit) : RecyclerView.Adapter<SongsAdapter.SongViewHolder>() {
    
    private var songs = listOf<Song>()
    
    fun updateSongs(newSongs: List<Song>) {
        songs = newSongs
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(songs[position])
    }
    
    override fun getItemCount(): Int = songs.size
    
    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.textViewTitle)
        private val artistTextView: TextView = itemView.findViewById(R.id.textViewArtist)
        private val durationTextView: TextView = itemView.findViewById(R.id.textViewDuration)
        
        fun bind(song: Song) {
            titleTextView.text = song.title
            artistTextView.text = song.artist
            durationTextView.text = formatDuration(song.duration)
            
            itemView.setOnClickListener {
                onSongClick(song)
            }
        }
        
        private fun formatDuration(duration: Long): String {
            val minutes = (duration / 1000) / 60
            val seconds = (duration / 1000) % 60
            return String.format("%d:%02d", minutes, seconds)
        }
    }
}