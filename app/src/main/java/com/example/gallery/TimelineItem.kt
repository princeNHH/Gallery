package com.example.gallery

import android.widget.CheckBox
import androidx.media3.common.MediaItem

sealed class TimelineItem {
    data class Header(val title: String) : TimelineItem()
    data class VideoItem(val mediaItem: MediaItem) : TimelineItem()
}