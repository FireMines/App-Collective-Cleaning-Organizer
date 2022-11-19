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

    var collectiveTasks : ArrayList<MutableMap<String,Any>>? = Database.userCollectiveData[0]?.data?.get("tasks") as ArrayList<MutableMap<String, Any>>?
    val collectiveID = Database.userData[0]?.data?.get("collectiveID")
    lateinit var assigned : ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userID = intent.getStringExtra("uid")
        if (userID == null) {
            Log.d("TaskOverview: Error", "the userID is null")
            return
        }

        assigned    = intent.getStringArrayListExtra("assigned") as ArrayList<String>

        Log.e("assigned here", assigned.toString())

        //Initializing a mutable map for the task information
        val taskInformation = mutableMapOf<String,Any>()
        //var tasksArray : ArrayList<MutableMap<String,Any>>

        //Initializing the ID of the collective the user is apart of retrieved from the user data

        setContentView(R.layout.activity_edit_task)
        setTitle("Task description page")

        Log.e("YOOO", "PAPA")
        Log.e("NAMEyoyo:", intent.getStringExtra("name").toString())
        editTaskName.setText(intent.getStringExtra("name").toString())
        editTaskDescription.setText(intent.getStringExtra("description").toString())
        editTaskDueDate.text    = intent.getStringExtra("dueDate").toString()
        //editAssignCollectiveMembersListView.text    = intent.getStringExtra("description").toString()


        val categories : ArrayList<String>? = Database.userCollectiveData[0]?.data?.get("categories") as ArrayList<String>?
        val catagoryIndex : Int? = categories?.indexOf(intent.getStringExtra("category"))
        //categories.add()

        val categoryForTask = ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,categories!!)
        editTaskCategories.adapter = categoryForTask
        editTaskCategories.setSelection(catagoryIndex!!, true)




        saveOrUpdateButton.setOnClickListener {
            updateTask()
        }

        //A click listener for the back button.
        back_btn.setOnClickListener {
            //Finishes this (CreateTaskActivity) and goes back to the TaskOverViewActivity
            this.finish()
        }
        showMembersToAssign()
    }

    private fun updateTask() {
        //Checking if the given username is empty and handling it accordingly
        if (editTaskName.text.toString() == "") {
            Toast.makeText(this, "Please write a task name in order to create the task", Toast.LENGTH_LONG).show()
            return
        }
        val assignedMembers : ArrayList<String> = arrayListOf()

        //Iterating through the assignCollectiveMembersListView and checking which members got assigned
        for (i:Int in 0 until editAssignCollectiveMembersListView.count) {
            //Statement checking if the item's check box is checked
            if (editAssignCollectiveMembersListView.isItemChecked(i)) {
                //Initializing the member's name retrieved from the assignCollectiveMembersListView row
                val memberName :String = editAssignCollectiveMembersListView.getItemAtPosition(i).toString()

                Log.e("membername", memberName)
                //Adding the assigned member's name to the assignedMembers arraylist
                assignedMembers.add(memberName)
            }
        }

        //Initializing a mutable map used to store the members assigned or non assigned members

        //Initializing a mutable map for the task information
        val taskInformation = mutableMapOf<String,Any>()

        //Adding all the task information to the task map
        taskInformation["name"] = editTaskName.text.toString()
        taskInformation["description"] = editTaskDescription.text.toString()
        taskInformation["dueDate"] = editTaskDueDate.text.toString()
        taskInformation["assigned"] = assignedMembers
        taskInformation["category"] = editTaskCategories.selectedItem.toString()
        collectiveTasks?.set(intent.getIntExtra("index",0), taskInformation)

        Database().updateValueInDB("collective",collectiveID.toString(),"tasks",collectiveTasks,null)

        //Finishing the CreateTaskActivity and returning back to the TaskOverviewActivity
        this.finish()
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
        editAssignCollectiveMembersListView.adapter = membersAdapter

        for (i : Int in 0 until assigned.size) {
            val assignedName : String= assigned[i]
            val indexOfAssignedName = collectiveMemberArrayList.indexOf(assignedName)
            editAssignCollectiveMembersListView.setItemChecked(indexOfAssignedName,true)
        }
    }
}
