package com.example.gallery.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaMetadataRetriever
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.media3.common.MediaItem
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.gallery.databinding.ItemVideoBinding

class TimelineAdapter(private val videoList: List<MediaItem>, private val context: Context) :
    ListAdapter<MediaItem, TimelineAdapter.TimelineViewHolder>(VideoDiffCallback()) {

    private var onItemClickListener: OnItemClickListener? = null
    private var onItemLongClickListener: OnItemLongClickListener? = null
    private var isSelectionMode = false
    private var selectedPosition: Int? = null
    private var selectedItems = mutableSetOf<Int>()

    class TimelineViewHolder(val binding: ItemVideoBinding) : RecyclerView.ViewHolder(binding.root)

    init {
        submitList(videoList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        val binding = ItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TimelineViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        val mediaItem = getItem(position)
        val videoUri = mediaItem.localConfiguration?.uri
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, videoUri)
        val videoDuration =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
        val minutes = videoDuration?.div(1000)?.div(60)
        val seconds = videoDuration?.div(1000)?.rem(60)
        if (minutes != null && seconds != null) {
            if (minutes < 10 && seconds < 10)
                holder.binding.itemVideoDuration.text = "0$minutes:0$seconds"
            else if (minutes < 10)
                holder.binding.itemVideoDuration.text = "0$minutes:$seconds"
            else if (seconds < 10)
                holder.binding.itemVideoDuration.text = "$minutes:0$seconds"
            else
                holder.binding.itemVideoDuration.text = "$minutes:$seconds"
        }

        Glide.with(holder.itemView.context)
            .load(videoUri)
            .apply(RequestOptions().frame(1000000)) // load frame at 1 second (adjust time as needed)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(holder.binding.itemVideo)

        holder.itemView.setOnClickListener {
            Log.d("TimelineAdapter", "Item clicked at position $position")

            onItemClickListener?.onItemClick(position)
        }
        // Update CheckBox visibility and state based on selection mode
        holder.binding.itemVideoCheckbox.visibility =
            if (isSelectionMode) View.VISIBLE else View.GONE
        holder.binding.itemVideoCheckbox.isChecked = selectedItems.contains(position)

        // Handle click events
        holder.itemView.setOnClickListener {
            if (isSelectionMode) {
                if (selectedItems.contains(position)) {
                    selectedItems.remove(position)
                } else {
                    selectedItems.add(position)
                }
                notifyItemChanged(position)
            } else {
                onItemClickListener?.onItemClick(position)
            }
        }

        holder.itemView.setOnLongClickListener {
            if (isSelectionMode) notifyItemChanged(position) else notifyDataSetChanged()
            isSelectionMode = true
            selectedItems.add(position)
            true
        }
//        holder.binding.itemVideo.setOnClickListener {
//            // Open video player activity or fragment and pass video URL
//            val intent = Intent(context, MainActivity::class.java)
//            intent.putExtra("videoUrl", videoUri.toString())
//            context.startActivity(intent)
//        }
//
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(position: Int)
    }

    fun setOnItemLongClickListener(listener: OnItemLongClickListener) {
        this.onItemLongClickListener = listener
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
