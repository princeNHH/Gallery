package com.example.gallery.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.VideoView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.MediaItem
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.gallery.R
import com.example.gallery.TimelineItem
import com.example.gallery.fragment.ViewPagerFragment

class VideoPagerAdapter(private val videoUris: List<Uri>) : RecyclerView.Adapter<VideoPagerAdapter.VideoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video_view_pager, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(videoUris[position])
    }

    override fun getItemCount(): Int = videoUris.size

    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val videoView: VideoView = itemView.findViewById(R.id.player_view)

        fun bind(uri: Uri) {
            videoView.setVideoURI(uri)
            videoView.setOnCompletionListener { mp -> mp.start() }
            videoView.setOnPreparedListener { mp ->
                val videoWidth = mp.videoWidth
                val videoHeight = mp.videoHeight
                val videoProportion = videoWidth.toFloat() / videoHeight.toFloat()

                val screenWidth = itemView.width
                val screenHeight = itemView.height
                val screenProportion = screenWidth.toFloat() / screenHeight.toFloat()

                val lp = videoView.layoutParams

                if (videoProportion > screenProportion) {
                    lp.width = screenWidth
                    lp.height = (screenWidth / videoProportion).toInt()
                } else {
                    lp.width = (screenHeight * videoProportion).toInt()
                    lp.height = screenHeight
                }
                videoView.layoutParams = lp
                videoView.start()
            }
        }
    }
}
