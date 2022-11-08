package com.example.collectivecleaningorganizer

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_task_overview.*


class TaskOverviewActivity : AppCompatActivity() {

    private val tasklist = mutableListOf<TaskModel>()
    private lateinit var recyclerView: RecyclerView


    private val db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_overview)
        val userID = intent.getStringExtra("uid")
        if (userID == null) {
            Log.d("TaskOverview: Error", "the userID is null")
            return
        }

        //recyclerView = findViewById(R.id.rv_todo)
        //val adapter = TaskPageAdapter(tasklist)
        //recyclerView.adapter = adapter




        //Retrieving user's tasks
        db.collection("users").document(userID).collection("tasks").get().addOnSuccessListener { tasks ->
            for (task in tasks) {
                Log.d(TAG, "${task.id} => ${task.data}")
                //Log.d("entries here: ", task.data)
                tasklist.add(TaskModel(task.data["name"] as String, task.data["dueDate"] as String, task.data["description"] as String))
                Log.d("Hallaballa", tasklist.toString())
                //adapter.notifyDataSetChanged()
            }
        }

        Log.d(TAG, tasklist.toString())
/*
        adapter.setOnItemClickListener(object: TaskPageAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val intent = Intent(this@TaskOverviewActivity, TaskActivity::class.java)
                val taskModel = tasklist[position]
                intent.putExtra("task",taskModel)
                startActivity(intent)
            }
        })
*/

        val intentAddTaskPage: Intent = Intent(this,AddTaskActivity::class.java)
        intentAddTaskPage.putExtra("uid",userID)


        // Starts addTaskActivity when clicking on Add Task button
        add_btn.setOnClickListener {
            startActivity(intentAddTaskPage)
        }

    }
}