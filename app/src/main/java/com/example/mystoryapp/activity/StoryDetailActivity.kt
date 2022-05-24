package com.example.mystoryapp.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.mystoryapp.R
import com.example.mystoryapp.databinding.ActivityStoryDetailBinding
import com.example.mystoryapp.response.ListStoryItem

class StoryDetailActivity : AppCompatActivity() {

    private var _activityStoryDetailBinding: ActivityStoryDetailBinding? = null
    private val binding get() = _activityStoryDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _activityStoryDetailBinding = ActivityStoryDetailBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val intentData = intent.getParcelableExtra<ListStoryItem>(EXTRA_STORY)

        val storyDescription = binding?.storyDescription

        binding?.apply {
            storyName.text = getString(R.string.username_tag, intentData?.name)
            storyDescription?.text = getString(R.string.description_tag, intentData?.description)
        }
        binding?.storyImage?.let {
            Glide.with(this)
                .load(intentData?.photoUrl)
                .into(it)
        }
    }

    companion object{
        const val EXTRA_STORY = "story"
    }
}