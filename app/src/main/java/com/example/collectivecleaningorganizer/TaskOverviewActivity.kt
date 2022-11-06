package com.example.collectivecleaningorganizer

import android.content.ContentValues.TAG
import android.content.Intent
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_task_overview.*

class TaskOverviewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_overview)
        val userID = intent.getStringExtra("uid")

        val intentAddTaskPage: Intent = Intent(this,AddTaskActivity::class.java)
        intentAddTaskPage.putExtra("uid",userID)

        // Starts ForgotPasswordActivity when clicking on forgotPassword
        add_btn.setOnClickListener {
            startActivity(intentAddTaskPage)
        }
    }
}