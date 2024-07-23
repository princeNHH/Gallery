package com.example.gallery.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.example.gallery.MainActivity
import com.example.gallery.adapter.TimelineAdapter
import com.example.gallery.databinding.TimelineFragmentBinding

class TimelineFragment : Fragment(), TimelineAdapter.OnItemClickListener, TimelineAdapter.OnItemLongClickListener {

    private var _binding: TimelineFragmentBinding? = null
    private val binding get() = _binding!!
    private var adapter: TimelineAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TimelineFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TimelineAdapter(requireContext())
        val gridLayoutManager = GridLayoutManager(requireContext(), 4)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (adapter!!.getItemViewType(position) == TimelineAdapter.VIEW_TYPE_HEADER) 4 else 1
            }
        }

        binding.rcvTimelineFragment.layoutManager = gridLayoutManager
        binding.rcvTimelineFragment.adapter = adapter
        // Listen to changes from ViewModel
        (activity as MainActivity).videoViewModel.timelineItems.observe(viewLifecycleOwner, Observer { items ->
            adapter!!.submitList(items)
        })
    }

    companion object {
        fun newInstance(): TimelineFragment {
            return TimelineFragment()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("Hiep", "onResumeTimeLineFragment")
    }

    fun setAdapter(adapter: TimelineAdapter) {
        this.adapter = adapter
        _binding?.rcvTimelineFragment?.adapter = adapter
        adapter.setOnItemClickListener(this)
        adapter.setOnItemLongClickListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(position: Int) {
//        Log.d("Hiep", "onclick")
//        //set checkbox checked when click on item
//        if (adapter!!.getSelectionMode()){
//            adapter!!.setSelectedPosition(position)
//        }
    }

    override fun onItemLongClick(position: Int) {
    }
}


