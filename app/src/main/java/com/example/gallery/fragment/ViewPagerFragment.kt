package com.example.gallery.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.VideoView
import androidx.fragment.app.Fragment
import com.example.gallery.databinding.ViewpagerFragmentBinding

class ViewPagerFragment : Fragment() {
    private var _binding: ViewpagerFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var videoView: VideoView
    private var videoUri: Uri? = null

    companion object {
        private const val ARG_VIDEO_URI = "video_uri"

        fun newInstance(videoUri: Uri): ViewPagerFragment {
            val fragment = ViewPagerFragment()
            val args = Bundle().apply {
                putParcelable(ARG_VIDEO_URI, videoUri)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ViewpagerFragmentBinding.inflate(inflater, container, false)
        videoView = binding.playerView
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        videoUri = arguments?.getParcelable(ARG_VIDEO_URI)
        initializePlayer()
    }

    private fun initializePlayer() {
        videoView.setMediaController(MediaController(requireContext()))
        videoUri?.let {
            videoView.setVideoURI(it)
            videoView.setOnPreparedListener { mediaPlayer ->
                mediaPlayer.start()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        videoView.pause()
    }

    override fun onResume() {
        super.onResume()
        videoView.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        videoView.suspend()
    }
}