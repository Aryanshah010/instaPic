package com.example.instapic.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.instapic.R
import com.example.instapic.adapter.PostAdapter
import com.example.instapic.databinding.FragmentProfileBinding
import com.example.instapic.repository.PostRepositoryImpl
import com.example.instapic.repository.UserRepositoryImpl
import com.example.instapic.ui.activity.LoginActivity
import com.example.instapic.viewmodel.PostViewModel
import com.example.instapic.viewmodel.PostViewModelFactory
import com.example.instapic.viewmodel.UserViewModel

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var postAdapter: PostAdapter
    private lateinit var userViewModel: UserViewModel
    
    private val postViewModel: PostViewModel by viewModels {
        PostViewModelFactory(PostRepositoryImpl())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViewModels()
        setupRecyclerView()
        setupUI()
        loadUserData()
        loadUserPosts()
    }

    private fun setupViewModels() {
        userViewModel = UserViewModel(UserRepositoryImpl())
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(requireContext(), listOf())
        binding.userPostsRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = postAdapter
        }
    }

    private fun setupUI() {
        binding.logoutButton.setOnClickListener {
            userViewModel.logout { success, message ->
                if (success) {
                    startActivity(Intent(requireContext(), LoginActivity::class.java))
                    requireActivity().finish()
                } else {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadUserData() {
        val userId = userViewModel.getCurrentUser()?.uid
        if (userId != null) {
            userViewModel.getUserFromDatabase(userId)
            userViewModel.userData.observe(viewLifecycleOwner) { user ->
                user?.let {
                    binding.userNameText.text = getString(R.string.user_full_name, it.firstName, it.lastName)
                    binding.userEmailText.text = it.email
                }
            }
        }
    }

    private fun loadUserPosts() {
        val userId = userViewModel.getCurrentUser()?.uid
        if (userId != null) {
            postViewModel.fetchUserPosts(userId)
            postViewModel.posts.observe(viewLifecycleOwner) { posts ->
                posts?.let {
                    postAdapter.updatePosts(it)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 