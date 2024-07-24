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
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            loadVideos()
        }
    }

    init {
        loadVideos()
        application.contentResolver.registerContentObserver(
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

        getApplication<Application>().contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val duration = cursor.getLong(durationColumn)
                val size = cursor.getLong(sizeColumn)
                val date = cursor.getLong(dateAddedColumn) * 1000L

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
