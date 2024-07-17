package com.example.gallery.adapter

import android.content.Context
import android.content.Intent
import android.media.browse.MediaBrowser
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.gallery.MainActivity
import com.example.gallery.R
import com.example.gallery.databinding.ItemVideoBinding

class TimelineAdapter(private val context: Context, private var videoList: List<MediaItem>): RecyclerView.Adapter<TimelineAdapter.TimelineViewModel>() {
    class TimelineViewModel(var itemVideoBinding: ItemVideoBinding) : RecyclerView.ViewHolder(itemVideoBinding.root){
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewModel {
        val itemVideoBinding = ItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TimelineViewModel(itemVideoBinding)
    }

    override fun getItemCount(): Int {
        return videoList.size
    }

    @OptIn(UnstableApi::class)
    override fun onBindViewHolder(holder: TimelineViewModel, position: Int) {
        val mediaItem = videoList[position]
        val videoUri = mediaItem.playbackProperties?.uri.toString()

        Glide.with(holder.itemView.context)
            .load(videoUri)
            .apply(RequestOptions().frame(1000000)) // load frame at 1 second (adjust time as needed)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(holder.itemVideoBinding.itemVideo)

        holder.itemVideoBinding.itemVideo.setOnClickListener {
            // Open video player activity or fragment and pass video URL
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("videoUrl", videoUri)
            context.startActivity(intent)
        }
    }
}