package com.example.collectivecleaningorganizer.ui.task

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.iterator
import androidx.recyclerview.widget.RecyclerView
import com.example.collectivecleaningorganizer.LogOutActivity
import com.example.collectivecleaningorganizer.R
import com.example.collectivecleaningorganizer.userCollectiveData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_task_overview.*
import kotlinx.android.synthetic.main.activity_view_task.view.*
import kotlinx.android.synthetic.main.task_layout.view.*


class TaskOverviewActivity : AppCompatActivity() {

    private val tasklist = mutableListOf<TaskModel>()
    //private lateinit var recyclerView: RecyclerView


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
        val adapter = TaskPageAdapter(tasklist)
        //recyclerView.adapter = adapter

        dbSync(userID)

        Log.d(TAG, tasklist.toString())
/*
        adapter.setOnItemClickListener(object: TaskPageAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val intent = Intent(this@TaskOverviewActivity, TaskActivity::class.java)
                val taskModel = tasklist[position]
                intent.putExtra("task",taskModel)
                startActivity(intent)
            }
        })*/


        val intentAddTaskPage: Intent = Intent(this, AddTaskActivity::class.java)
        intentAddTaskPage.putExtra("uid",userID)


        // Starts addTaskActivity when clicking on Add Task button
        add_btn.setOnClickListener {
            startActivity(intentAddTaskPage)
        }

        // passes user id to intent
        val intentLogoutPage: Intent = Intent(this, LogOutActivity::class.java)
        intentLogoutPage.putExtra("uid",userID)

        // Sends user to logout activity when clicking on settings wheel
        logoutImageView.setOnClickListener {
            startActivity(intentLogoutPage)
        }
    }

    private fun dbSync(userID : String) {
        removeAllRecipes()
        //Retrieving user's tasks
        val collectiveTasks : ArrayList<MutableMap<String,String>> = userCollectiveData[0]?.data?.get("tasks") as ArrayList<MutableMap<String, String>>
        for (task in collectiveTasks) {
            val view = layoutInflater.inflate(R.layout.task_layout, null)
            view.task_tv.text = task["name"]
            view.duedate_tv.text = task["dueDate"]
            val desc = task["description"].toString()
            view.task_tv.setOnClickListener{
                openTaskPage(view.task_tv.text.toString(),view.duedate_tv.text.toString(), desc, userID)
                //tasklist.add(TaskModel(view.task_tv.text as String,view.duedate_tv.text as String, view.taskDescription.text as String))
            }
            rv_todo.addView(view)

        }

    }

    private fun openTaskPage(name : String, dueDate : String, description : String, userID: String) {
        val newIntent = Intent(this, TaskActivity::class.java)

        newIntent.putExtra("uid",userID)
        newIntent.putExtra("name",name)
        newIntent.putExtra("dueDate", dueDate)
        newIntent.putExtra("description", description)

        startActivity(newIntent)
    }


    private fun removeAllRecipes(){
        val i = rv_todo.iterator()
        while (i.hasNext()){
            i.next()
            i.remove()
        }
    }
}

