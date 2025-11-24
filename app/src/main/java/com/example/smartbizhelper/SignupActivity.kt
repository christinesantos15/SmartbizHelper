package com.example.smartbizhelper

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()

        val fullNameInput = findViewById<EditText>(R.id.full_name_input)
        val emailInput = findViewById<EditText>(R.id.email_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)
        val signupButton = findViewById<Button>(R.id.signup_button)
        val loginPrompt = findViewById<TextView>(R.id.login_prompt)

        signupButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            val fullName = fullNameInput.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && fullName.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign up success, go back to LoginActivity
                            Toast.makeText(baseContext, "Sign up successful! Please log in.", Toast.LENGTH_LONG).show()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        } else {
                            // If sign up fails, display a message to the user.
                            Toast.makeText(baseContext, "Authentication failed: ${task.exception?.message}",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show()
            }
        }

        loginPrompt.setOnClickListener {
            finish() // Go back to the login screen
        }
    }
}
