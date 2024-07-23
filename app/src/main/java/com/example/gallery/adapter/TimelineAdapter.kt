package com.example.gallery.adapter

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.media3.common.MediaItem
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.gallery.TimelineItem
import com.example.gallery.databinding.ItemHeaderBinding
import com.example.gallery.databinding.ItemVideoBinding

class TimelineAdapter(private val context: Context) :
    ListAdapter<TimelineItem, RecyclerView.ViewHolder>(TimelineDiffCallBack()) {

    private var onItemClickListener: OnItemClickListener? = null
    private var onItemLongClickListener: OnItemLongClickListener? = null
    private var isSelectionMode = false
    private val selectedItems = mutableSetOf<Int>()
    private val viewHolders = mutableListOf<RecyclerView.ViewHolder>()
    private val retriever = MediaMetadataRetriever()
    private val videoDurationCache = mutableMapOf<Uri, Pair<Long, Long>>() // Cache video duration

    companion object {
        private const val VIEW_TYPE_VIDEO = 0
        const val VIEW_TYPE_HEADER = 1

    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is TimelineItem.Header -> VIEW_TYPE_HEADER
            is TimelineItem.VideoItem -> VIEW_TYPE_VIDEO
        }
    }

    inner class VideoViewHolder(val binding: ItemVideoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(mediaItem: MediaItem) {
            val videoUri = mediaItem.localConfiguration?.uri

            // Chỉ cập nhật lại giao diện nếu videoUri thay đổi
            if (binding.root.tag != videoUri) {
                binding.root.tag = videoUri

                val durationPair = videoDurationCache[videoUri] ?: run {
                    retriever.setDataSource(binding.root.context, videoUri)
                    val videoDuration =
                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                            ?.toLongOrNull()
                    val minutes = videoDuration?.let { it / 1000 / 60 } ?: 0
                    val seconds = videoDuration?.let { it / 1000 % 60 } ?: 0
                    val pair = Pair(minutes, seconds)
                    videoDurationCache[videoUri!!] = pair // Cache the duration
                    pair
                }

                binding.itemVideoDuration.text =
                    String.format("%02d:%02d", durationPair.first, durationPair.second)

                Glide.with(binding.root.context).load(videoUri)
                    .apply(RequestOptions().frame(1000000)) // load frame tại 1 giây (điều chỉnh thời gian nếu cần)
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Dùng cache cho Glide
                    .into(binding.itemVideo)
            }

            // Cập nhật trạng thái checkbox và visibility
            binding.itemVideoCheckbox.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
            binding.itemVideoCheckbox.isChecked = selectedItems.contains(bindingAdapterPosition)

            binding.root.setOnClickListener {
                if (isSelectionMode) {
                    if (selectedItems.contains(bindingAdapterPosition)) {
                        selectedItems.remove(bindingAdapterPosition)
                    } else {
                        selectedItems.add(bindingAdapterPosition)
                    }
                    binding.itemVideoCheckbox.isChecked =
                        selectedItems.contains(bindingAdapterPosition) // Cập nhật checkbox trực tiếp
                } else {
                    onItemClickListener?.onItemClick(bindingAdapterPosition)
                }
            }

            binding.root.setOnLongClickListener {
                if (!isSelectionMode) {
                    isSelectionMode = true
                    toggleCheckboxVisibility(true) // Hiển thị tất cả checkbox
                }
                selectedItems.add(bindingAdapterPosition)
                binding.itemVideoCheckbox.isChecked = true
                true
            }
        }
    }

    inner class HeaderViewHolder(val binding: ItemHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String) {
            binding.itemHeaderText.text = title
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding =
                    ItemHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                HeaderViewHolder(binding)
            }

            VIEW_TYPE_VIDEO -> {
                val binding =
                    ItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return VideoViewHolder(binding).also { viewHolder ->
                    viewHolders.add(viewHolder) // Thêm vào danh sách view holders
                }
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.bind((getItem(position) as TimelineItem.Header).title)
            is VideoViewHolder -> holder.bind((getItem(position) as TimelineItem.VideoItem).mediaItem)
        }
    }

    private fun toggleCheckboxVisibility(visible: Boolean) {
        viewHolders.forEach { viewHolder ->
            if (viewHolder is VideoViewHolder) {
                viewHolder.binding.itemVideoCheckbox.visibility =
                    if (visible) View.VISIBLE else View.GONE
            }
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

    class TimelineDiffCallBack() : DiffUtil.ItemCallback<TimelineItem>() {
        override fun areItemsTheSame(oldItem: TimelineItem, newItem: TimelineItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: TimelineItem, newItem: TimelineItem): Boolean {
            return oldItem == newItem
        }
    }
}
