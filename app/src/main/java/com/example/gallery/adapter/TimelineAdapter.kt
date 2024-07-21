package com.example.gallery.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.gallery.MainActivity
import com.example.gallery.databinding.ItemVideoBinding

class TimelineAdapter(private val videoList: List<MediaItem>, private val context: Context) :
    ListAdapter<MediaItem, TimelineAdapter.TimelineViewHolder>(VideoDiffCallback()) {

    class TimelineViewHolder(val binding: ItemVideoBinding) : RecyclerView.ViewHolder(binding.root)

    init {
        submitList(videoList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        val binding = ItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TimelineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        val mediaItem = getItem(position)
        val videoUri = mediaItem.localConfiguration?.uri

        Glide.with(holder.itemView.context)
            .load(videoUri)
            .apply(RequestOptions().frame(1000000)) // load frame at 1 second (adjust time as needed)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(holder.binding.itemVideo)

        holder.binding.itemVideo.setOnClickListener {
            // Open video player activity or fragment and pass video URL
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("videoUrl", videoUri.toString())
            context.startActivity(intent)
        }
    }

    class VideoDiffCallback : DiffUtil.ItemCallback<MediaItem>() {
        override fun areItemsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean {
            return oldItem.mediaId == newItem.mediaId // Assuming MediaItem has mediaId or a similar unique identifier
        }

        override fun areContentsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean {
            return oldItem == newItem
        }
    }
}
