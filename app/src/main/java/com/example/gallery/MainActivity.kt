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
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
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
    private lateinit var timelineFragment: TimelineFragment
    private lateinit var albumFragment: AlbumFragment
    private var currentFragment: Fragment? = null

    val viewModel: VideoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        timelineFragment = TimelineFragment.newInstance()
        albumFragment = AlbumFragment()

        binding.videoTab.setOnClickListener {
            switchFragment(timelineFragment)
            setTabUnderlineVisibility(binding.videoTabUnderline, binding.albumTabUnderline)
        }

        binding.albumTab.setOnClickListener {
            switchFragment(albumFragment)
            setTabUnderlineVisibility(binding.albumTabUnderline, binding.videoTabUnderline)
        }

        // Load initial fragment
        switchFragment(timelineFragment)
        setTabUnderlineVisibility(binding.videoTabUnderline, binding.albumTabUnderline)

        checkAndRequestPermissions()
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.loadVideos()
        }
    }

    private fun switchFragment(newFragment: Fragment) {
        supportFragmentManager.commit {
            if (currentFragment != null) hide(currentFragment!!)
            if (!newFragment.isAdded) {
                add(R.id.fragment_container, newFragment, newFragment::class.java.simpleName)
            } else {
                show(newFragment)
            }
        }
        currentFragment = newFragment
    }

    private fun setTabUnderlineVisibility(visibleTab: View, invisibleTab: View) {
        visibleTab.visibility = View.VISIBLE
        invisibleTab.visibility = View.GONE
    }

    private fun checkAndRequestPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED -> {
                viewModel.loadVideos()
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

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.loadVideos()
        }
    }

    private fun showPermissionSettingsDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_permission, null)
        AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()
            .apply {
                window?.setGravity(Gravity.BOTTOM)
                window?.attributes?.verticalMargin = 0.025f
                window?.setBackgroundDrawableResource(android.R.color.transparent)
                show()
                dialogView.findViewById<TextView>(R.id.button_settings).setOnClickListener {
                    dismiss()
                    openAppSettings()
                }
                dialogView.findViewById<TextView>(R.id.button_exit).setOnClickListener {
                    dismiss()
                    finish()
                }
            }
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

}


