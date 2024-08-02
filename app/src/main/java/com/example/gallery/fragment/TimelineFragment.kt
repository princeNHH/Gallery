package com.example.gallery.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.gallery.MainActivity
import com.example.gallery.adapter.TimelineAdapter
import com.example.gallery.databinding.TimelineFragmentBinding
import com.example.gallery.viewmodel.VideoViewModel

class TimelineFragment : Fragment(), TimelineAdapter.OnItemClickListener,
    TimelineAdapter.OnItemLongClickListener, TimelineAdapter.OnSelectionChangedListener {

    private var _binding: TimelineFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: TimelineAdapter
    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            adapter.exitSelectionMode()
            binding.selectText.visibility = View.GONE
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TimelineFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TimelineAdapter(requireContext()).apply {
            setOnItemClickListener(this@TimelineFragment)
            setOnItemLongClickListener(this@TimelineFragment)
            setOnSelectionChangedListener(this@TimelineFragment)
        }

        val gridLayoutManager = GridLayoutManager(requireContext(), 3).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (adapter.getItemViewType(position) == TimelineAdapter.VIEW_TYPE_HEADER) 3 else 1
                }
            }
        }

        binding.rcvTimelineFragment.layoutManager = gridLayoutManager
        binding.rcvTimelineFragment.adapter = adapter

        (activity as MainActivity).viewModel.timelineItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun updateSelectedCount(count: Int) {
        if(count == 0){
            binding.selectText.visibility = View.VISIBLE
            binding.selectText.text = "Select items"
        }else{
            binding.selectText.visibility = View.VISIBLE
            binding.selectText.text = "$count selected"
        }

    }

    override fun onItemClick(position: Int) {
        // Handle item click
    }

    override fun onItemLongClick(position: Int) {
        // Handle item long click
    }

    override fun onSelectionChanged(selectedCount: Int) {
        updateSelectedCount(selectedCount)
    }

    companion object {
        fun newInstance(): TimelineFragment {
            return TimelineFragment()
        }
    }
}



