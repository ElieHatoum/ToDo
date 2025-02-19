package com.example.todo

import android.os.Bundle

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.todo.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import android.content.Intent
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuthException
import java.lang.Exception

class Signup : AppCompatActivity() {
    private lateinit var auth : FirebaseAuth
    private lateinit var binding : ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Initialize
        auth = Firebase.auth

        //Set up button click listener
        binding.signupButton.setOnClickListener{
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            val confirmPassword = binding.confirmPassword.text.toString()

            if(email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()){
                if(password == confirmPassword){
                    createAccount(email, password)
                }else {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvRedirectLogin.setOnClickListener{
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }

    private fun createAccount(email : String, password : String){
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){ task ->
            if(task.isSuccessful){
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
                finish()
            }else{
                handleFirebaseSignupError(task.exception)
            }
        }
    }

    private fun handleFirebaseSignupError(exception: Exception?) {
        if (exception is FirebaseAuthException) {
            val errorMessages = mapOf(
                "ERROR_EMAIL_ALREADY_IN_USE" to "This email is already registered. Try logging in.",
                "ERROR_INVALID_EMAIL" to "Invalid email format. Please enter a valid email.",
                "ERROR_WEAK_PASSWORD" to "Weak password! Must be at least 6 characters long.",
                "ERROR_OPERATION_NOT_ALLOWED" to "Account creation is currently disabled. Contact support."
            )

            val errorMessage = errorMessages[exception.errorCode] ?: "Signup failed: ${exception.localizedMessage}"
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "An unexpected error occurred. Try again.", Toast.LENGTH_LONG).show()
        }
    }

}