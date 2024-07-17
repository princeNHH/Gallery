package com.example.gallery.fragment

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.database.Cursor
import android.media.browse.MediaBrowser
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.recyclerview.widget.GridLayoutManager
import com.example.gallery.R
import com.example.gallery.adapter.TimelineAdapter
import com.example.gallery.databinding.TimelineFragmentBinding

class TimelineFragment : Fragment() {
    private var _binding: TimelineFragmentBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = TimelineFragmentBinding.inflate(inflater, container, false)
        val recyclerView = binding.rcvTimelineFragment
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 4)

        val adapter = TimelineAdapter(requireContext(),loadVideos())
        recyclerView.adapter = adapter
//        if (checkPermission()) {
//            loadVideos()
//
//        } else {
//            requestPermission()
//        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val PERMISSION_REQUEST_CODE = 100

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadVideos()
            } else {
                Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadVideos(): List<MediaItem> {
        val listVideo = mutableListOf<MediaItem>()
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE
        )

        val cursor: Cursor? = requireContext().contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val name = it.getString(nameColumn)
                val duration = it.getLong(durationColumn)
                val size = it.getLong(sizeColumn)

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                val mediaItem = MediaItem.Builder()
                    .setMediaId(id.toString())
                    .setUri(contentUri)
                    .build()
                listVideo.add(mediaItem)
            }
        }
        return listVideo
    }
}