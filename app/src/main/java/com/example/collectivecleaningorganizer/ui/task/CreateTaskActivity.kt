package com.example.collectivecleaningorganizer.ui.task

import android.app.DatePickerDialog
import android.content.Intent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*

import com.example.collectivecleaningorganizer.Database
import com.example.collectivecleaningorganizer.R
import com.example.collectivecleaningorganizer.ui.collective.SpecificCollectiveActivity
import com.example.collectivecleaningorganizer.ui.friends.FriendsActivity
import com.example.collectivecleaningorganizer.ui.utilities.Utilities
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_create_task.*


import java.util.*
import kotlin.collections.ArrayList

class CreateTaskActivity : AppCompatActivity() {
    //Initializing a tag used to indicate which file Log is writing about
    private val tag = "CreateTaskActivity"

    //Initializing an array list to retrieve the categories data from the snapshot as an arraylist
    private val categoriesArrayListFromSnapshot : ArrayList<String>? = Database.userCollectiveData[0]?.data?.get("categories") as ArrayList<String>?

    //Initializing an array list for categories with a default value of "No category"
    private var categoriesArrayList : ArrayList<String> = arrayListOf("No Category")
    //Initializing the ID of the collective the user is apart of retrieved from the user data
    private val collectiveID = Database.userData[0]?.data?.get("collectiveID")
    override fun onCreate(savedInstanceState: Bundle?) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_task)
        val userID = Database.userData[0]?.id.toString()
        if (userID == null) {
            Log.e(tag, "the userID is null")
            return
        }

        saveOrCreateButton.setOnClickListener{
            //Calling the function used to create the task by adding it to the DB
            createTask()
        }

        //A click listener for the back button.
        back_btn.setOnClickListener{
            //Finishes this (CreateTaskActivity) and goes back to the TaskOverViewActivity
            this.finish()
        }

        //Calling the showCategoriesToChooseFrom() functon to populate the spinner and show the collective categories
        showCategoriesToChooseFrom()
        //Calling the showMembersToAssign() to populate the listview with collective members
        showMembersToAssign()

        val navigationBarView = findViewById<BottomNavigationView>(R.id.bottom_navigator)
        navigationBarView.selectedItemId = R.id.taskOverView

        navigationBarView.setOnItemSelectedListener { it ->
            when(it.itemId) {
                R.id.taskOverView -> {
                    startActivity(Intent(this, TaskOverviewActivity::class.java))
                    true
                }
                R.id.collective -> {
                    startActivity(Intent(this, SpecificCollectiveActivity::class.java))
                    true
                }
                R.id.friends -> {
                    startActivity(Intent(this, FriendsActivity::class.java))
                }
            }
            false
        }

        taskDueDate.setOnClickListener{
            Utilities().showDatePickerDialog(this, taskDueDate)
        }
        deleteCategoryButton.setOnClickListener {
            Utilities().deleteCategory(this,categoriesArrayList,collectiveID.toString())
        }
        createNewCategoryButton.setOnClickListener {
            Utilities().createNewCategory(this,categoriesArrayList,collectiveID.toString())
        }
    }

    /**
     * This is a function that populate the spinner with the id "taskCategories" with categories if there are some
     */
    private fun showCategoriesToChooseFrom() {
        //Checking if the categories data exist in the snapshot
        if (categoriesArrayListFromSnapshot != null && categoriesArrayListFromSnapshot.isNotEmpty()) {
            //Replacing the contents of the categoriesArrayListFromSnapshot into categoriesArrayList
            categoriesArrayList = categoriesArrayListFromSnapshot
            Log.d(tag, "There exists category data in the snapshot. Using the arraylist found in the snapshot")
        }
        else {
            Log.d(tag, "There are no category data in the snapshot. Using an almost empty arraylist with default value: 'No category'")
        }

        //Creating an ArrayAdapter with the "simple_dropdown_item_1line" layout and adding the categoriesArrayList to the adapter
        val categoriesAdapter = ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,categoriesArrayList)

        //Assigning the adapter to the Spinner with the id "taskCategories"
        taskCategories.adapter = categoriesAdapter
    }

    /**
     * A function that shows all the members of the collective. It is shown in the listview with the id "assignCollectiveMembersListView"
     */
    private fun showMembersToAssign() {
        //Retrieving the collective members map from the snapshot
        val collectiveMembersMap : MutableMap<String,String> = Database.userCollectiveData[0]?.data?.get("members") as MutableMap<String, String>

        //Creating an arraylist for the keys from the collective members map
        val collectiveMemberArrayList : ArrayList<String> = ArrayList(collectiveMembersMap.keys)

        //Creating an ArrayAdapter with the "simple_list_item_multiple_choice" layout and adding the collectiveMemberArrayList to the adapter
        val membersAdapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice,collectiveMemberArrayList)

        //Assigning the adapter to the ListView with the id "assignCollectiveMembersListView"
        assignCollectiveMembersListView.adapter = membersAdapter
    }





    /**
     * A function that creates the task by adding all the given task information by the user into a map
     * and adding that map to the task arraylist and updating the database accordingly
     */
    private fun createTask() {
        //Checking if the given username is empty and handling it accordingly
        if (taskName.text.toString() == "") {
            Toast.makeText(this, "Please write a task name in order to create the task", Toast.LENGTH_LONG).show()
            return
        }

        //Initializing a mutable map used to store the members assigned or non assigned members
        val assignedMembers : ArrayList<String> = arrayListOf()

        //Creating a variable of ArrayList<MutableMap<String,Any>>
        var tasksArray : ArrayList<MutableMap<String,Any>>

        //Checking if the collectiveData snapshot doesn't have tasks data
        if (Database.userCollectiveData[0]?.data?.get("tasks") == null) {
            //Initializing the tasksArray variable with an empty arraylist
            tasksArray = ArrayList<MutableMap<String,Any>>()
            Log.d("CreateTaskActivity()","No tasks in the collective")
        }

        else {
            //Initializing the tasksArray variable with the task data from the collectiveData snapshot
            tasksArray = Database.userCollectiveData[0]?.data?.get("tasks") as ArrayList<MutableMap<String, Any>>
        }

        //Initializing a mutable map for the task information
        val taskInformation = mutableMapOf<String,Any>()

        //Iterating through the assignCollectiveMembersListView and checking which members got assigned
        for (i:Int in 0 until assignCollectiveMembersListView.count) {
            //Statement checking if the item's check box is checked
            if (assignCollectiveMembersListView.isItemChecked(i)) {
                //Initializing the member's name retrieved from the assignCollectiveMembersListView row
                val memberName :String = assignCollectiveMembersListView.getItemAtPosition(i).toString()

                //Adding the assigned member's name to the assignedMembers arraylist
                assignedMembers.add(memberName)
            }
        }

        //Adding all the task information to the task map
        taskInformation["name"] = taskName.text.toString()
        taskInformation["description"] = taskDescription.text.toString()
        taskInformation["dueDate"] = taskDueDate.text.toString()
        taskInformation["assigned"] = assignedMembers
        taskInformation["category"] = taskCategories.selectedItem.toString()

        //Adding the task information to the task array which holds all the collective's tasks
        tasksArray.add(taskInformation)

        //Adding the new task array to the DB
        Database().updateValueInDB("collective",collectiveID.toString(),"tasks",tasksArray,null)

        //Finishing the CreateTaskActivity and returning back to the TaskOverviewActivity
        this.finish()
    }


}
