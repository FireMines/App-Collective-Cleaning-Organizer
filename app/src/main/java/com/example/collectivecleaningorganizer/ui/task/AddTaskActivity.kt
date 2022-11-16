package com.example.collectivecleaningorganizer.ui.task

import android.app.AlertDialog
import android.app.DatePickerDialog

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*

import com.example.collectivecleaningorganizer.Database
import com.example.collectivecleaningorganizer.R
import com.example.collectivecleaningorganizer.userCollectiveData
import com.example.collectivecleaningorganizer.userData

import kotlinx.android.synthetic.main.activity_add_task.*

import java.util.*
import kotlin.collections.ArrayList

class AddTaskActivity : AppCompatActivity() {
    //Initializing a tag used to indicate which file Log is writing about
    private val tag = "AddTaskActivity"

    //Initializing an array list to retrieve the categories data from the snapshot as an arraylist
    private val categoriesArrayListFromSnapshot : ArrayList<String>? = userCollectiveData[0]?.data?.get("categories") as ArrayList<String>?

    //Initializing an array list for categories with a default value of "No category"
    private var categoriesArrayList : ArrayList<String> = arrayListOf("No Category")

    override fun onCreate(savedInstanceState: Bundle?) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)
        val userID = userData[0]?.id.toString()
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
        //Checking if the categories data exist in the snapshot
        if (categoriesArrayListFromSnapshot != null) {
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
        val collectiveMembersMap : MutableMap<String,String> = userCollectiveData[0]?.data?.get("members") as MutableMap<String, String>

        //Creating an arraylist for the keys from the collective members map
        val collectiveMemberArrayList : ArrayList<String> = ArrayList(collectiveMembersMap.keys)

        //Creating an ArrayAdapter with the "simple_list_item_multiple_choice" layout and adding the collectiveMemberArrayList to the adapter
        val membersAdapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice,collectiveMemberArrayList)

        //Assigning the adapter to the ListView with the id "assignCollectiveMembersListView"
        assignCollectiveMembersListView.adapter = membersAdapter
    }

    /**
     * A function that shows an alert dialog where the user can write a category name and create a category if all the conditions are met
     * @param view is the "New category" button with onclick to this function
     */
    fun createNewCategory(view: View) {
        //Initializing an EditText widget
        val inputEditTextField = EditText(this)

        //Initializing an AlertDialog and building a dialog that allows the user to write a category name
        val dialog = AlertDialog.Builder(this)
            .setTitle("New category")
            .setMessage("Enter the name of the category you want to create")
            .setView(inputEditTextField)
            .setPositiveButton("Create") { _, _ ->
                //Initializing a variable to get the text value from the input Edit text field
                val userInput = inputEditTextField.text.toString()

                //Checking if the input is empty and handling it accordingly
                if (userInput == "") {
                    Toast.makeText(this, "The category wasn't created. Reason: Name was empty", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                //Checking if there exists a category with the same name as the user input and handling it accordingly
                if (checkIfCategoryExists(categoriesArrayList, userInput)) {
                    Log.e("OH NO","IT CONTAINS")
                    Toast.makeText(this, "The category wasn't created. Reason: There is already exists a category with that name", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                //Getting the ID of the collective the user is apart of
                val collectiveID = userData[0]?.data?.get("collectiveID")

                //Adding the new category name into the arraylist holding all the categories
                categoriesArrayList.add(inputEditTextField.text.toString())

                //Updating the categories data with the updated contents of categoriesArrayList
                Database().updateValueInDB("collective",collectiveID.toString(),"categories",categoriesArrayList,null)

                //Creating a message which shows the user that the category was created successfully
                Toast.makeText(this, "You successfully created a category with the name $userInput" , Toast.LENGTH_LONG).show()
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
        //Iterating through the given array list
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
        val assignedMembers : ArrayList<String> = arrayListOf()

        //Creating a variable of ArrayList<MutableMap<String,Any>>
        var tasksArray : ArrayList<MutableMap<String,Any>>

        //Checking if the collectiveData snapshot doesn't have tasks data
        if (userCollectiveData[0]?.data?.get("tasks") == null) {
            //Initializing the tasksArray variable with an empty arraylist
            tasksArray = ArrayList<MutableMap<String,Any>>()
            Log.d("AddTaskActivity()","No tasks in the collective")
        }

        else {
            //Initializing the tasksArray variable with the task data from the collectiveData snapshot
            tasksArray = userCollectiveData[0]?.data?.get("tasks") as ArrayList<MutableMap<String, Any>>
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
     * @param view the textview that the user clicks on in order to run this function
     */
    fun showDatePickerDialog(view: View) {
        //Creating a calendar instance
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

            //Getting the chosen day as a number
            day = dueDate[0].toInt()

            //Getting the chosen month as a number
            // and decrementing the month to show the correct month in the dialog as the calander instance's month is from range 0 to 11.
            month = dueDate[1].toInt().dec()

            //Getting the chosen year as a number
            year = dueDate[2].toInt()
        }
        //Creating a date picker dialog allowing the user to choose a date
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
