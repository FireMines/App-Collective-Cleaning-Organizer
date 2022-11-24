package com.example.collectivecleaningorganizer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.collectivecleaningorganizer.ui.login.LoginActivity


/**
 * This is an AppCompatActivity main activity class.
 * It is used to run the app, by sending the user straight to the login page
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Sends the user straight to the login activity
        startActivity(Intent(this, LoginActivity::class.java))

    }

}