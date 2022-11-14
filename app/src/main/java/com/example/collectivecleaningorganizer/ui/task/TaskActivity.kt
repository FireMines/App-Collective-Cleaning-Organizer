package com.example.collectivecleaningorganizer.ui.task

import android.content.ContentValues.TAG
import android.content.Intent
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.collectivecleaningorganizer.R
import kotlinx.android.synthetic.main.activity_add_task.*

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
        dueDate = findViewById(R.id.taskDueDate)
        description = findViewById(R.id.taskDescription)



        val name = intent.getStringExtra("name").toString()
        //val dueDate = intent.getStringExtra("dueDate").toString()
        val desc = intent.getStringExtra("description").toString()
        Log.d(TAG, name)

        taskName.text = name
        //dueDate = "hei"
        description.text = desc

        back_btn.setOnClickListener{
            val intentTaskPage: Intent = Intent(this, TaskOverviewActivity::class.java)
            intentTaskPage.putExtra("uid",userID)
            startActivity(intentTaskPage)
        }


        /*
        intent?.let {
            val task = intent.extras?.getParcelable("task") as TaskModel?
            if (task != null) {
                taskName.text = task.name
                dueDate.text = task.dueDate
                description.text = task.description
            }
        }

         */
        //button = findViewById(R.id.button)
        //button.setOnClickListener {
        //    val intent = Intent(this, FoodMenuActivity::class.java)
        //    startActivity(intent)
        //}
    }

}