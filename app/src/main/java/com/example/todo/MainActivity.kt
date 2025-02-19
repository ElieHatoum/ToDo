package com.example.todo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Check if the user is logged in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            //User logged in -> go to Home activity
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        } else {
            // User is not logged in, go to Login activity
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
        finish()
    }
}