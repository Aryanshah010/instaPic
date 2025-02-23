package com.example.instapic.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.instapic.R
import com.example.instapic.databinding.ActivityLoginBinding
import com.example.instapic.repository.UserRepositoryImpl
import com.example.instapic.viewmodel.UserViewModel

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var repo = UserRepositoryImpl()
        userViewModel = UserViewModel(repo)

        // Login button click
        binding.btnLogin.setOnClickListener {
            val email: String = binding.loginEmail.text.toString()
            val password: String = binding.loginPassword.text.toString()

            userViewModel.login(email, password) { success, message ->
                if (success) {
                    Toast.makeText(this@LoginActivity, message, Toast.LENGTH_LONG).show()
                    val intent = Intent(this@LoginActivity, NavigationActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                }
            }
        }

        // Navigate to sign up screen
        binding.btnSignupnavigate.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Navigate to forgot password screen
        binding.btnForget.setOnClickListener {
            val intent = Intent(this@LoginActivity, ForgetPasswordActivity::class.java)
            startActivity(intent)
        }

        // Apply edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Forgot password action
    fun onForgotPasswordClicked(view: View) {
        val intent = Intent(this, ForgetPasswordActivity::class.java)
        startActivity(intent)
    }

    // Sign up action
    fun onSignUpClicked(view: View) {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
}
