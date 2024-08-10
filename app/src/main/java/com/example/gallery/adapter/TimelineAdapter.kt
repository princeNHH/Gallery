package com.example.gallery.adapter

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gallery.R
import com.example.gallery.TimelineItem
import com.example.gallery.databinding.ItemHeaderBinding
import com.example.gallery.databinding.ItemVideoBinding
import com.example.gallery.helper.DragSelectReceiver
import com.example.gallery.viewholder.HeaderViewHolder
import com.example.gallery.viewholder.VideoViewHolder
import com.google.android.material.resources.MaterialResources.getDimensionPixelSize

class TimelineAdapter(private val context: Context) :
    ListAdapter<TimelineItem, RecyclerView.ViewHolder>(TimelineDiffCallBack()), DragSelectReceiver {

    var onItemClickListener: OnItemClickListener? = null
    var onItemLongClickListener: OnItemLongClickListener? = null
    private var onSelectionChangedListener: OnSelectionChangedListener? = null
    var isSelectionMode = false
    val selectedItems = mutableSetOf<Uri>()
    private val viewHolders = mutableListOf<RecyclerView.ViewHolder>()

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
        val spanCount = getSpanCountForOrientation(parent)
        val screenWidth = parent.context.resources.displayMetrics.widthPixels
        val itemSpacing = context.resources.getDimensionPixelSize(R.dimen.item_spacing)
        val itemSpacingHorizontal =
            context.resources.getDimensionPixelSize(R.dimen.rcv_margin_horizontal)
        val itemSize =
            ((screenWidth - spanCount * 2 * itemSpacing - 2 * itemSpacingHorizontal) / spanCount)

        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding =
                    ItemHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                HeaderViewHolder(binding, this).also { viewHolders.add(it) }
            }

            VIEW_TYPE_VIDEO -> {
                val binding =
                    ItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                binding.root.layoutParams = ViewGroup.LayoutParams(itemSize, itemSize)
                VideoViewHolder(
                    context,
                    binding,
                    this
                ).also { viewHolders.add(it) }
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    fun getSpanCountForOrientation(parent: ViewGroup): Int {
        return if (parent.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            6 // Số cột trong chế độ ngang
        } else {
            4 // Số cột trong chế độ dọc
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.bind((getItem(position) as TimelineItem.Header).title)
            is VideoViewHolder -> holder.bind((getItem(position) as TimelineItem.VideoItem).mediaItem)
        }
    }

    override fun onCurrentListChanged(
        previousList: List<TimelineItem>,
        currentList: List<TimelineItem>
    ) {
        super.onCurrentListChanged(previousList, currentList)
        updateHeaderCheckboxOnItemSelection()
    }

    fun updateHeaderCheckboxOnItemSelection() {
        viewHolders.forEach { viewHolder ->
            if (viewHolder is HeaderViewHolder) {
                viewHolder.updateHeaderCheckbox()
            }
        }
    }

    fun updateSelectionCount() {
        onSelectionChangedListener?.onSelectionChanged(selectedItems.size)
    }

    fun toggleSelectionForAllItems(select: Boolean, headerPosition: Int) {
        var position = headerPosition + 1
        while (position < itemCount && getItemViewType(position) == VIEW_TYPE_VIDEO) {
            val videoUri =
                (getItem(position) as TimelineItem.VideoItem).mediaItem.localConfiguration?.uri
            videoUri?.let {
                if (select) {
                    selectedItems.add(it)
                } else {
                    selectedItems.remove(it)
                }
                viewHolders.forEach { viewHolder ->
                    if (viewHolder is VideoViewHolder && viewHolder.bindingAdapterPosition == position) {
                        viewHolder.binding.itemVideoCheckbox.isChecked = select
                        viewHolder.binding.itemBlurFrame.visibility =
                            if (select) View.VISIBLE else View.GONE
                    }
                }
            }
            position++
        }
        updateHeaderCheckboxOnItemSelection()
    }

    fun exitSelectionMode() {
        isSelectionMode = false
        selectedItems.clear()
        toggleCheckboxVisibility(false)
        viewHolders.forEach { viewHolder ->
            if (viewHolder is HeaderViewHolder) {
                toggleSelectionForAllItems(false, viewHolder.bindingAdapterPosition)
            } else if (viewHolder is VideoViewHolder) {
                viewHolder.binding.itemBlurFrame.visibility = View.GONE
            }
        }
        updateSelectionCount()
    }

    fun toggleCheckboxVisibility(visible: Boolean) {
        viewHolders.forEach { viewHolder ->
            if (viewHolder is VideoViewHolder) {
                viewHolder.binding.itemVideoCheckbox.visibility =
                    if (visible) View.VISIBLE else View.GONE
                viewHolder.adapter.createBounceAnimator(viewHolder.binding.itemVideoCheckbox)
                    .start()
            }
            if (viewHolder is HeaderViewHolder) {
                viewHolder.setCheckBoxHeader(visible)
                viewHolder.adapter.createBounceAnimator(viewHolder.binding.itemHeaderCheckbox)
                    .start()
            }
        }
    }


    fun areAllVideoSelected(headerPosition: Int): Boolean {
        var position = headerPosition + 1
        while (position < itemCount) {
            if (getItemViewType(position) == VIEW_TYPE_HEADER) break;
            val videoUri =
                (getItem(position) as TimelineItem.VideoItem).mediaItem.localConfiguration?.uri
            if (videoUri != null && !selectedItems.contains(videoUri)) {
                return false
            }
            position++
        }
        return true
    }

    fun toggleSelection(videoUri: Uri, position: Int) {
        if (selectedItems.contains(videoUri)) {
            selectedItems.remove(videoUri)
        } else {
            selectedItems.add(videoUri)
        }
        notifyItemChanged(position)
        updateSelectionCount()
        updateHeaderCheckboxOnItemSelection()
    }

    fun getUriAtPosition(position: Int): Uri? {
        // Ensure the position is within bounds
        if (position in 0 until itemCount) {
            val item = currentList.getOrNull(position)
            if (item is TimelineItem.VideoItem) {
                return item.mediaItem.localConfiguration?.uri
            }
        }
        return null
    }


    fun createBounceAnimator(checkBox: CheckBox): AnimatorSet {
        val scaleX = ObjectAnimator.ofFloat(checkBox, "scaleX", 0.8f, 1.0f, 1.0f)
        val scaleY = ObjectAnimator.ofFloat(checkBox, "scaleY", 0.8f, 1.0f, 1.0f)
        val alpha = ObjectAnimator.ofFloat(checkBox, "alpha", 1.0f, 0.8f, 1.0f)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleX, scaleY, alpha)
        animatorSet.duration = 300

        return animatorSet
    }

    fun enterSelectionMode() {
        if (!isSelectionMode) {
            isSelectionMode = true
            toggleCheckboxVisibility(true)
        }
    }

    override fun setSelected(index: Int, selected: Boolean) {
        if (selected && !selectedItems.contains(getUriAtPosition(index))) {
            getUriAtPosition(index)?.let { selectedItems.add(it) }
        } else if (!selected && selectedItems.contains(getUriAtPosition(index))) {
            selectedItems.remove(getUriAtPosition(index))
        }
        updateSelectionCount()
        notifyItemChanged(index)
        updateHeaderCheckboxOnItemSelection()
    }

    override fun isSelected(index: Int): Boolean {
        return selectedItems.contains(getUriAtPosition(index))
    }

    override fun isIndexSelectable(index: Int): Boolean {
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
