package com.example.instapic.repository

import com.example.instapic.model.PostModel

interface PostRepository {

    // Upload a new post
    fun uploadPost(post: PostModel, callback: (Boolean, String) -> Unit)

    // Fetch all posts (for FeedFragment)
    fun fetchAllPosts(callback: (List<PostModel>?, Boolean, String) -> Unit)

    // Fetch posts by a specific user
    fun fetchUserPosts(userId: String, callback: (List<PostModel>?, Boolean, String) -> Unit)

    // Like a post
    fun likePost(postId: String, userId: String, callback: (Boolean, String) -> Unit)

    // Unlike a post
    fun unlikePost(postId: String, userId: String, callback: (Boolean, String) -> Unit)

    // Fetch likes count for a post
    fun getLikesCount(postId: String, callback: (Int, Boolean, String) -> Unit)
}
