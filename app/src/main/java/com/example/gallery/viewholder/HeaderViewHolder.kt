package com.example.gallery.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.gallery.adapter.TimelineAdapter
import com.example.gallery.databinding.ItemHeaderBinding

class HeaderViewHolder(
    private val binding: ItemHeaderBinding,
    private val adapter: TimelineAdapter
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(title: String) {
        binding.itemHeaderText.text = title
        binding.itemHeaderCheckbox.isChecked = areAllItemsSelected()
        setCheckBoxHeader(adapter.isSelectionMode)

        binding.itemHeaderCheckbox.setOnClickListener {
            if (adapter.isSelectionMode) {
                val isChecked = binding.itemHeaderCheckbox.isChecked
                adapter.toggleSelectionForAllItems(isChecked, bindingAdapterPosition)
                adapter.updateSelectionCount()
            }
        }
    }

    private fun areAllItemsSelected(): Boolean {
        val headerPosition = bindingAdapterPosition
        var position = headerPosition + 1
        while (position < adapter.itemCount && adapter.getItemViewType(position) == TimelineAdapter.VIEW_TYPE_VIDEO) {
            if (!adapter.selectedItems.contains(position)) {
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
