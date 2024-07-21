package com.example.gallery.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.gallery.R
import com.example.gallery.adapter.TimelineAdapter
import com.example.gallery.databinding.TimelineFragmentBinding


class TimelineFragment : Fragment(), TimelineAdapter.OnItemClickListener, TimelineAdapter.OnItemLongClickListener {

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


        binding.rcvTimelineFragment.layoutManager = GridLayoutManager(requireContext(), 4)

        if (adapter != null) {
            binding.rcvTimelineFragment.adapter = adapter
            adapter!!.setOnItemClickListener(this)
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


