package com.example.collectivecleaningorganizer.ui.task

import android.content.ContentValues.TAG
import android.content.Intent
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.collectivecleaningorganizer.R
import kotlinx.android.synthetic.main.activity_edit_task.*
import kotlinx.android.synthetic.main.activity_view_task.*
import kotlinx.android.synthetic.main.activity_view_task.assignCollectiveMembersListView
import kotlinx.android.synthetic.main.activity_view_task.back_btn
import kotlinx.android.synthetic.main.activity_view_task.taskDescription
import kotlinx.android.synthetic.main.activity_view_task.taskDueDate
import kotlinx.android.synthetic.main.activity_view_task.taskName
import kotlinx.android.synthetic.main.activity_view_task.view.*
import kotlinx.android.synthetic.main.task_layout.view.*


class TaskActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userID = intent.getStringExtra("uid")
        if (userID == null) {
            Log.d("TaskOverview: Error", "the userID is null")
            return
        }

        setContentView(R.layout.activity_view_task)
        setTitle("Task description page")


        taskName.text           = intent.getStringExtra("name").toString()
        taskDueDate.text        = intent.getStringExtra("dueDate").toString()
        taskDescription.text    = intent.getStringExtra("description").toString()
        val assigned : ArrayList<String>   = intent.getStringArrayListExtra("assigned") as ArrayList<String>

        val listOfAssigned = mutableListOf<String>()
        listOfAssigned.add(intent.getStringExtra("assigned").toString())

        val membersAdapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice,assigned)
        //Assigning the adapter to the ListView with the id "assignCollectiveMembersListView"
        assignCollectiveMembersListView.adapter = membersAdapter

        for (i : Int in 0 until assigned.size) {
            assignCollectiveMembersListView.setItemChecked(i,true)
        }
        assignCollectiveMembersListView.isEnabled = false


        val categories = mutableListOf<String>()
        categories.add(intent.getStringExtra("category").toString())

        val categoryForTask = ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,categories)
        taskCategories.adapter = categoryForTask


        editTask_btn.setOnClickListener{
            editTaskPage(taskName.text.toString(),taskDueDate.text.toString(), taskDescription.text.toString(), intent.getStringExtra("category").toString(), assigned, userID)
        }


        back_btn.setOnClickListener{
            val intentTaskPage: Intent = Intent(this, TaskOverviewActivity::class.java)
            intentTaskPage.putExtra("uid",userID)
            startActivity(intentTaskPage)

        }
    }


    private fun editTaskPage(name : String, dueDate : String, description : String, category : String, assigned : ArrayList<String>, userID: String) {
        val newIntent = Intent(this, EditTaskActivity::class.java)

        newIntent.putExtra("uid",userID)
        newIntent.putExtra("name",name)
        newIntent.putExtra("dueDate", dueDate)
        newIntent.putExtra("description", description)
        newIntent.putExtra("assigned", assigned)
        newIntent.putExtra("category", category)
        newIntent.putExtra("index", intent.getIntExtra("index",0))


        startActivity(newIntent)
    }
}