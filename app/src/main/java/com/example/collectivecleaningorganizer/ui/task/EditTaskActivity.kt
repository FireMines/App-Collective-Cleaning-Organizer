package com.example.collectivecleaningorganizer.ui.task


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.collectivecleaningorganizer.database.Database
import com.example.collectivecleaningorganizer.R
import com.example.collectivecleaningorganizer.ui.utilities.Utilities
import kotlinx.android.synthetic.main.activity_create_task.back_btn
import kotlinx.android.synthetic.main.activity_edit_task.*
import java.util.*


class EditTaskActivity : AppCompatActivity() {

    var collectiveTasks : ArrayList<MutableMap<String,Any>>? = Database.userCollectiveData[0]?.data?.get("tasks") as ArrayList<MutableMap<String, Any>>?
    val collectiveID = Database.userData[0]?.data?.get("collectiveID")
    lateinit var assigned : ArrayList<String>
    private var categoriesArrayList : ArrayList<String> = arrayListOf("No Category")
    //Initializing an array list to retrieve the categories data from the snapshot as an arraylist
    private val categoriesArrayListFromSnapshot : ArrayList<String>? = Database.userCollectiveData[0]?.data?.get("categories") as ArrayList<String>?
    private val tag : String = "EditTaskActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userID = intent.getStringExtra("uid")
        if (userID == null) {
            Log.d("TaskOverview: Error", "the userID is null")
            return
        }
        // Sets data for assigned
        assigned    = intent.getStringArrayListExtra("assigned") as ArrayList<String>

        setContentView(R.layout.activity_edit_task)
        setTitle("Task description page")

        // Sets data in the intent from the edit texts
        editTaskName.setText(intent.getStringExtra("name").toString())
        editTaskDescription.setText(intent.getStringExtra("description").toString())
        editTaskDueDate.text    = intent.getStringExtra("dueDate").toString()

        //Checking if the categories data exist in the snapshot
        if (categoriesArrayListFromSnapshot != null && categoriesArrayListFromSnapshot.isNotEmpty()) {
            //Replacing the contents of the categoriesArrayListFromSnapshot into categoriesArrayList
            categoriesArrayList = categoriesArrayListFromSnapshot
            Log.d(tag, "There exists category data in the snapshot. Using the arraylist found in the snapshot")
        }

        val catagoryIndex : Int? = categoriesArrayListFromSnapshot?.indexOf(intent.getStringExtra("category"))
        Log.e("Index", catagoryIndex.toString())
        val categoryForTask = ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,categoriesArrayList!!)
        editTaskCategories.adapter = categoryForTask
        editTaskCategories.setSelection(catagoryIndex!!, true)

        // Opens a date picker dialog option
        editTaskDueDate.setOnClickListener{
            Utilities().showDatePickerDialog(this, editTaskDueDate)
        }
        // Deletes a category
        editDeleteCategoryButton.setOnClickListener {
            Utilities().deleteCategory(this,categoriesArrayList,collectiveID.toString())
        }
        // Creates a new category
        editCreateNewCategoryButton.setOnClickListener {
            Utilities().createNewCategory(this,categoriesArrayList,collectiveID.toString())
        }
        // Saves/ updates the task you are in to the database
        saveOrUpdateButton.setOnClickListener {
            updateTask()
        }

        //A click listener for the back button.
        back_btn.setOnClickListener {
            //Finishes this (EditTaskActivity) and goes back to the TaskActivity
            startActivity(Intent(this, TaskOverviewActivity::class.java))
            //Finishing the current Activity
            this.finish()
        }
        showMembersToAssign()

        //Handles the applications navigation
        Utilities().navigation(this,R.id.taskOverView,bottom_navigator_edit_task)
    }

    private fun updateTask() {
        try {
            //Checking if the given username is empty and handling it accordingly
            if (editTaskName.text.toString() == "") {
                Toast.makeText(
                    this,
                    "Please write a task name in order to create the task",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
            val assignedMembers: ArrayList<String> = arrayListOf()

            //Iterating through the assignCollectiveMembersListView and checking which members got assigned
            for (i: Int in 0 until editAssignCollectiveMembersListView.count) {
                //Statement checking if the item's check box is checked
                if (editAssignCollectiveMembersListView.isItemChecked(i)) {
                    //Initializing the member's name retrieved from the assignCollectiveMembersListView row
                    val memberName: String =
                        editAssignCollectiveMembersListView.getItemAtPosition(i).toString()

                    //Adding the assigned member's name to the assignedMembers arraylist
                    assignedMembers.add(memberName)
                }
            }

            //Initializing a mutable map for the task information
            val taskInformation = mutableMapOf<String, Any>()

            //Adding all the task information to the task map
            taskInformation["name"] = editTaskName.text.toString()
            taskInformation["description"] = editTaskDescription.text.toString()
            taskInformation["dueDate"] = editTaskDueDate.text.toString()
            taskInformation["assigned"] = assignedMembers
            taskInformation["category"] = editTaskCategories.selectedItem.toString()
            collectiveTasks?.set(intent.getIntExtra("index", 0), taskInformation)

            Database().updateValueInDB(
                "collective",
                collectiveID.toString(),
                "tasks",
                collectiveTasks,
                null
            )

            //Finishing the EditTaskActivity and returning back to the TaskOverviewActivity
            startActivity(Intent(this, TaskOverviewActivity::class.java))
            //Finishing the current Activity
            this.finish()
        }
        catch (error : Exception) {
            Toast.makeText(this, "An error occurred when trying to update the task. Try again ", Toast.LENGTH_LONG).show()
            Log.e(tag, "Error when trying to run the updateTask() function",error)
        }
    }

    /**
     * A function that shows all the members of the collective. It is shown in the listview with the id "assignCollectiveMembersListView"
     */
    private fun showMembersToAssign() {
        try {
            //Retrieving the collective members map from the snapshot
            val collectiveMembersMap: MutableMap<String, String> =
                Database.userCollectiveData[0]?.data?.get("members") as MutableMap<String, String>

            //Creating an arraylist for the keys from the collective members map
            val collectiveMemberArrayList: ArrayList<String> = ArrayList(collectiveMembersMap.keys)

            //Creating an ArrayAdapter with the "simple_list_item_multiple_choice" layout and adding the collectiveMemberArrayList to the adapter
            val membersAdapter = ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_multiple_choice,
                collectiveMemberArrayList
            )

            //Assigning the adapter to the ListView with the id "assignCollectiveMembersListView"
            editAssignCollectiveMembersListView.adapter = membersAdapter

            for (i: Int in 0 until assigned.size) {
                val assignedName: String = assigned[i]
                val indexOfAssignedName = collectiveMemberArrayList.indexOf(assignedName)
                editAssignCollectiveMembersListView.setItemChecked(indexOfAssignedName, true)
            }
        }
        catch (error : Exception) {
            Toast.makeText(this, "An error occurred when trying to show members to assing in the task. Try again ", Toast.LENGTH_LONG).show()
            Log.e(tag, "Error when trying to run the showMembersToAssign() function",error)
        }
    }


}
