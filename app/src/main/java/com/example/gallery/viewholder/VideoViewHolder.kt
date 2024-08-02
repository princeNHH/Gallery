package com.example.gallery.viewholder

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.LruCache
import android.view.View
import androidx.media3.common.MediaItem
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
    private val retriever: MediaMetadataRetriever,
    private val videoDurationCache: LruCache<Uri, Pair<Long, Long>>,
    private val adapter: TimelineAdapter
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(mediaItem: MediaItem) {
        val videoUri = mediaItem.localConfiguration?.uri ?: return

        if (binding.root.tag != videoUri) {
            binding.root.tag = videoUri

            CoroutineScope(Dispatchers.IO).launch {
                val durationPair = videoDurationCache[videoUri] ?: run {
                    retriever.setDataSource(context, videoUri)
                    val videoDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        ?.toLongOrNull()
                    val minutes = videoDuration?.div(1000)?.div(60) ?: 0
                    val seconds = videoDuration?.div(1000)?.rem(60) ?: 0
                    Pair(minutes, seconds).also { videoDurationCache.put(videoUri, it) }
                }

                withContext(Dispatchers.Main) {
                    binding.itemVideoDuration.text = String.format("%02d:%02d", durationPair.first, durationPair.second)

                    Glide.with(context)
                        .load(videoUri)
                        .apply(RequestOptions().frame(1000000))
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .into(binding.itemVideo)
                }
            }
        }

        binding.itemVideoCheckbox.visibility = if (adapter.isSelectionMode) View.VISIBLE else View.GONE
        binding.itemVideoCheckbox.isChecked = adapter.selectedItems.contains(bindingAdapterPosition)

        binding.root.setOnClickListener {
            if (adapter.isSelectionMode) {
                if (adapter.selectedItems.contains(bindingAdapterPosition)) {
                    adapter.selectedItems.remove(bindingAdapterPosition)
                } else {
                    adapter.selectedItems.add(bindingAdapterPosition)
                }
                binding.itemVideoCheckbox.isChecked = adapter.selectedItems.contains(bindingAdapterPosition)
                adapter.updateSelectionCount()
                adapter.updateHeaderCheckboxOnItemSelection(bindingAdapterPosition)
            } else {
                adapter.onItemClickListener?.onItemClick(bindingAdapterPosition)
            }
        }

        binding.itemVideoCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                adapter.selectedItems.add(bindingAdapterPosition)
            } else {
                adapter.selectedItems.remove(bindingAdapterPosition)
            }
            adapter.updateSelectionCount()
            adapter.updateHeaderCheckboxOnItemSelection(bindingAdapterPosition)
        }

        binding.root.setOnLongClickListener {
            if (!adapter.isSelectionMode) {
                adapter.isSelectionMode = true
                adapter.toggleCheckboxVisibility(true)
            }
            adapter.selectedItems.add(bindingAdapterPosition)
            binding.itemVideoCheckbox.isChecked = true
            adapter.updateSelectionCount()
            adapter.updateHeaderCheckboxOnItemSelection(bindingAdapterPosition)
            true
        }
    }
}
