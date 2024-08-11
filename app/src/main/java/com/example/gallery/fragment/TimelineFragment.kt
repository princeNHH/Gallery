package com.example.gallery.fragment

import android.content.res.Configuration
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gallery.MainActivity
import com.example.gallery.R
import com.example.gallery.TimelineItem
import com.example.gallery.adapter.TimelineAdapter
import com.example.gallery.databinding.TimelineFragmentBinding
import com.example.gallery.helper.DragSelectTouchListener
import com.example.gallery.helper.Mode

class TimelineFragment : Fragment(), TimelineAdapter.OnItemClickListener,
    TimelineAdapter.OnItemLongClickListener, TimelineAdapter.OnSelectionChangedListener {

    private var _binding: TimelineFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: TimelineAdapter
    private lateinit var dragSelectTouchListener: DragSelectTouchListener

    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            adapter.exitSelectionMode()
            binding.selectText.visibility = View.GONE
            (activity as MainActivity).binding.bottomBar.visibility = View.VISIBLE
            adapter.createBottomBarUpAnimator((activity as MainActivity).binding.bottomBar)
            binding.bottomBarController.visibility = View.GONE
        }
    }

    companion object {
        fun newInstance(): TimelineFragment {
            return TimelineFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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

        //hold all select and deselect
        savedInstanceState?.let {
            val selectedUris =
                it.getStringArray("selected_uris")?.map { uri -> Uri.parse(uri) } ?: emptyList()
            adapter.isSelectionMode = it.getBoolean("is_selection_mode", false)

            if (adapter.isSelectionMode) {
                adapter.selectedItems.addAll(selectedUris)
                adapter.toggleCheckboxVisibility(true)
                adapter.updateSelectionCount()
                adapter.updateHeaderCheckboxOnItemSelection()
            }
        }

        val spanCount = adapter.getSpanCountForOrientation(binding.rcvTimelineFragment)
        val gridLayoutManager = GridLayoutManager(requireContext(), spanCount).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (
                        adapter.getItemViewType(position) == TimelineAdapter.VIEW_TYPE_HEADER
                    ) spanCount else 1
                }
            }
        }
        setItemSpacing()
        binding.rcvTimelineFragment.layoutManager = gridLayoutManager
        binding.rcvTimelineFragment.adapter = adapter
        binding.rcvTimelineFragment.itemAnimator = null

        (activity as MainActivity).viewModel.timelineItems.observe(viewLifecycleOwner) { items ->
            if (adapter.currentList != items) {
                adapter.submitList(items)
            }
        }

        //drag to select
        dragSelectTouchListener = DragSelectTouchListener.create(requireContext(), adapter)
        binding.rcvTimelineFragment.addOnItemTouchListener(dragSelectTouchListener)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rcvTimelineFragment.adapter = null
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val selectedUris = adapter.selectedItems.map { it.toString() }.toTypedArray()
        outState.putStringArray("selected_uris", selectedUris)
        outState.putBoolean("is_selection_mode", adapter.isSelectionMode)
    }

    private fun setItemSpacing() {
        val itemSpacing =
            resources.getDimensionPixelSize(R.dimen.item_spacing) // Thêm giá trị khoảng cách trong res/values/dimens.xml

        binding.rcvTimelineFragment.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
            ) {
                outRect.set(
                    itemSpacing, // left
                    itemSpacing, // top
                    itemSpacing, // right
                    itemSpacing // bottom
                )
            }
        })
    }

    private fun updateSelectedCount(count: Int) {
        if (count == 0) {
            binding.selectText.visibility = View.VISIBLE
            binding.selectText.text = "Select items"
        } else {
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
        parentFragmentManager.beginTransaction().replace(R.id.main, fragment).addToBackStack(null)
            .commit()
    }

    override fun onItemLongClick(position: Int) {
        dragSelectTouchListener.setIsActive(true, position)
        binding.bottomBarController.visibility = View.VISIBLE
        adapter.createBottomBarUpAnimator(binding.bottomBarController)
        (activity as MainActivity).binding.bottomBar.visibility = View.GONE
    }

    override fun onSelectionChanged(selectedCount: Int) {
        updateSelectedCount(selectedCount)
    }
}



