package com.example.gallery.adapter

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.media3.common.MediaItem
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.gallery.MainActivity
import com.example.gallery.R
import com.example.gallery.TimelineItem
import com.example.gallery.databinding.ItemHeaderBinding
import com.example.gallery.databinding.ItemVideoBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TimelineAdapter(private val context: Context) :
    ListAdapter<TimelineItem, RecyclerView.ViewHolder>(TimelineDiffCallBack()) {

    private var onItemClickListener: OnItemClickListener? = null
    private var onItemLongClickListener: OnItemLongClickListener? = null
    private var onSelectionChangedListener: OnSelectionChangedListener? = null
    var isSelectionMode = false
    val selectedItems = mutableSetOf<Int>()
    val viewHolders = mutableListOf<RecyclerView.ViewHolder>()
    private val retriever = MediaMetadataRetriever()
//    private val videoDurationCache = mutableMapOf<Uri, Pair<Long, Long>>()
    val videoDurationCache = object : LruCache<Uri, Pair<Long, Long>>(100) {
        override fun sizeOf(key: Uri, value: Pair<Long, Long>): Int {
            // The cache size will be the number of entries
            return 1
        }
    }
    companion object {
        const val VIEW_TYPE_VIDEO = 0
        const val VIEW_TYPE_HEADER = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is TimelineItem.Header -> VIEW_TYPE_HEADER
            is TimelineItem.VideoItem -> VIEW_TYPE_VIDEO
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    inner class VideoViewHolder(val binding: ItemVideoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(mediaItem: MediaItem) {
            val videoUri = mediaItem.localConfiguration?.uri ?: return

            if (binding.root.tag != videoUri) {
                binding.root.tag = videoUri

                CoroutineScope(Dispatchers.IO).launch {
                    // Get duration from LruCache or retrieve and store in LruCache if not present
                    val durationPair = videoDurationCache[videoUri] ?: run {
                        retriever.setDataSource(context, videoUri)
                        val videoDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                            ?.toLongOrNull()
                        val minutes = videoDuration?.div(1000)?.div(60) ?: 0
                        val seconds = videoDuration?.div(1000)?.rem(60) ?: 0
                        Pair(minutes, seconds).also { videoDurationCache.put(videoUri, it) }
                    }

                    withContext(Dispatchers.Main) {
                        // Set the video duration text
                        binding.itemVideoDuration.text = String.format("%02d:%02d", durationPair.first, durationPair.second)

                        // Use Glide to load the video thumbnail
                        Glide.with(context)
                            .load(videoUri)
                            .apply(RequestOptions().frame(1000000))
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .into(binding.itemVideo)
                    }
                }
            }

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
                        selectedItems.contains(bindingAdapterPosition)
                    updateSelectionCount()
                    updateHeaderCheckboxOnItemSelection(bindingAdapterPosition)
                } else {
                    onItemClickListener?.onItemClick(bindingAdapterPosition)
                }
            }

            binding.itemVideoCheckbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedItems.add(bindingAdapterPosition)
                } else {
                    selectedItems.remove(bindingAdapterPosition)
                }
                updateSelectionCount()
                updateHeaderCheckboxOnItemSelection(bindingAdapterPosition)
            }

            binding.root.setOnLongClickListener {
                if (!isSelectionMode) {
                    isSelectionMode = true
                    toggleCheckboxVisibility(true)
                }
                selectedItems.add(bindingAdapterPosition)
                binding.itemVideoCheckbox.isChecked = true
                updateSelectionCount()
                updateHeaderCheckboxOnItemSelection(bindingAdapterPosition)
                true
            }

        }
    }

    inner class HeaderViewHolder(val binding: ItemHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String) {
            binding.itemHeaderText.text = title
            binding.itemHeaderCheckbox.isChecked = areAllItemsSelected()
            setCheckBoxHeader(isSelectionMode)

            binding.itemHeaderCheckbox.setOnClickListener {
                if (isSelectionMode) {
                    val isChecked = binding.itemHeaderCheckbox.isChecked
                    toggleSelectionForAllItems(isChecked, bindingAdapterPosition)
                    updateSelectionCount()
                }
            }
        }

        private fun areAllItemsSelected(): Boolean {
            val headerPosition = bindingAdapterPosition
            var position = headerPosition + 1
            while (position < itemCount && getItemViewType(position) == VIEW_TYPE_VIDEO) {
                if (!selectedItems.contains(position)) {
                    return false
                }
                position++
            }
            return true
        }

        fun setCheckBoxHeader(visible: Boolean) {
            binding.itemHeaderCheckbox.visibility = if (visible) View.VISIBLE else View.GONE
        }

        fun updateHeaderCheckbox() {
            binding.itemHeaderCheckbox.isChecked = areAllItemsSelected()
        }
    }

    fun toggleSelectionForAllItems(select: Boolean, headerPosition: Int) {
        var position = headerPosition + 1
        while (position < itemCount && getItemViewType(position) == VIEW_TYPE_VIDEO) {
            if (select) {
                selectedItems.add(position)
            } else {
                selectedItems.remove(position)
            }
            viewHolders.forEach { viewHolder ->
                if (viewHolder is VideoViewHolder && viewHolder.bindingAdapterPosition == position) {
                    viewHolder.binding.itemVideoCheckbox.isChecked = select
                }
            }
            position++
        }
        viewHolders.forEach { viewHolder ->
            if (viewHolder is HeaderViewHolder && viewHolder.bindingAdapterPosition == headerPosition) {
                viewHolder.updateHeaderCheckbox()
            }
        }
    }

    fun updateSelectionCount() {
        onSelectionChangedListener?.onSelectionChanged(selectedItems.size)
    }

    fun toggleCheckboxVisibility(visible: Boolean) {
        viewHolders.forEach { viewHolder ->
            if (viewHolder is VideoViewHolder) {
                viewHolder.binding.itemVideoCheckbox.visibility =
                    if (visible) View.VISIBLE else View.GONE
            }
            if (viewHolder is HeaderViewHolder) {
                viewHolder.setCheckBoxHeader(visible)
            }
        }
    }

    fun updateHeaderCheckboxOnItemSelection(videoPosition: Int) {
        viewHolders.forEach { viewHolder ->
            if (viewHolder is HeaderViewHolder) {
                if (viewHolder.bindingAdapterPosition < videoPosition) {
                    viewHolder.updateHeaderCheckbox()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding =
                    ItemHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                HeaderViewHolder(binding).also { viewHolders.add(it) }
            }

            VIEW_TYPE_VIDEO -> {
                val binding =
                    ItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                VideoViewHolder(binding).also { viewHolders.add(it) }
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

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    fun setOnItemLongClickListener(listener: OnItemLongClickListener) {
        this.onItemLongClickListener = listener
    }

    fun setOnSelectionChangedListener(listener: OnSelectionChangedListener) {
        this.onSelectionChangedListener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(position: Int)
    }

    interface OnSelectionChangedListener {
        fun onSelectionChanged(selectedCount: Int)
    }

    fun exitSelectionMode() {
        isSelectionMode = false
        selectedItems.clear()
        toggleCheckboxVisibility(false)
        viewHolders.forEach{ viewHolder ->
            if (viewHolder is HeaderViewHolder){
                toggleSelectionForAllItems(false, viewHolder.bindingAdapterPosition)
            }
        }
        updateSelectionCount()
    }

    class TimelineDiffCallBack : DiffUtil.ItemCallback<TimelineItem>() {
        override fun areItemsTheSame(oldItem: TimelineItem, newItem: TimelineItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: TimelineItem, newItem: TimelineItem): Boolean {
            return oldItem == newItem
        }
    }
}

