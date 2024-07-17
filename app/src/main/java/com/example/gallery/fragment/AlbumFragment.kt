package com.example.gallery.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.gallery.R
import com.example.gallery.databinding.TimelineFragmentBinding

class AlbumFragment : Fragment() {
    var _binding: TimelineFragmentBinding? = null
    val binding get()   = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = TimelineFragmentBinding.inflate(inflater, container, false)
        val recyclerView = binding.rcvTimelineFragment
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 4)

        return _binding?.root
    }


}