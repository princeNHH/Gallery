package com.example.gallery.viewmodel

import android.app.Application
import android.content.ContentUris
import android.database.ContentObserver
import android.database.Cursor
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.annotation.OptIn
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import com.example.gallery.TimelineItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VideoViewModel(application: Application) : AndroidViewModel(application) {

    private val _timelineItems = MutableLiveData<List<TimelineItem>>()
    val timelineItems: LiveData<List<TimelineItem>> get() = _timelineItems

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

    @OptIn(UnstableApi::class)
    fun loadVideos() {
        val listVideo = mutableListOf<MediaItem>()
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_ADDED
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
            val dateAddedColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val name = it.getString(nameColumn)
                val duration = it.getLong(durationColumn)
                val size = it.getLong(sizeColumn)
                val date = it.getLong(dateAddedColumn)*1000L

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                val mediaItem = MediaItem.Builder()
                    .setMediaId(id.toString())
                    .setUri(contentUri)
                    .setImageDurationMs(duration)
                    .setTag(date)
                    .build()
                listVideo.add(mediaItem)
            }
        }
        _timelineItems.postValue(groupVideosByDate(listVideo))
    }
    private fun groupVideosByDate(listVideo: List<MediaItem>): List<TimelineItem> {
        val timelineItems = mutableListOf<TimelineItem>()
        val groupedVideos = listVideo.groupBy { mediaItem ->
            val date = Date(mediaItem.localConfiguration?.tag as Long)
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
        }

        groupedVideos.forEach { (date, videos) ->
            timelineItems.add(TimelineItem.Header(date))
            videos.forEach { video ->
                timelineItems.add(TimelineItem.VideoItem(video))
            }
        }

        return timelineItems
    }
}
