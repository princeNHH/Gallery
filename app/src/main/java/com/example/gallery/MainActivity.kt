package com.example.gallery

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.VideoView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.gallery.databinding.ActivityMainBinding
import com.example.gallery.fragment.AlbumFragment
import com.example.gallery.fragment.TimelineFragment

@UnstableApi
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val TAG_TIMELINE_FRAGMENT = "TimelineFragment"
    private var timelineFragment: TimelineFragment? = null
    private lateinit var videoView: VideoView

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)

        if (savedInstanceState == null) {
            timelineFragment = TimelineFragment()
            addFragment(timelineFragment!!)
        } else {
            timelineFragment =
                supportFragmentManager.findFragmentByTag(TAG_TIMELINE_FRAGMENT) as TimelineFragment?
        }

        binding.videoTab.setOnClickListener {
            if (timelineFragment == null) {
                timelineFragment = TimelineFragment()
                addFragment(timelineFragment!!)
            } else {
                showFragment(timelineFragment!!)
            }
        }

        binding.albumTab.setOnClickListener {
            hideFragment(timelineFragment!!)
        }

        videoView = binding.playerView

        // Get the video URL from Intent extras
        val videoUrl = intent.getStringExtra("videoUrl")
        if (videoUrl != null) {
            playVideo(videoUrl)
        }
        setContentView(binding.root)
    }

    private fun playVideo(videoUrl: String) {
        val uri = Uri.parse(videoUrl)
        binding.playerLayout.visibility = View.VISIBLE
        videoView.setVideoURI(uri)
        videoView.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.isLooping = true  // Optionally loop the video
            videoView.start()
        }
        videoView.setOnCompletionListener {
            // Optionally handle video completion
        }
    }

    private fun addFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.content_layout, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun hideFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .hide(fragment)
            .commit()
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .show(fragment)
            .commit()
    }
}
