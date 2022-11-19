package com.example.collectivecleaningorganizer.ui.task


import android.app.DatePickerDialog
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.collectivecleaningorganizer.Database
import com.example.collectivecleaningorganizer.R
import com.example.collectivecleaningorganizer.ui.utilities.Utilities
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_create_task.*
import kotlinx.android.synthetic.main.activity_create_task.assignCollectiveMembersListView
import kotlinx.android.synthetic.main.activity_create_task.back_btn
import kotlinx.android.synthetic.main.activity_create_task.taskCategories
import kotlinx.android.synthetic.main.activity_create_task.taskDescription
import kotlinx.android.synthetic.main.activity_create_task.taskDueDate
import kotlinx.android.synthetic.main.activity_create_task.taskName
import kotlinx.android.synthetic.main.activity_edit_task.*
import kotlinx.android.synthetic.main.activity_view_task.*
import java.util.*


class EditTaskActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userID = intent.getStringExtra("uid")
        if (userID == null) {
            Log.d("TaskOverview: Error", "the userID is null")
            return
        }
        //Initializing a mutable map for the task information
        val taskInformation = mutableMapOf<String,Any>()
        //var tasksArray : ArrayList<MutableMap<String,Any>>


        setContentView(R.layout.activity_edit_task)
        setTitle("Task description page")

        Log.e("YOOO", "PAPA")
        Log.e("NAMEyoyo:", intent.getStringExtra("name").toString())
        editTaskName.setText(intent.getStringExtra("name").toString())
        editTaskDescription.setText(intent.getStringExtra("descriptiondueDate").toString())
        editTaskDueDate.text    = intent.getStringExtra("dueDate").toString()
        //editAssignCollectiveMembersListView.text    = intent.getStringExtra("description").toString()

        val listOfAssigned = mutableListOf<String>()
        listOfAssigned.add(intent.getStringExtra("assigned").toString())

        val membersAdapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice,listOfAssigned)
        //Assigning the adapter to the ListView with the id "assignCollectiveMembersListView"
        //assignCollectiveMembersListView.adapter = membersAdapter



        val categories = mutableListOf<String>()
        categories.add(intent.getStringExtra("category").toString())

        val categoryForTask = ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,categories)
        //taskCategories.adapter = categoryForTask

        saveOrUpdateButton.setOnClickListener {
            //Adding all the task information to the task map
            taskInformation["name"] = editTaskName.text.toString()
            taskInformation["description"] = editTaskDescription.text.toString()
            taskInformation["dueDate"] = editTaskDueDate.text.toString()
            //taskInformation["assigned"] = assignedMembers
            //taskInformation["category"] = editTaskCategories.selectedItem.toString()

            Database().changeTaskDataInDB("collective", userID, taskInformation)
            //Adding the task information to the task array which holds all the collective's tasks
            //tasksArray.add(taskInformation)

            //Adding the new task array to the DB
            //Database().updateValueInDB("collective",collectiveID.toString(),"tasks",tasksArray,null)

        }

        //A click listener for the back button.
        back_btn.setOnClickListener {
            //Finishes this (CreateTaskActivity) and goes back to the TaskOverViewActivity
            this.finish()
        }
    }


}
