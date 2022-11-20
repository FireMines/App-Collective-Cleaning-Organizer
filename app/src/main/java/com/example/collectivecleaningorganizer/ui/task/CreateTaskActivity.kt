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
     * A function that shows an alert dialog where the user can write a category name and create a category if all the conditions are met
     * @param view is the "Create a new category" button with onclick to this function
     */
    fun createNewCategory(view: View) {
        //Initializing an EditText widget
        val inputEditTextField = EditText(this)

        //Building an alert dialog that shows a view with an EditText where the user can write a category name to add
        Utilities().alertDialogBuilder(this,"Create a new category", "Enter the name of the category you want to create", inputEditTextField)
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

                //Adding the new category name into the arraylist holding all the categories
                categoriesArrayList.add(inputEditTextField.text.toString())

                //Updating the categories data with the updated contents of categoriesArrayList
                Database().updateValueInDB("collective",collectiveID.toString(),"categories",categoriesArrayList,null)

                //Creating a message which shows the user that the category was created successfully
                Toast.makeText(this, "You successfully created a category with the name $userInput" , Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()


    }

    /**
     * A function that displays an alert dialog with a spinner containing all the categories, and the user can choose a category to delete from
     * @param view is the "Delete a category" button with onclick to this function
     */
    fun deleteCategory(view : View) {
        //Checking if there is only one category and that category is the default category named "No Category". If so deny the option to delete
        if (categoriesArrayList.size == 1 && categoriesArrayList.contains("No Category")) {
            Toast.makeText(this, "There are no categories to delete" , Toast.LENGTH_SHORT).show()
            return
        }
        //Initializing an Spinner widget
        val categorySpinner = Spinner(this)

        //Initializing an temporary arraylist which is empty
        val temporaryCategoriesToDeleteList : ArrayList<String> = arrayListOf()

        //Adding all the values from the categoriesArrayList to the temporaryCategoriesToDeleteList
        temporaryCategoriesToDeleteList.addAll(categoriesArrayList)

        //Removing the value "No Category" from the list as its a default value and a category the user should not be able to delete
        temporaryCategoriesToDeleteList.remove("No Category")

        //Creating an ArrayAdapter for the spinner
        val spinnerAdapter = ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,temporaryCategoriesToDeleteList)

        //Attaching the ArrayAdapter to the spinner with the id "categorySpinner"
        categorySpinner.adapter = spinnerAdapter

        //Building an alert dialog that shows a view with a spinner where the user can choose a category to delete
        Utilities().alertDialogBuilder(this,"Delete a category", "Choose a category you want to delete", categorySpinner)
            .setPositiveButton("Delete") { _, _ ->
                //Initializing a variable to retrieve the name of the category the user selected
                val selectedCategory : String = categorySpinner.selectedItem.toString()

                //Removing the category name the user selected from the categories arraylist
                categoriesArrayList.remove(selectedCategory)

                //Updating the categories data with the updated contents of categoriesArrayList
                Database().updateValueInDB("collective",collectiveID.toString(),"categories",categoriesArrayList,null)

                //Retrieving collective's tasks stored in the cached collective data
                var collectiveTasks : ArrayList<MutableMap<String,String>>? = Database.userCollectiveData[0]?.data?.get("tasks") as ArrayList<MutableMap<String, String>>?

                //Checking if the collective task data exists and if its not empty
                if (collectiveTasks != null && collectiveTasks.isNotEmpty()) {
                    //Removing the category from all tasks that has the category assigned to it
                    collectiveTasks = Utilities().removeCategoryFromTasks(collectiveTasks,selectedCategory)
                    /*
                    Updating the collective task data in DB,
                    with a data that that has removed the category from all tasks that was assigned to them
                    */
                    Database().updateValueInDB("collective", collectiveID.toString(),"tasks",collectiveTasks,null)

                }

            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
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
