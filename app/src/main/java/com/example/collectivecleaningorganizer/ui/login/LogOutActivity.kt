package com.example.collectivecleaningorganizer.ui.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.collectivecleaningorganizer.database.Database
import com.example.collectivecleaningorganizer.R
import com.example.collectivecleaningorganizer.ui.task.TaskOverviewActivity
import com.example.collectivecleaningorganizer.ui.utilities.Utilities
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_log_out.*

class LogOutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_out)

        // gets user id passed via intent from TaskOverviewActivity.kt
        val userId = intent.getStringExtra("uid")
        if (userId == null) {
            Log.d("TaskOverview: Error", "the userID is null")
            return
        }

        // takes user back to task overview
        backToTaskOverviewButton.setOnClickListener {
            val intentTaskPage: Intent = Intent(this, TaskOverviewActivity::class.java)
            intentTaskPage.putExtra("uid",userId)
            startActivity(intentTaskPage)
            this.finish()
        }

        // when user clicks on logout button
        logOutButton.setOnClickListener {
            Utilities().logout(this)
        }

        //Handles the applications navigation
        Utilities().navigation(this, R.id.taskOverView, bottom_navigator_log_out)

    }

}