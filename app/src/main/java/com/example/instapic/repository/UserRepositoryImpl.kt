package com.example.instapic.repository

import android.util.Log
import com.example.instapic.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class UserRepositoryImpl : UserRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")

    // Login User
    override fun login(email: String, password: String, callback: (Boolean, String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Login success")
                } else {
                    callback(false, task.exception?.message ?: "Login failed")
                }
            }
    }

    // Signup / Register User
    override fun signup(email: String, password: String, callback: (Boolean, String, String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""
                    callback(true, userId, "Signup successful")
                } else {
                    callback(false, "", task.exception?.message ?: "Signup failed")
                }
            }
    }

    // Forget Password
    override fun forgetPassword(email: String, callback: (Boolean, String) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Password reset email sent")
                } else {
                    callback(false, task.exception?.message ?: "Failed to send reset email")
                }
            }
    }

    // Add user to Firebase Realtime Database
    override fun addUserToDatabase(userId: String, userModel: UserModel, callback: (Boolean, String) -> Unit) {
        database.child(userId).setValue(userModel)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "User added successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to add user")
                }
            }
    }

    // Get Current Logged-in User
    override fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    // Fetch user details from Firebase Realtime Database
    override fun getUserFromDatabase(userId: String, callback: (UserModel?, Boolean, String) -> Unit) {
        database.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UserModel::class.java)
                if (user != null) {
                    callback(user, true, "User found")
                } else {
                    callback(null, false, "User not found")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null, false, error.message)
            }
        })
    }

    // Logout User
    override fun logout(callback: (Boolean, String) -> Unit) {
        auth.signOut()
        callback(true, "Logout successful")
    }

    // Edit User Profile
    override fun editProfile(userId: String, data: MutableMap<String, Any>, callback: (Boolean, String) -> Unit) {
        database.child(userId).updateChildren(data)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Profile updated successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to update profile")
                }
            }
    }
}
