package com.example.mystoryapp.activity

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mystoryapp.R
import com.example.mystoryapp.adapter.ListStoryAdapter
import com.example.mystoryapp.data.Session
import com.example.mystoryapp.databinding.ActivityStoryBinding
import com.example.mystoryapp.response.GetStoriesResponse
import com.example.mystoryapp.response.ListStoryItem
import com.example.mystoryapp.retrofit.ApiConfig
import com.example.mystoryapp.settings.SettingPreferences
import com.example.mystoryapp.settings.SettingPreferencesViewModel
import com.example.mystoryapp.settings.SettingPreferencesViewModelFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private val Context.datasore: DataStore<Preferences> by preferencesDataStore(name = "login")

class StoryActivity : AppCompatActivity(), ListStoryAdapter.OnItemClickCallback {

    private var _activityStoryBinding: ActivityStoryBinding? = null
    private val binding get() = _activityStoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _activityStoryBinding = ActivityStoryBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val pref = SettingPreferences.getInstance(datasore)
        val settingPreferencesViewModel = ViewModelProvider(this, SettingPreferencesViewModelFactory(pref))[SettingPreferencesViewModel::class.java]

        settingPreferencesViewModel.getLoginSettings().observe(this
        ) { data: Session ->
            if (!data.isLogin) {
                val intent = Intent(this@StoryActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                showAllStories(data.token)

                binding?.fabAddStory?.setOnClickListener {
                    val intent = Intent(this@StoryActivity, MainCameraActivity::class.java)
                    intent.putExtra(MainCameraActivity.EXTRA_POST_STORY, data.token)
                    startActivity(intent)
                    finish()
                }
            }
        }

        showRecyclerList()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.option_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.logout -> {
                login(false)
                true
            }
            else -> true
        }
    }

    private fun showRecyclerList(){
        if (applicationContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
            binding?.rvStory?.layoutManager = GridLayoutManager(this, 2)
        } else{
            binding?.rvStory?.layoutManager = LinearLayoutManager(this)
        }

        val listUserAdapter = ListStoryAdapter(arrayListOf(), this@StoryActivity)
        binding?.rvStory?.adapter = listUserAdapter
    }

    private fun showAllStories(token: String?){
        showLoading(true)
        val bearerToken = "Bearer $token"
        Log.d(TAG, bearerToken)

        val listStoryItem = arrayListOf <List<ListStoryItem>>()
        val storyItem = arrayListOf<ListStoryItem>()

        val client = ApiConfig.getApiService().getStories(bearerToken)
        client.enqueue(object : Callback<GetStoriesResponse>{
            override fun onResponse(
                call: Call<GetStoriesResponse>,
                response: Response<GetStoriesResponse>
            ) {
                showLoading(false)
                val stories = response.body()
                if (stories != null){
                    listStoryItem.add(stories.listStory)
                    Log.d("listStoryItem: ", listStoryItem.toString())
                    for (story in listStoryItem){
                        story.forEach {
                            Log.d("listStory:", it.toString())
                            storyItem.add(it)
                        }
                        val adapter = ListStoryAdapter(storyItem, this@StoryActivity)
                        binding?.rvStory?.adapter = adapter
                    }
                }
            }

            override fun onFailure(call: Call<GetStoriesResponse>, t: Throwable) {
                showLoading(false)
                t.message?.let { debugLog(it) }
            }
        })
    }

    private fun debugLog(message: String){
        Log.d(TAG, message)
    }

    private fun showLoading(isLoading: Boolean){
        if (isLoading){
            binding?.progressBar?.visibility  = View.VISIBLE
        } else {
            binding?.progressBar?.visibility  = View.GONE
        }
    }

    override fun onItemClicked(data: ListStoryItem) {
        Toast.makeText(this, data.name, Toast.LENGTH_SHORT).show()
        val intentToDetail = Intent(this@StoryActivity, StoryDetailActivity::class.java)
        intentToDetail.putExtra(StoryDetailActivity.EXTRA_STORY, data)
        startActivity(intentToDetail, ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle())
    }

    private fun login(isLogin: Boolean){
        val pref = SettingPreferences.getInstance(datasore)
        val settingPreferencesViewModel = ViewModelProvider(this, SettingPreferencesViewModelFactory(pref))[SettingPreferencesViewModel::class.java]

        settingPreferencesViewModel.saveLoginSetting(isLogin, "")
    }

    companion object{
        const val TAG = "StoryActivity"
    }
}