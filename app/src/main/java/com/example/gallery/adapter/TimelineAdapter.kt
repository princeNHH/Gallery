package com.example.gallery.adapter

import android.media.browse.MediaBrowser
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gallery.databinding.ItemVideoBinding

class TimelineAdapter(private var videoList: List<MediaBrowser.MediaItem>): RecyclerView.Adapter<TimelineAdapter.TimelineViewModel>() {
    class TimelineViewModel(var itemVideoBinding: ItemVideoBinding) : RecyclerView.ViewHolder(itemVideoBinding.root){
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewModel {
        val itemVideoBinding = ItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TimelineViewModel(itemVideoBinding)
    }

    override fun getItemCount(): Int {
        return videoList.size
    }

    override fun onBindViewHolder(holder: TimelineViewModel, position: Int) {
        val video = videoList[position]
       // holder.itemVideoBinding.videoItem.setImageResource(video.mediaId.toInt())
    }
}