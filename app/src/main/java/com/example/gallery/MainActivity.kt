package com.example.gallery

import com.example.gallery.viewmodel.VideoViewModel
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.VideoView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.Log
import androidx.viewpager2.widget.ViewPager2
import com.example.gallery.adapter.TimelineAdapter
import com.example.gallery.databinding.ActivityMainBinding
import com.example.gallery.fragment.AlbumFragment
import com.example.gallery.fragment.TimelineFragment
import com.example.gallery.fragment.ViewPagerFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var videoViewModel: VideoViewModel
    private lateinit var timeLineAdapter: TimelineAdapter
    private var timelineFragment: TimelineFragment? = null
    private var albumFragment: AlbumFragment? = null
    private var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        videoViewModel = ViewModelProvider(this)[VideoViewModel::class.java]

        timeLineAdapter = TimelineAdapter(emptyList(), this)

        videoViewModel.listVideo.observe(this, Observer { videos ->
            timeLineAdapter.submitList(videos)
        })

        timelineFragment = TimelineFragment(timeLineAdapter)
        albumFragment = AlbumFragment()

        binding.videoTab.setOnClickListener {
            switchFragment(timelineFragment!!)
            binding.videoTabUnderline.visibility = View.VISIBLE
            binding.albumTabUnderline.visibility = View.GONE
        }

        binding.albumTab.setOnClickListener {
            switchFragment(albumFragment!!)
            binding.videoTabUnderline.visibility = View.GONE
            binding.albumTabUnderline.visibility = View.VISIBLE
        }

        // Load initial fragment
        switchFragment(timelineFragment!!)
    }

    private fun switchFragment(newFragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()

        if (currentFragment != null) {
            transaction.hide(currentFragment!!)
        }

        if (!newFragment.isAdded) {
            transaction.add(R.id.fragment_container, newFragment, newFragment::class.java.simpleName)
        } else {
            transaction.show(newFragment)
        }

        transaction.commit()
        currentFragment = newFragment
    }

    override fun onResume() {
        super.onResume()
        checkPermission()
    }

    private fun checkPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED -> {
            }
            !ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.READ_MEDIA_VIDEO
            ) -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_VIDEO)
            }
            else -> {
                showPermissionSettingsDialog()
            }
        }
    }

    private fun showPermissionSettingsDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_permission, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<TextView>(R.id.button_settings).setOnClickListener {
            openAppSettings()
            dialog.dismiss()
        }

        dialogView.findViewById<TextView>(R.id.button_exit).setOnClickListener {
            dialog.dismiss()
            finish()
        }
        dialog.window?.attributes?.gravity = Gravity.BOTTOM
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.attributes?.verticalMargin = 0.025f
        dialog.show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}
}


