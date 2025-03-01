package com.example.instapic.repository

import com.example.instapic.model.PostModel
import com.google.firebase.database.*
import com.google.firebase.database.Transaction
import com.google.firebase.database.MutableData

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
        val postRef = postsRef.child(postId)
        
        postRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val post = mutableData.getValue(PostModel::class.java) ?: return Transaction.success(mutableData)
                
                post.likes[userId] = true
                post.likesCount = post.likes.size
                
                mutableData.value = post
                return Transaction.success(mutableData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                if (error != null) {
                    callback(false, error.message)
                } else {
                    callback(true, "Post liked")
                }
            }
        })
    }

    // Unlike a post
    override fun unlikePost(postId: String, userId: String, callback: (Boolean, String) -> Unit) {
        val postRef = postsRef.child(postId)
        
        postRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val post = mutableData.getValue(PostModel::class.java) ?: return Transaction.success(mutableData)
                
                post.likes.remove(userId)
                post.likesCount = post.likes.size
                
                mutableData.value = post
                return Transaction.success(mutableData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                if (error != null) {
                    callback(false, error.message)
                } else {
                    callback(true, "Post unliked")
                }
            }
        })
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

    // Delete a post
    override fun deletePost(postId: String, callback: (Boolean, String) -> Unit) {
        postsRef.child(postId).removeValue()
            .addOnSuccessListener { callback(true, "Post deleted successfully") }
            .addOnFailureListener { callback(false, it.message ?: "Error deleting post") }
    }

    // Edit post
    override fun editPost(postId: String, updates: Map<String, Any>, callback: (Boolean, String) -> Unit) {
        postsRef.child(postId).updateChildren(updates)
            .addOnSuccessListener { callback(true, "Post updated successfully") }
            .addOnFailureListener { callback(false, it.message ?: "Error updating post") }
    }
}
