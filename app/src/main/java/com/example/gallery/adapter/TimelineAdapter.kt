package com.example.gallery.adapter

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gallery.TimelineItem
import com.example.gallery.databinding.ItemHeaderBinding
import com.example.gallery.databinding.ItemVideoBinding
import android.util.LruCache
import android.view.View
import androidx.media3.common.MediaItem
import com.example.gallery.viewholder.HeaderViewHolder
import com.example.gallery.viewholder.VideoViewHolder

class TimelineAdapter(private val context: Context) :
    ListAdapter<TimelineItem, RecyclerView.ViewHolder>(TimelineDiffCallBack()) {

    var onItemClickListener: OnItemClickListener? = null
    private var onItemLongClickListener: OnItemLongClickListener? = null
    private var onSelectionChangedListener: OnSelectionChangedListener? = null
    var isSelectionMode = false
    val selectedItems = mutableSetOf<Uri>()
    private val viewHolders = mutableListOf<RecyclerView.ViewHolder>()
    private val retriever = MediaMetadataRetriever()

    private val videoDurationCache = object : LruCache<Uri, Pair<Long, Long>>(100) {
        override fun sizeOf(key: Uri, value: Pair<Long, Long>): Int {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding =
                    ItemHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                HeaderViewHolder(binding, this).also { viewHolders.add(it) }
            }

            VIEW_TYPE_VIDEO -> {
                val binding =
                    ItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                VideoViewHolder(context, binding, retriever, videoDurationCache, this).also { viewHolders.add(it) }
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

    fun updateHeaderCheckboxOnItemSelection(videoPosition: Int) {
        viewHolders.forEach { viewHolder ->
            if (viewHolder is HeaderViewHolder) {
                if (viewHolder.bindingAdapterPosition < videoPosition) {
                    viewHolder.updateHeaderCheckbox()
                }
            }
        }
    }

    fun updateSelectionCount() {
        onSelectionChangedListener?.onSelectionChanged(selectedItems.size)
    }

    fun toggleSelectionForAllItems(select: Boolean, headerPosition: Int) {
        var position = headerPosition + 1
        while (position < itemCount && getItemViewType(position) == VIEW_TYPE_VIDEO) {
            val videoUri = (getItem(position) as TimelineItem.VideoItem).mediaItem.localConfiguration?.uri
            videoUri?.let {
                if (select) {
                    selectedItems.add(it)
                } else {
                    selectedItems.remove(it)
                }
                viewHolders.forEach { viewHolder ->
                    if (viewHolder is VideoViewHolder && viewHolder.bindingAdapterPosition == position) {
                        viewHolder.binding.itemVideoCheckbox.isChecked = select
                    }
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

    fun exitSelectionMode() {
        isSelectionMode = false
        selectedItems.clear()
        toggleCheckboxVisibility(false)
        viewHolders.forEach { viewHolder ->
            if (viewHolder is HeaderViewHolder) {
                toggleSelectionForAllItems(false, viewHolder.bindingAdapterPosition)
            }
        }
        updateSelectionCount()
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

    fun areAllVideoSelected(headerPosition: Int): Boolean {
        var position = headerPosition + 1
        while (position < itemCount && getItemViewType(position) == VIEW_TYPE_VIDEO) {
            val videoUri = (getItem(position) as TimelineItem.VideoItem).mediaItem.localConfiguration?.uri
            if (videoUri != null && !selectedItems.contains(videoUri)) {
                return false
            }
            position++
        }
        return true
    }
    fun registerOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    fun registerOnItemLongClickListener(listener: OnItemLongClickListener) {
        this.onItemLongClickListener = listener
    }

    fun registerOnSelectionChangedListener(listener: OnSelectionChangedListener) {
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

    class TimelineDiffCallBack : DiffUtil.ItemCallback<TimelineItem>() {
        override fun areItemsTheSame(oldItem: TimelineItem, newItem: TimelineItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: TimelineItem, newItem: TimelineItem): Boolean {
            return oldItem == newItem
        }
    }
}
