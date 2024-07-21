package com.example.gallery.viewmodel

import android.app.Application
import android.content.ContentUris
import android.database.ContentObserver
import android.database.Cursor
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.MediaItem

class VideoViewModel(application: Application) : AndroidViewModel(application) {

    private val _listVideo = MutableLiveData<List<MediaItem>>()
    val listVideo: LiveData<List<MediaItem>> get() = _listVideo

    private val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(self: Boolean) {
            super.onChange(self)
            loadVideos() // Reload videos when media store changes
        }
    }

    init {
        loadVideos()
        getApplication<Application>().contentResolver.registerContentObserver(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            true,
            contentObserver
        )
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().contentResolver.unregisterContentObserver(contentObserver)
    }

    fun addVideo(video: MediaItem) {
        val currentList = _listVideo.value?.toMutableList() ?: mutableListOf()
        currentList.add(video)
        _listVideo.value = currentList
    }

    fun removeVideo(video: MediaItem) {
        val currentList = _listVideo.value?.toMutableList() ?: mutableListOf()
        currentList.remove(video)
        _listVideo.value = currentList
    }

    fun updateVideo(updatedVideo: MediaItem) {
        val currentList = _listVideo.value?.toMutableList() ?: mutableListOf()
        val index = currentList.indexOfFirst { it.mediaId == updatedVideo.mediaId }
        if (index != -1) {
            currentList[index] = updatedVideo
            _listVideo.value = currentList
        }
    }

    fun clearVideos() {
        _listVideo.value = emptyList()
    }

    fun loadVideos() {
        val listVideo = mutableListOf<MediaItem>()
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE
        )

        val cursor: Cursor? = getApplication<Application>().contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val name = it.getString(nameColumn)
                val duration = it.getLong(durationColumn)
                val size = it.getLong(sizeColumn)

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                val mediaItem = MediaItem.Builder()
                    .setMediaId(id.toString())
                    .setUri(contentUri)
                    .build()
                listVideo.add(mediaItem)
            }
        }
        _listVideo.postValue(listVideo)
    }

}
