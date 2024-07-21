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
import com.example.gallery.viewmodel.VideoViewModel

class TimelineFragment : Fragment() {
    private var _binding: TimelineFragmentBinding? = null
    private val binding get() = _binding!!
    private var adapter: TimelineAdapter? = null

    companion object {
        fun newInstance(): TimelineFragment {
            return TimelineFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = TimelineFragmentBinding.inflate(inflater, container, false)
        Log.d("Hiep", "onCreateViewTimeLineFragment")

        val recyclerView = binding.rcvTimelineFragment
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 4)

        if (adapter != null) {
            recyclerView.adapter = adapter
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        Log.d("Hiep", "onResumeTimeLineFragment")
    }

    fun setAdapter(adapter: TimelineAdapter) {
        this.adapter = adapter
        _binding?.rcvTimelineFragment?.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


