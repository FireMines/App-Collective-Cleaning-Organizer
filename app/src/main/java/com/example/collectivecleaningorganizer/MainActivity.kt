package com.example.collectivecleaningorganizer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.collectivecleaningorganizer.ui.login.LoginActivity



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Sends the user straight to the login activity
        startActivity(Intent(this, LoginActivity::class.java))

    }

}