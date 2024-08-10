package com.example.gallery.viewholder

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.gallery.MainActivity
import com.example.gallery.R
import com.example.gallery.adapter.TimelineAdapter
import com.example.gallery.databinding.ItemVideoBinding

class VideoViewHolder(
    private val context: Context,
    val binding: ItemVideoBinding,
    val adapter: TimelineAdapter
) : RecyclerView.ViewHolder(binding.root) {

    @SuppressLint("DefaultLocale")
    @OptIn(UnstableApi::class)
    fun bind(mediaItem: MediaItem) {
        val videoUri = mediaItem.localConfiguration?.uri ?: return
        binding.root.setTag(R.id.item_video, videoUri)
        val videoDuration = mediaItem.localConfiguration?.imageDurationMs
        val minutes = videoDuration?.div(1000)?.div(60)
        val seconds = videoDuration?.div(1000)?.rem(60)

        binding.itemVideoDuration.text = String.format("%02d:%02d", minutes, seconds)

        Glide.with(context)
            .load(videoUri)
            .apply(RequestOptions().frame(0))
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(binding.itemVideo)

        binding.itemVideoCheckbox.visibility =
            if (adapter.isSelectionMode) View.VISIBLE else View.GONE
        binding.itemVideoCheckbox.setOnCheckedChangeListener(null)
        binding.itemVideoCheckbox.isChecked = adapter.selectedItems.contains(videoUri)
        binding.itemBlurFrame.visibility = if (adapter.selectedItems.contains(videoUri)) View.VISIBLE else View.GONE

        binding.root.setOnClickListener {
            if (adapter.isSelectionMode) {
                adapter.toggleSelection(videoUri, bindingAdapterPosition)
            } else {
                adapter.onItemClickListener?.onItemClick(bindingAdapterPosition)
            }
        }

        binding.itemVideoCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                adapter.selectedItems.add(videoUri)
            } else {
                adapter.selectedItems.remove(videoUri)
            }
            adapter.updateSelectionCount()
            adapter.updateHeaderCheckboxOnItemSelection()
            adapter.createBounceAnimator(binding.itemVideoCheckbox).start()
        }

        binding.root.setOnLongClickListener {
            if (!adapter.isSelectionMode) {
                adapter.onItemLongClickListener?.onItemLongClick(bindingAdapterPosition)
                adapter.enterSelectionMode()
            }
            adapter.selectedItems.add(videoUri)
            binding.itemVideoCheckbox.isChecked = true
            binding.itemBlurFrame.visibility = View.VISIBLE
            adapter.updateSelectionCount()
            adapter.updateHeaderCheckboxOnItemSelection()
            adapter.onItemLongClickListener?.onItemLongClick(bindingAdapterPosition)
            (context as MainActivity).binding.bottomBar.visibility = View.GONE
            true
        }
    }
}

