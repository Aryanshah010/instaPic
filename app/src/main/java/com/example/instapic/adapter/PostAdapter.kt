package com.example.instapic.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instapic.R
import com.example.instapic.databinding.ItemPostBinding
import com.example.instapic.model.PostModel

class PostAdapter(private val context: Context, private var postList: List<PostModel>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    inner class PostViewHolder(val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        holder.binding.apply {
            Glide.with(context).load(post.imageUrl).into(postImage)
            captionText.text = post.caption
            likeCount.text = context.getString(R.string.likes_count, post.likesCount)
        }
    }

    override fun getItemCount(): Int = postList.size

    fun updatePosts(newPosts: List<PostModel>) {
        postList = newPosts
        notifyDataSetChanged()
    }
}
