package com.example.mystoryapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mystoryapp.databinding.StoryItemCardBinding
import com.example.mystoryapp.response.ListStoryItem

class ListStoryAdapter(private val story: ArrayList<ListStoryItem>, onItemClick: OnItemClickCallback): RecyclerView.Adapter<ListStoryAdapter.ViewHolder>() {

    private var onItemClickCallback: OnItemClickCallback = onItemClick

    class ViewHolder (private val binding: StoryItemCardBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(story: ListStoryItem){
            with(binding){
                userName.text = story.name
                Glide.with(itemView.context)
                    .load(story.photoUrl)
                    .into(imgItemPhoto)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = StoryItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(this.story[position])
        holder.itemView.setOnClickListener {
            onItemClickCallback.onItemClicked(story[holder.adapterPosition])
        }
    }

    override fun getItemCount(): Int {
        return story.size
    }

    interface OnItemClickCallback {
        fun onItemClicked(data: ListStoryItem)
    }
}