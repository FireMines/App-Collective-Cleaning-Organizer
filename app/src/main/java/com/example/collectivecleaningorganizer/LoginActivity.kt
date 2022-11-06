package com.example.collectivecleaningorganizer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    // declares instance of firebaseAuth
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Starts ForgotPasswordActivity when clicking on forgotPassword
        forgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        // initializes instance of FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // activates login function when login button is clicked
        logInButton.setOnClickListener {
            login()
        }
    }

    // function to login user
    private fun login() {
        val email = emailLogin.text.toString()
        val password = passwordLogin.text.toString()

        // uses email and password as login credentials
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.d("log in", "login:success")
                Toast.makeText(this, "Successful login", Toast.LENGTH_SHORT).show()   // displays message to user
            /**
                startActivity(Intent(this, ForgotPasswordActivity::class.java))
                Needs different activity here. Used ForgotPasswordActivity as test
            */
            val intentTaskPage: Intent = Intent(this,TaskOverviewActivity::class.java)
                intentTaskPage.putExtra("uid",task.result.user?.uid )
                startActivity(intentTaskPage)
            } else {
                Log.w("log in", "login:failure")
                Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

