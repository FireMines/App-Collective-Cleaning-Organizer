package com.example.collectivecleaningorganizer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_task_overview.*

class TaskOverviewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_overview)

        // Starts ForgotPasswordActivity when clicking on forgotPassword
        add_btn.setOnClickListener {
            startActivity(Intent(this, AddTaskActivity::class.java))
        }
    }
}