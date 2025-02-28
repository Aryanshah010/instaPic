package com.example.instapic.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instapic.adapter.PostAdapter
import com.example.instapic.databinding.FragmentFeedBinding
import com.example.instapic.repository.PostRepositoryImpl
import com.example.instapic.viewmodel.PostViewModel
import com.example.instapic.viewmodel.PostViewModelFactory

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
        loadPosts()
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(requireContext(), listOf())
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = postAdapter
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
