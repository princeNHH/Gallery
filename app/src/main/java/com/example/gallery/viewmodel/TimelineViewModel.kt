package com.example.gallery.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TimelineViewModel : ViewModel() {
    private val _selectedItems = MutableLiveData<Set<Int>>(setOf())
    val selectedItems: LiveData<Set<Int>> get() = _selectedItems

    fun toggleSelection(position: Int) {
        _selectedItems.value = _selectedItems.value?.let {
            if (it.contains(position)) it - position else it + position
        }
    }

    fun setSelectionMode(isSelectionMode: Boolean) {
        if (!isSelectionMode) {
            _selectedItems.value = setOf()
        }
    }
}
