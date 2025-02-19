package com.example.todo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.todo.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException

class Login : AppCompatActivity() {
    private lateinit var auth : FirebaseAuth
    private lateinit var binding : ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = FirebaseAuth.getInstance()

        binding.loginButton.setOnClickListener{
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()

            if(email.isNotEmpty() && password.isNotEmpty()){
                login(email, password)
            }else{
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvRedirectSignUp.setOnClickListener{
            val intent = Intent(this, Signup::class.java)
            startActivity(intent)
        }
    }

    private fun login(email : String, password : String){
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this){task ->
            if(task.isSuccessful){
                val intent = Intent(this, Home::class.java)
                startActivity(intent)
                finish()
            }else{
                handleFirebaseLoginError(task.exception)
            }
        }
    }

    private fun handleFirebaseLoginError(exception: Exception?) {
        if (exception is FirebaseAuthException) {
            val errorMessages = mapOf(
                "ERROR_INVALID_EMAIL" to "Invalid email format. Please enter a valid email.",
                "ERROR_WRONG_PASSWORD" to "Incorrect password. Try again.",
                "ERROR_USER_NOT_FOUND" to "No account found with this email. Please sign up first."
            )

            val errorMessage = errorMessages[exception.errorCode] ?: "Login failed: ${exception.localizedMessage}"
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "An unexpected error occurred. Try again.", Toast.LENGTH_LONG).show()
        }
    }

}