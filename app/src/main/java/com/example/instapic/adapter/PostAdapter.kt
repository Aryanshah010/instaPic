package com.example.instapic.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instapic.R
import com.example.instapic.databinding.ItemPostBinding
import com.example.instapic.model.PostModel
import com.google.firebase.auth.FirebaseAuth

class PostAdapter(
    private val context: Context, 
    private var postList: List<PostModel>,
    private val onLikeClicked: (PostModel) -> Unit = {},
    private val onEditClicked: (PostModel) -> Unit = {}
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    inner class PostViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        holder.binding.apply {
            Glide.with(context).load(post.imageUrl).into(postImage)
            captionText.text = post.caption
            likeCount.text = "${post.likesCount} likes"
            
            // Update like button state and click listener
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            val isLiked = currentUserId?.let { post.likes.containsKey(it) } ?: false
            btnLike.setImageResource(
                if (isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
            )
            
            btnLike.setOnClickListener {
                onLikeClicked(post)
            }

            // Add long click listener for edit
            root.setOnLongClickListener {
                onEditClicked(post)
                true
            }
        }
    }

    override fun getItemCount(): Int = postList.size

    fun updatePosts(newPosts: List<PostModel>) {
        postList = newPosts
        notifyDataSetChanged()
    }

    fun getPost(position: Int): PostModel = postList[position]

    fun removePost(position: Int) {
        val newList = postList.toMutableList()
        newList.removeAt(position)
        postList = newList
        notifyItemRemoved(position)
    }
}
