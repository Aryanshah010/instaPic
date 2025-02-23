package com.example.instapic.repository

import com.example.instapic.model.PostModel
import com.google.firebase.database.*

class PostRepositoryImpl : PostRepository {

    private val database = FirebaseDatabase.getInstance()
    private val postsRef = database.getReference("posts")

    // Upload a new post
    override fun uploadPost(post: PostModel, callback: (Boolean, String) -> Unit) {
        val postId = postsRef.push().key // Generate unique post ID
        if (postId != null) {
            post.postId = postId
            postsRef.child(postId).setValue(post)
                .addOnSuccessListener { callback(true, "Post uploaded successfully") }
                .addOnFailureListener { callback(false, it.message ?: "Error uploading post") }
        } else {
            callback(false, "Error generating post ID")
        }
    }

    // Fetch all posts
    override fun fetchAllPosts(callback: (List<PostModel>?, Boolean, String) -> Unit) {
        postsRef.orderByChild("timestamp").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val posts = mutableListOf<PostModel>()
                for (postSnapshot in snapshot.children) {
                    postSnapshot.getValue(PostModel::class.java)?.let { posts.add(it) }
                }
                callback(posts, true, "Posts fetched successfully")
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null, false, error.message)
            }
        })
    }

    // Fetch posts by a specific user
    override fun fetchUserPosts(userId: String, callback: (List<PostModel>?, Boolean, String) -> Unit) {
        postsRef.orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val posts = mutableListOf<PostModel>()
                    for (postSnapshot in snapshot.children) {
                        postSnapshot.getValue(PostModel::class.java)?.let { posts.add(it) }
                    }
                    callback(posts, true, "User posts fetched successfully")
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(null, false, error.message)
                }
            })
    }

    // Like a post
    override fun likePost(postId: String, userId: String, callback: (Boolean, String) -> Unit) {
        val postRef = postsRef.child(postId).child("likes").child(userId)

        postRef.setValue(true)
            .addOnSuccessListener { callback(true, "Post liked") }
            .addOnFailureListener { callback(false, it.message ?: "Error liking post") }
    }

    // Unlike a post
    override fun unlikePost(postId: String, userId: String, callback: (Boolean, String) -> Unit) {
        val postRef = postsRef.child(postId).child("likes").child(userId)

        postRef.removeValue()
            .addOnSuccessListener { callback(true, "Post unliked") }
            .addOnFailureListener { callback(false, it.message ?: "Error unliking post") }
    }

    // Get likes count
    override fun getLikesCount(postId: String, callback: (Int, Boolean, String) -> Unit) {
        val likesRef = postsRef.child(postId).child("likes")

        likesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback(snapshot.childrenCount.toInt(), true, "Likes count fetched")
            }

            override fun onCancelled(error: DatabaseError) {
                callback(0, false, error.message)
            }
        })
    }
}
