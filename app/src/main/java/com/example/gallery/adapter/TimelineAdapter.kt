package com.example.gallery.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
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
    private val selectedItems = mutableSetOf<Int>()
    private val viewHolders = mutableListOf<TimelineViewHolder>()
    private val retriever = MediaMetadataRetriever()
    private val videoDurationCache = mutableMapOf<Uri, Pair<Long, Long>>() // Cache video duration

    inner class TimelineViewHolder(val binding: ItemVideoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(mediaItem: MediaItem, position: Int) {
            val videoUri = mediaItem.localConfiguration?.uri

            // Chỉ cập nhật lại giao diện nếu videoUri thay đổi
            if (binding.root.tag != videoUri) {
                binding.root.tag = videoUri

                val durationPair = videoDurationCache[videoUri] ?: run {
                    retriever.setDataSource(binding.root.context, videoUri)
                    val videoDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
                    val minutes = videoDuration?.let { it / 1000 / 60 } ?: 0
                    val seconds = videoDuration?.let { it / 1000 % 60 } ?: 0
                    val pair = Pair(minutes, seconds)
                    videoDurationCache[videoUri!!] = pair // Cache the duration
                    pair
                }

                binding.itemVideoDuration.text = String.format("%02d:%02d", durationPair.first, durationPair.second)

                Glide.with(binding.root.context)
                    .load(videoUri)
                    .apply(RequestOptions().frame(1000000)) // load frame tại 1 giây (điều chỉnh thời gian nếu cần)
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Dùng cache cho Glide
                    .into(binding.itemVideo)
            }

            // Cập nhật trạng thái checkbox và visibility
            binding.itemVideoCheckbox.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
            binding.itemVideoCheckbox.isChecked = selectedItems.contains(position)

            binding.root.setOnClickListener {
                if (isSelectionMode) {
                    if (selectedItems.contains(position)) {
                        selectedItems.remove(position)
                    } else {
                        selectedItems.add(position)
                    }
                    binding.itemVideoCheckbox.isChecked = selectedItems.contains(position) // Cập nhật checkbox trực tiếp
                } else {
                    onItemClickListener?.onItemClick(position)
                }
            }

            binding.root.setOnLongClickListener {
                if (!isSelectionMode) {
                    isSelectionMode = true
                    toggleCheckboxVisibility(true) // Hiển thị tất cả checkbox
                }
                selectedItems.add(position)
                binding.itemVideoCheckbox.isChecked = true
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        val binding = ItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TimelineViewHolder(binding).also { viewHolder ->
            viewHolders.add(viewHolder) // Thêm vào danh sách view holders
        }
    }

    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    private fun toggleCheckboxVisibility(visible: Boolean) {
        viewHolders.forEach { viewHolder ->
            viewHolder.binding.itemVideoCheckbox.visibility = if (visible) View.VISIBLE else View.GONE
        }
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    fun setOnItemLongClickListener(listener: OnItemLongClickListener) {
        this.onItemLongClickListener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(position: Int)
    }

    class VideoDiffCallback : DiffUtil.ItemCallback<MediaItem>() {
        override fun areItemsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean {
            return oldItem.mediaId == newItem.mediaId
        }

        override fun areContentsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean {
            return oldItem == newItem
        }
    }
}
