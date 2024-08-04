package com.example.gallery.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.gallery.R
import com.example.gallery.adapter.VideoPagerAdapter
import com.example.gallery.databinding.ViewpagerFragmentBinding
import kotlinx.coroutines.launch

class ViewPagerFragment : Fragment() {
    private var _binding: ViewpagerFragmentBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_VIDEO_URIS = "video_uris"
        private const val ARG_INITIAL_URI = "initial_uri"

        fun newInstance(videoUris: List<Uri>, initialUri: Uri): ViewPagerFragment {
            val args = bundleOf(
                ARG_VIDEO_URIS to ArrayList(videoUris),
                ARG_INITIAL_URI to initialUri
            )
            return ViewPagerFragment().apply { arguments = args }
        }
    }

    private lateinit var viewPager: ViewPager2
    private lateinit var videoUris: List<Uri>
    private lateinit var initialUri: Uri

    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ViewpagerFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        videoUris = BundleCompat.getParcelableArrayList(requireArguments(), ARG_VIDEO_URIS, Uri::class.java) ?: emptyList()
        initialUri = BundleCompat.getParcelable(requireArguments(), ARG_INITIAL_URI, Uri::class.java) ?: Uri.EMPTY

        viewPager = binding.viewPager
        val adapter = VideoPagerAdapter(videoUris)
        viewPager.adapter = adapter

        // Set the current item to the initial URI's position
        val initialPosition = videoUris.indexOf(initialUri)
        if (initialPosition != -1) {
            viewPager.setCurrentItem(initialPosition, false)
        }
        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        viewLifecycleOwner.lifecycleScope.launch {
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
