package com.example.gallery.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.gallery.MainActivity
import com.example.gallery.R
import com.example.gallery.TimelineItem
import com.example.gallery.adapter.TimelineAdapter
import com.example.gallery.databinding.TimelineFragmentBinding

class TimelineFragment : Fragment(), TimelineAdapter.OnItemClickListener,
    TimelineAdapter.OnItemLongClickListener, TimelineAdapter.OnSelectionChangedListener {

    private var _binding: TimelineFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: TimelineAdapter

    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            adapter.exitSelectionMode()
            binding.selectText.visibility = View.GONE
            (activity as MainActivity).binding.bottomBar.visibility = View.VISIBLE
        }
    }

    companion object {
        fun newInstance(): TimelineFragment {
            return TimelineFragment()
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
            registerOnItemClickListener(this@TimelineFragment)
            registerOnItemLongClickListener(this@TimelineFragment)
            registerOnSelectionChangedListener(this@TimelineFragment)
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

    private fun updateSelectedCount(count: Int) {
        if(count == 0){
            binding.selectText.visibility = View.VISIBLE
            binding.selectText.text = "Select items"
        }else{
            binding.selectText.visibility = View.VISIBLE
            binding.selectText.text = "$count selected"
        }

    }

    override fun onItemClick(position: Int) {
        val videoUriPositions = adapter.currentList.mapIndexedNotNull { index, item ->
            if (item is TimelineItem.VideoItem) {
                item.mediaItem.localConfiguration?.uri?.let { uri -> Pair(index, uri) }
            } else {
                null
            }
        }

        val videoPosition = videoUriPositions.indexOfFirst { it.first == position }
        val selectedUri = videoUriPositions[videoPosition].second
        val videoUris = videoUriPositions.map { it.second }

        val fragment = ViewPagerFragment.newInstance(videoUris, selectedUri)
        parentFragmentManager.beginTransaction()
            .replace(R.id.main, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onItemLongClick(position: Int) {
        // Handle item long click
    }

    override fun onSelectionChanged(selectedCount: Int) {
        updateSelectedCount(selectedCount)
    }

}



