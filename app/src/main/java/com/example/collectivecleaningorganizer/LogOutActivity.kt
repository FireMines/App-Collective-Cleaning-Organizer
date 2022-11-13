package com.example.collectivecleaningorganizer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.collectivecleaningorganizer.ui.task.TaskOverviewActivity
import kotlinx.android.synthetic.main.activity_add_task.*
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
        }


        //back_btn.setOnClickListener{
        //    val intentTaskPage: Intent = Intent(this, TaskOverviewActivity::class.java)
        //    intentTaskPage.putExtra("uid",userID)
        //    startActivity(intentTaskPage)
        //}

    }
}