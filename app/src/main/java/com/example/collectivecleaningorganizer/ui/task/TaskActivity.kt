package com.example.collectivecleaningorganizer.ui.task

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.collectivecleaningorganizer.R
import com.example.collectivecleaningorganizer.ui.utilities.Utilities
import kotlinx.android.synthetic.main.activity_edit_task.*
import kotlinx.android.synthetic.main.activity_friends.*
import kotlinx.android.synthetic.main.activity_view_task.*
import kotlinx.android.synthetic.main.activity_view_task.assignCollectiveMembersListView
import kotlinx.android.synthetic.main.activity_view_task.back_btn
import kotlinx.android.synthetic.main.activity_view_task.taskDescription
import kotlinx.android.synthetic.main.activity_view_task.taskDueDate
import kotlinx.android.synthetic.main.activity_view_task.taskName
import kotlinx.android.synthetic.main.activity_view_task.view.*
import kotlinx.android.synthetic.main.task_layout.view.*

/**
 * This is an AppCompatActivity class for the TaskActivity
 * It is used to create a page where the user can see the full details of the task he/she clicked
 */
class TaskActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userID = intent.getStringExtra("uid")
        if (userID == null) {
            Log.d("TaskOverview: Error", "the userID is null")
            return
        }
        setContentView(R.layout.activity_view_task)
        //Setting the title of the layout
        setTitle("Task description page")

        //Retrieving the task details from the intent
        taskName.text           = intent.getStringExtra("name").toString()
        taskDueDate.text        = intent.getStringExtra("dueDate").toString()
        taskDescription.text    = intent.getStringExtra("description").toString()

        //Creating an arraylist for the assigned members
        val assigned : ArrayList<String>   = intent.getStringArrayListExtra("assigned") as ArrayList<String>

        // Makes list of all users assigned to a task
        val listOfAssigned = mutableListOf<String>()
        listOfAssigned.add(intent.getStringExtra("assigned").toString())

        val membersAdapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice,assigned)
        //Assigning the adapter to the ListView with the id "assignCollectiveMembersListView"
        assignCollectiveMembersListView.adapter = membersAdapter

        // Set all assigned users to have their checkbox checked
        for (i : Int in 0 until assigned.size) {
            assignCollectiveMembersListView.setItemChecked(i,true)
        }
        assignCollectiveMembersListView.isEnabled = false

        // Makes list of all categories to a task
        val categories = mutableListOf<String>()
        categories.add(intent.getStringExtra("category").toString())

        val categoryForTask = ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,categories)
        taskCategories.adapter = categoryForTask

        // Opens the edit task activity
        editTask_btn.setOnClickListener{
            editTaskPage(taskName.text.toString(),taskDueDate.text.toString(), taskDescription.text.toString(), intent.getStringExtra("category").toString(), assigned, userID)
        }

        // Takes the user back to the Task Overview Activity
        back_btn.setOnClickListener{
            //Starting the activity called "TaskOverViewActivity"
            startActivity(Intent(this, TaskOverviewActivity::class.java))
            //Finishing the current Activity
            this.finish()
        }

        //Handles the applications navigation
        Utilities().navigation(this,R.id.taskOverView,bottom_navigator_task_activity)
    }

    /**
     * This is a function that opens the edit Task page
     * @param name is the name of the task you want to enter
     * @param dueDate is the due date of the task you want to enter
     * @param description is the description of the task you want to enter
     * @param category is the category of the task you want to enter
     * @param assigned is which person(s) assigned to the task you want to enter
     * @param index is the index of the task you are entering
     * @param userID is the userID of the current user
     */
    private fun editTaskPage(name : String, dueDate : String, description : String, category : String, assigned : ArrayList<String>, userID: String) {
        try {
            val newIntent = Intent(this, EditTaskActivity::class.java)

            newIntent.putExtra("uid", userID)
            newIntent.putExtra("name", name)
            newIntent.putExtra("dueDate", dueDate)
            newIntent.putExtra("description", description)
            newIntent.putExtra("assigned", assigned)
            newIntent.putExtra("category", category)
            newIntent.putExtra("index", intent.getIntExtra("index", 0))

            startActivity(newIntent)
            //Finishing the current Activity
            this.finish()
        }
        catch (error : Exception) {
            Toast.makeText(this, "An error occurred when trying to edit the task. Try again ", Toast.LENGTH_LONG).show()
            Log.e(TAG, "Error when trying to run the editTaskPage() function",error)
        }
    }
}