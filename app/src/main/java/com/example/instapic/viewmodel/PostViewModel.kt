package com.example.instapic.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instapic.model.PostModel
import com.example.instapic.repository.PostRepository
import com.google.firebase.auth.FirebaseAuth

class PostViewModel(private val postRepository: PostRepository) : ViewModel() {

    private val _posts = MutableLiveData<List<PostModel>?>()
    val posts: MutableLiveData<List<PostModel>?> get() = _posts

    private val _uploadStatus = MutableLiveData<Pair<Boolean, String>>()
    val uploadStatus: LiveData<Pair<Boolean, String>> get() = _uploadStatus

    private val _likeStatus = MutableLiveData<Pair<Boolean, String>>()
    val likeStatus: LiveData<Pair<Boolean, String>> get() = _likeStatus

    private val _likesCount = MutableLiveData<Pair<Int, String>>()
    val likesCount: LiveData<Pair<Int, String>> get() = _likesCount

    // Upload Post
    fun uploadPost(post: PostModel) {
        postRepository.uploadPost(post) { success, message ->
            _uploadStatus.postValue(Pair(success, message))
        }
    }

    // Fetch All Posts
    fun fetchAllPosts() {
        postRepository.fetchAllPosts { postList, success, _ ->
            if (success) {
                _posts.postValue(postList)
            }
        }
    }

    // Fetch User Posts
    fun fetchUserPosts(userId: String) {
        postRepository.fetchUserPosts(userId) { postList, success, _ ->
            if (success) {
                _posts.postValue(postList)
            }
        }
    }

    // Like Post
    fun likePost(postId: String, userId: String) {
        postRepository.likePost(postId, userId) { success, message ->
            _likeStatus.postValue(Pair(success, message))
        }
    }

    // Unlike Post
    fun unlikePost(postId: String, userId: String) {
        postRepository.unlikePost(postId, userId) { success, message ->
            _likeStatus.postValue(Pair(success, message))
        }
    }

    // Get Likes Count
    fun getLikesCount(postId: String) {
        postRepository.getLikesCount(postId) { count, success, message ->
            if (success) {
                _likesCount.postValue(Pair(count, message))
            }
        }
    }

    fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid 
            ?: throw IllegalStateException("User must be logged in")
    }
}
