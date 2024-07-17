package com.example.gallery

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gallery.databinding.ActivityMainBinding
import com.example.gallery.fragment.TimelineFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)

        supportFragmentManager.beginTransaction()
            .replace(
                R.id.content_layout,
                TimelineFragment()
            ) // Replace fragment_container with your container ID
            .addToBackStack(null)  // Optional: Add transaction to back stack
            .commit()

        setContentView(binding.root)
    }
}
