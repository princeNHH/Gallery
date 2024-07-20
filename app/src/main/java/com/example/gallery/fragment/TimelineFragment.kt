package com.example.gallery.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.gallery.MainActivity
import com.example.gallery.adapter.TimelineAdapter
import com.example.gallery.databinding.TimelineFragmentBinding

class TimelineFragment(private val adapter: TimelineAdapter) : Fragment() {
    private var _binding: TimelineFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = TimelineFragmentBinding.inflate(inflater, container, false)

        val recyclerView = binding.rcvTimelineFragment
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 4)
        recyclerView.adapter = adapter
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


