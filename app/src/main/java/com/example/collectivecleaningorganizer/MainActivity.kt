package com.example.collectivecleaningorganizer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Sends the user straight to the login activity
        startActivity(Intent(this, LoginActivity::class.java))

        /*
        val intentAddTask: Intent = Intent(this,AddTaskActivity::class.java)
        startActivity(intentAddTask)

         */
        /*
        val newIntent = Intent(this, FriendsActivity::class.java)
        startActivity(newIntent)

         */

    }
}