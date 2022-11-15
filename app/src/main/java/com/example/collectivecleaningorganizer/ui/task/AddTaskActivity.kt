package com.example.collectivecleaningorganizer.ui.task

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.*
import androidx.core.view.size
import com.example.collectivecleaningorganizer.Database
import com.example.collectivecleaningorganizer.R
import com.example.collectivecleaningorganizer.userCollectiveData
import com.example.collectivecleaningorganizer.userData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_add_task.*
import java.util.*
import kotlin.collections.ArrayList

class AddTaskActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    private val tag = "AddTaskActivity"
    private val categoriesArrayListFromCache : ArrayList<String>? = userCollectiveData[0]?.data?.get("categories") as ArrayList<String>?
    private var categoriesArrayList : ArrayList<String> = arrayListOf("No Category")
    override fun onCreate(savedInstanceState: Bundle?) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        val userID = userData[0]?.id.toString()//intent.getStringExtra("uid")
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
            //Finishes this (AddTaskActivity) and goes back to the TaskOverViewActivity
            this.finish()
        }

        //Calling the showCategoriesToChooseFrom() functon to populate the spinner and show the collective categories
        showCategoriesToChooseFrom()
        //Calling the showMembersToAssign() to populate the listview with collective members
        showMembersToAssign()

    }

    /**
     * This is a function that populate the spinner with the id "taskCategories" with categories if there are some
     */
    private fun showCategoriesToChooseFrom() {
        //Checking if the categories list doesn't exist in the cached data
        if (categoriesArrayListFromCache != null) {
            Log.d(tag, "There are no category data in the cached data")
            //Initializing the categoriesArrayList which has a value "No Category"
            categoriesArrayList = categoriesArrayListFromCache
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
        //Retrieving the collective members map from the stored cache data
        val collectiveMembersMap : MutableMap<String,String> = userCollectiveData[0]?.data?.get("members") as MutableMap<String, String>
        //Creating an arraylist for the keys from the collective members map
        val collectiveMemberArrayList : ArrayList<String> = ArrayList(collectiveMembersMap.keys)

        //Creating an ArrayAdapter with the "simple_list_item_multiple_choice" layout and adding the collectiveMemberArrayList to the adapter
        val membersAdapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice,collectiveMemberArrayList)
        //Assigning the adapter to the ListView with the id "assignCollectiveMembersListView"
        assignCollectiveMembersListView.adapter = membersAdapter

    }
    fun createNewCategory(view: View) {
        //Initializing an EditText widget
        val inputEditTextField = EditText(this)
        //Initializing a variable to get the text value from the input Edit text field
        val userInput = inputEditTextField.text.toString()
        //Initializing an AlertDialog and building a dialog that allows the user to write a category name
        val dialog = AlertDialog.Builder(this)
            .setTitle("New category")
            .setMessage("Enter the name of the category you want to create")
            .setView(inputEditTextField)
            .setPositiveButton("Create") { _, _ ->
                //Checking if the input is empty and handling it accordingly
                if (userInput == "") {
                    Toast.makeText(this, "The category wasn't created. Reason: Name was empty", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }
                //Checking if there exists a category with the same name as the user input
                if (checkIfCategoryExists(categoriesArrayList, userInput)) {
                    Log.e("OH NO","IT CONTAINS")
                    Toast.makeText(this, "The category wasn't created. Reason: There is already exists a category with that name", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }
                //Getting the categories array listed from the userCollective cache data
                //Getting the ID of the collective the user is apart of
                val collectiveID = userData[0]?.data?.get("collectiveID")
                //Checking if the categories list dosnt exist in the cached data

                categoriesArrayList.add(inputEditTextField.text.toString())
                this.onStop()
                //Storing the categoriesArrayList to the DB
                Database().updateValueInDB("collective",collectiveID.toString(),"categories",categoriesArrayList,null)
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()

    }

    /**
     * A function that checks if there is a category existing with the same name. The check is case insensitive
     * @param list the arraylist which holds all the categories to check in
     * @param nameToCheck the name of the category we want to check for
     */
    private fun checkIfCategoryExists(list: ArrayList<String>, nameToCheck : String) : Boolean{
        //Iterating through the array list
        for (i in list) {
            //Checking if the value in the list is the same as the value we are checking for. Case insensitive
            if (i.lowercase() == nameToCheck.lowercase() ) {
                return true
            }
        }
        return false
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
        val assignedMembers : MutableMap<String, Boolean> = mutableMapOf()

        //Creating a variable of ArrayList<MutableMap<String,Any>>
        var tasksArray : ArrayList<MutableMap<String,Any>>

        //Checking if the collectiveData cache doesn't have tasks data
        if (userCollectiveData[0]?.data?.get("tasks") == null) {
            //Initializing the tasksArray variable with an empty arraylist
            tasksArray = ArrayList<MutableMap<String,Any>>()
            Log.d("AddTaskActivity()","No tasks in the collective")
        }
        else {
            //Initializing the tasksArray variable with the task data from the collectiveData cache
            tasksArray = userCollectiveData[0]?.data?.get("tasks") as ArrayList<MutableMap<String, Any>>
        }

        //Initializing a mutable map for the task information
        val taskInformation = mutableMapOf<String,Any>()

        //Iterating through the assignCollectiveMembersListView and checking which members got assigned
        for (i in 0 until assignCollectiveMembersListView.size) {
            //Initializing the name of the member retrieved from the assignCollectiveMembersListView
            val memberName :String = assignCollectiveMembersListView.getItemAtPosition(i).toString()
            //Initializing the boolean value of the member retrieved from the assignCollectiveMembersListView
            val memberIsAssigned : Boolean = assignCollectiveMembersListView.isItemChecked(i)
            //Adding the member to a map with the value true or false depending on if the user got clicked on in the list
            assignedMembers[memberName] = memberIsAssigned

        }
        //Adding all the task information to the task map
        taskInformation["name"] = taskName.text.toString()
        taskInformation["description"] = taskDescription.text.toString()
        taskInformation["dueDate"] = taskDueDate.text.toString()
        taskInformation["assigned"] = assignedMembers
        taskInformation["category"] = taskCategories.selectedItem.toString()


        //Adding the task information to the task array which holds all the collective's tasks
        tasksArray.add(taskInformation)

        //Initializing a variable with the collectiveID found in the userData
        val collectiveID = userData[0]?.data?.get("collectiveID").toString()

        //Adding the new task array to the DB
        Database().updateValueInDB("collective",collectiveID,"tasks",tasksArray,null)

        //Finishing the AddTaskActivity and returning back to the TaskOverviewActivity
        this.finish()

    }

    /**
     * A function that is called everytime the user clicks on the TextView element with the id "taskDueDate"
     * It shows a date picker dialog where the user can choose a due date
     */
    fun showDatePickerDialog(view: View) {
        //Creating a calander instance
        val calendar = Calendar.getInstance()
        //Getting the current day
        var day : Int = calendar.get(Calendar.DAY_OF_MONTH)
        //Getting the current month
        var month : Int = calendar.get(Calendar.MONTH)
        //Getting the current year
        var year : Int = calendar.get(Calendar.YEAR)

        //Checking if the the user has already chosen a date, and getting the datePickerDialog to show that date instead of current date
        if (taskDueDate.text != "") {
            //Splitting the taskDueDate to get the day, month and year
            val dueDate : List<String> = taskDueDate.text.split("/")
            day = dueDate.get(0).toInt()
            //Decrementing the month to show the correct month in the dialog as the calander instance's month is from range 0 to 11.
            month = dueDate.get(1).toInt().dec()
            year = dueDate.get(2).toInt()
        }
        val datePickerDialog = DatePickerDialog(this,DatePickerDialog.OnDateSetListener { datePicker : DatePicker, pickedYear: Int, pickedMonth: Int, pickedDay : Int ->
            //Setting the date into the EditText element with the id "taskDueDate".
            //Incrementing the month to give the correct month value as the calander instance's month is from range 0 to 11.
            taskDueDate.setText("$pickedDay/${pickedMonth.inc()}/$pickedYear")

        }, year, month, day)

        //Not allowing the dialog to show past dates
        datePickerDialog.datePicker.minDate = calendar.getTimeInMillis();

        //Showing the datepicker dialog
        datePickerDialog.show()

    }
}
