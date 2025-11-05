package com.example.smartbizhelper

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        auth = FirebaseAuth.getInstance()

        Handler(Looper.getMainLooper()).postDelayed({
            // Check if the user is already logged in
            if (auth.currentUser != null) {
                // User is logged in, go to HomeActivity
                startActivity(Intent(this, HomeActivity::class.java))
            } else {
                // User is not logged in, go to LoginActivity
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish() // Finish SplashActivity so the user can't go back to it
        }, 3000) // 3-second delay
    }
}
