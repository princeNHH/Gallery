package com.example.gallery.viewholder

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import android.util.LruCache
import android.view.View
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.gallery.adapter.TimelineAdapter
import com.example.gallery.databinding.ItemVideoBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VideoViewHolder(
    private val context: Context,
    val binding: ItemVideoBinding,
    private val adapter: TimelineAdapter
) : RecyclerView.ViewHolder(binding.root) {

    @SuppressLint("DefaultLocale")
    @OptIn(UnstableApi::class)
    fun bind(mediaItem: MediaItem) {
        val videoUri = mediaItem.localConfiguration?.uri ?: return
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

        binding.root.setOnClickListener {
            if (adapter.isSelectionMode) {
                toggleSelection(videoUri)
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
                adapter.isSelectionMode = true
                adapter.toggleCheckboxVisibility(true)
            }
            adapter.selectedItems.add(videoUri)
            binding.itemVideoCheckbox.isChecked = true
            adapter.updateSelectionCount()
            adapter.updateHeaderCheckboxOnItemSelection()
            true
        }
    }

    private fun toggleSelection(videoUri: Uri) {
        if (adapter.selectedItems.contains(videoUri)) {
            adapter.selectedItems.remove(videoUri)
        } else {
            adapter.selectedItems.add(videoUri)
        }
        binding.itemVideoCheckbox.isChecked = adapter.selectedItems.contains(videoUri)
        adapter.updateSelectionCount()
        adapter.updateHeaderCheckboxOnItemSelection()
    }
}

