package com.example.collectivecleaningorganizer.ui.task

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.collectivecleaningorganizer.R

class TaskActivity : AppCompatActivity() {
    private lateinit var taskName       : TextView
    private lateinit var description    : TextView
    private lateinit var dueDate        : TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userID = intent.getStringExtra("uid")
        if (userID == null) {
            Log.d("TaskOverview: Error", "the userID is null")
            return
        }


        setContentView(R.layout.activity_view_task)
        setTitle("Task description page")


        taskName = findViewById(R.id.taskName)
        dueDate = findViewById(R.id.duedate_tv)
        description = findViewById(R.id.taskDescription)

        intent?.let {
            val task = intent.extras?.getParcelable("task") as TaskModel?
            if (task != null) {
                taskName.text = task.name
                dueDate.text = task.dueDate
                description.text = task.description
            }
        }
        //button = findViewById(R.id.button)
        //button.setOnClickListener {
        //    val intent = Intent(this, FoodMenuActivity::class.java)
        //    startActivity(intent)
        //}
    }

}