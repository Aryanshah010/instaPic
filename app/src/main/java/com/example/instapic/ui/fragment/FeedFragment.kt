package com.example.instapic.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.instapic.adapter.PostAdapter
import com.example.instapic.databinding.FragmentFeedBinding
import com.example.instapic.repository.PostRepositoryImpl
import com.example.instapic.viewmodel.PostViewModel
import com.example.instapic.viewmodel.PostViewModelFactory
import com.example.instapic.model.PostModel
import com.example.instapic.utils.SwipeToDeleteCallback
import android.widget.EditText

class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    private lateinit var postAdapter: PostAdapter

    private val postViewModel: PostViewModel by viewModels {
        PostViewModelFactory(PostRepositoryImpl())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        loadPosts()
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(
            requireContext(),
            listOf(),
            onLikeClicked = { post -> handleLikeClick(post) },
            onEditClicked = { post -> showEditDialog(post) }
        )
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = postAdapter
            
            // Add swipe functionality
            val swipeHandler = SwipeToDeleteCallback(postAdapter) { position ->
                val post = postAdapter.getPost(position)
                if (post.userId == postViewModel.getCurrentUserId()) {
                    showDeleteConfirmation(post, position)
                } else {
                    // Reset the swipe if user is not the owner
                    postAdapter.notifyItemChanged(position)
                    Toast.makeText(context, "Only the post owner can delete this post", Toast.LENGTH_SHORT).show()
                }
            }
            ItemTouchHelper(swipeHandler).attachToRecyclerView(this)
        }
    }

    private fun setupObservers() {
        postViewModel.posts.observe(viewLifecycleOwner) { posts ->
            posts?.let { postList ->
                postAdapter.updatePosts(postList)
            }
        }

        postViewModel.likeStatus.observe(viewLifecycleOwner) { (success, message) ->
            if (!success) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleLikeClick(post: PostModel) {
        try {
            val userId = postViewModel.getCurrentUserId()
            if (post.likes.containsKey(userId)) {
                postViewModel.unlikePost(post.postId, userId)
            } else {
                postViewModel.likePost(post.postId, userId)
            }
            // Refresh posts to update like count
            postViewModel.fetchAllPosts()
        } catch (e: IllegalStateException) {
            Toast.makeText(context, "Please login to like posts", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadPosts() {
        postViewModel.fetchAllPosts()
        postViewModel.posts.observe(viewLifecycleOwner) { posts ->
            posts?.let { postList ->
                if (postList.isNotEmpty()) {
                    postAdapter.updatePosts(postList)
                } else {
                    Toast.makeText(requireContext(), "No posts available", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showDeleteConfirmation(post: PostModel, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Post")
            .setMessage("Are you sure you want to delete this post?")
            .setPositiveButton("Delete") { _, _ ->
                postViewModel.deletePost(post.postId) { success, message ->
                    if (success) {
                        postAdapter.removePost(position)
                    } else {
                        postAdapter.notifyItemChanged(position)
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                postAdapter.notifyItemChanged(position)
            }
            .show()
    }

    private fun showEditDialog(post: PostModel) {
        if (post.userId != postViewModel.getCurrentUserId()) {
            Toast.makeText(context, "Only the post owner can edit this post", Toast.LENGTH_SHORT).show()
            return
        }

        val editText = EditText(context).apply {
            setText(post.caption)
            setSingleLine(false)
            maxLines = 5
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Post")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val newCaption = editText.text.toString()
                postViewModel.editPost(post.postId, newCaption) { success, message ->
                    if (success) {
                        postViewModel.fetchAllPosts() // Refresh the posts
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
