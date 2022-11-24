package com.example.collectivecleaningorganizer.ui.utilities

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.collectivecleaningorganizer.database.Database
import com.example.collectivecleaningorganizer.R
import com.example.collectivecleaningorganizer.ui.collective.CollectiveActivity
import com.example.collectivecleaningorganizer.ui.collective.SpecificCollectiveActivity
import com.example.collectivecleaningorganizer.ui.friends.FriendsActivity
import com.example.collectivecleaningorganizer.ui.login.LoginActivity
import com.example.collectivecleaningorganizer.ui.task.TaskOverviewActivity
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_create_task.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * A class that hold functions considered as a utility for the app
 */
class Utilities {
    /**
     * A function that builds an alert dialog
     * @param context is the context we want to show the dialog
     * @param title is the title of the alert dialog
     * @param message is the message which is shown in the alert dialog
     * @param view is the layout widgets we want to add to the dialog if there any
     */
    fun alertDialogBuilder(context : Context, title: String?, message: String?, view: View?): AlertDialog.Builder {

        return AlertDialog.Builder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Background)
            .setTitle(title)
            .setMessage(message)
            .setView(view)
    }

    /**
     * A function that removes a member from tasks assigned to them
     * @param collectiveTasks is the arraylist containing the collective tasks
     * @param username is the username we want to remove from the tasks
     * @return returns an arraylist with a mutable<String,String> as its value
     */
    fun removeMemberFromTasks(collectiveTasks : ArrayList<MutableMap<String,String>>, username : String) : ArrayList<MutableMap<String,String>> {
        //Iterating through the collectiveTasks arraylist
        for (task in collectiveTasks) {
            //Initializing an arraylist with the assigned members list for the task
            val assignedMembers : ArrayList<String> = task["assigned"] as ArrayList<String>

            //If the assignedMembers arraylist for each task contains the userID, remove it from the assignedMembers list
            if (assignedMembers.contains(username)) {
                //Removing the user from the assignedMembers arraylist
                assignedMembers.remove(username)
            }
        }
        return collectiveTasks
    }

    /**
     * This is a function that removes a deleted category from all tasks that has it selected as their category
     * @param collectiveTasks is the arraylist containing the collective tasks
     * @param removedCategoryName is the name of the removed category name
     * @return returns an arraylist with a mutable<String,String> as its value
     */
    fun removeCategoryFromTasks (collectiveTasks : ArrayList<MutableMap<String,String>>, removedCategoryName : String) : ArrayList<MutableMap<String,String>> {
        //Iterating through the collectiveTasks arraylist
        for (task in collectiveTasks) {
            //Checking if the category of the task is the same as the removed category name
            if (task["category"].toString().lowercase() == removedCategoryName.lowercase()) {
                //Changing the task's category to the default name of "No Category"
                task["category"] = "No Category"
            }
        }
        return collectiveTasks
    }
    /**
     * A function that is called everytime the user clicks on the TextView element with the id "taskDueDate"
     * It shows a date picker dialog where the user can choose a due date
     * @param context is the activity context
     * @param taskDueDate is the textview that we are showing the date to
     */
    fun showDatePickerDialog(context : Context, taskDueDate : TextView) {
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
        val datePickerDialog = DatePickerDialog(context,
            DatePickerDialog.OnDateSetListener { datePicker : DatePicker, pickedYear: Int, pickedMonth: Int, pickedDay : Int ->
            //Setting the date into the EditText element with the id "taskDueDate".
            //Incrementing the month to give the correct month value as the calander instance's month is from range 0 to 11.
            taskDueDate.setText("$pickedDay/${pickedMonth.inc()}/$pickedYear")

        }, year, month, day)

        //Not allowing the dialog to show past dates
        datePickerDialog.datePicker.minDate = calendar.timeInMillis;

        //Showing the datepicker dialog
        datePickerDialog.show()
    }

    /**
     * A function that displays an alert dialog with a spinner containing all the categories, and the user can choose a category to delete from
     * @param context is the activity context
     * @param categoriesArrayList is the arraylist containing all the categories of the collective
     * @param collectiveID is the ID of the collective
     */
    fun deleteCategory(context: Context, categoriesArrayList : ArrayList<String>, collectiveID : String) {
        //Checking if there is only one category and that category is the default category named "No Category". If so deny the option to delete
        if (categoriesArrayList.size == 1 && categoriesArrayList.contains("No Category")) {
            Toast.makeText(context, "There are no categories to delete" , Toast.LENGTH_SHORT).show()
            return
        }
        //Initializing an Spinner widget
        val categorySpinner = Spinner(context)

        //Initializing an temporary arraylist which is empty
        val temporaryCategoriesToDeleteList : ArrayList<String> = arrayListOf()

        //Adding all the values from the categoriesArrayList to the temporaryCategoriesToDeleteList
        temporaryCategoriesToDeleteList.addAll(categoriesArrayList)

        //Removing the value "No Category" from the list as its a default value and a category the user should not be able to delete
        temporaryCategoriesToDeleteList.remove("No Category")

        //Creating an ArrayAdapter for the spinner
        val spinnerAdapter = ArrayAdapter<String>(context,android.R.layout.simple_dropdown_item_1line,temporaryCategoriesToDeleteList)

        //Attaching the ArrayAdapter to the spinner with the id "categorySpinner"
        categorySpinner.adapter = spinnerAdapter

        //Building an alert dialog that shows a view with a spinner where the user can choose a category to delete
        Utilities().alertDialogBuilder(context,"Delete a category", "Choose a category you want to delete", categorySpinner)
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
                Toast.makeText(context, "You successfully deleted the category named $selectedCategory" , Toast.LENGTH_SHORT).show()

            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    /**
     * A function that shows an alert dialog where the user can write a category name and create a category if all the conditions are met
     * @param context is the activity context
     * @param categoriesArrayList is the arraylist containing all the categories of the collective
     * @param collectiveID is the ID of the collective
     */
    fun createNewCategory(context: Context,categoriesArrayList : ArrayList<String>, collectiveID : String) {
        //Initializing an EditText widget
        val inputEditTextField = EditText(context)

        //Building an alert dialog that shows a view with an EditText where the user can write a category name to add
        Utilities().alertDialogBuilder(context,"Create a new category", "Enter the name of the category you want to create", inputEditTextField)
            .setPositiveButton("Create") { _, _ ->
                //Initializing a variable to get the text value from the input Edit text field
                val userInput = inputEditTextField.text.toString()

                //Checking if the input is empty and handling it accordingly
                if (userInput == "") {
                    Toast.makeText(context, "The category wasn't created. Reason: Name was empty", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                //Checking if there exists a category with the same name as the user input and handling it accordingly
                if (checkIfCategoryExists(categoriesArrayList, userInput)) {
                    Log.e("OH NO","IT CONTAINS")
                    Toast.makeText(context, "The category wasn't created. Reason: There is already exists a category with that name", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                //Adding the new category name into the arraylist holding all the categories
                categoriesArrayList.add(inputEditTextField.text.toString())

                //Updating the categories data with the updated contents of categoriesArrayList
                Database().updateValueInDB("collective",collectiveID.toString(),"categories",categoriesArrayList,null)

                //Creating a message which shows the user that the category was created successfully
                Toast.makeText(context, "You successfully created a category with the name $userInput" , Toast.LENGTH_SHORT).show()
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
     * A function used to set the current date in the due date field for the create task and edit task activity
     * @param taskDueDate is the TextView where we are placing the date
     */
    fun setCurrentDate(taskDueDate : TextView) {
        //Creating a calendar instance
        val calendar = Calendar.getInstance()

        //Getting the current day
        var day : Int = calendar.get(Calendar.DAY_OF_MONTH)

        //Getting the current month
        var month : Int = calendar.get(Calendar.MONTH)+1

        //Getting the current year
        var year : Int = calendar.get(Calendar.YEAR)
        taskDueDate.text = "$day/$month/$year"
    }

    /**
     * A function that enables navigation of the bottom navigation bar
     * @param context is the context we want to use
     * @param id is the id of the selected menu item
     * @param navigationBarview selected navigation bar
     */
    fun navigation(app: AppCompatActivity, id: Int, navigationbarview : NavigationBarView) {
        //Retrieving the collectiveID from the userdata
        val collectiveID = Database.userData[0]?.data?.get("collectiveID")

        //Sets id of menu item
        navigationbarview.selectedItemId = id
        //Clicking on any of the icons in navigation bar
        navigationbarview.setOnItemSelectedListener {
            when(it.itemId) {
                // if the menu id is taskOverView
                R.id.taskOverView -> {
                    //Checking if the user isnt apart of a collective
                    if (collectiveID == null) {
                        Toast.makeText(app, "Please join a collective first", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        val intent = Intent(app, TaskOverviewActivity::class.java)
                        //Starts new activity
                        app.startActivity(intent)
                        //Finishing the activity the user was on
                        app.finish()
                    }
                }
                // if the menu id is collective
                R.id.collective -> {
                    var intent = Intent(app, SpecificCollectiveActivity::class.java)
                    if (collectiveID == null) {
                        intent = Intent(app, CollectiveActivity::class.java)
                    }

                    //Starts new activity
                    app.startActivity(intent)
                    //Finishing the activity the user was on
                    app.finish()
                }
                //if the menu id is friends
                R.id.friends -> {
                    val intent = Intent(app, FriendsActivity::class.java)
                    //Starts new activity
                    app.startActivity(intent)
                    //Finishing the activity the user was on
                    app.finish()
                }
            }
            false
        }
    }
    /**
     * A function that is used to log out a new user that does not create or join a collective.
     *  The function calls the firebase auth.sigOut() method.
     *  The function removes both the database listener for userData and collectiveData
     */
     fun logout(context: Context) {
        try {
            //Signing out
            Firebase.auth.signOut()
            //Removing the listens for the userdata if it exists
            Database.listenerMap["userData"]?.remove()
            //Removing the listener for the collective data if it exists
            Database.listenerMap["collectiveData"]?.remove()
            //Creating an intent for the login activity
            val intent = Intent(context, LoginActivity::class.java)
            //Sending the user back to login page
            context.startActivity(intent)
            //Finishing the current Activity
            //app.finish()
            Toast.makeText(context,"Successfully logged out"
                ,Toast.LENGTH_LONG).show()
        }
        catch (error : Exception) {
            Toast.makeText(context, "An error occurred when trying to log out. Try again ", Toast.LENGTH_LONG).show()
            Log.e("Logout", "Error when trying to run the logout() function",error)
        }
    }

    /**
     * A function used to check if the user is supposed to be in an activity
     * @param context is the activity the function is called from
     */
    fun checkIfUserIsSupposedToBeInCollective(context: Context) {
        //Initializing the collective id retrieved from the collective data in the DB
        val collectiveID : String = Database.userCollectiveData[0]?.id.toString()
        //Initializing the collective id retrieved from the userData in the DB
        val userCollectiveID = Database.userData[0]?.data?.get("collectiveID").toString()
        //Checking if the logged in user is not apart of the collective, and handling it accordingly
        if (collectiveID != userCollectiveID) {
            Toast.makeText(context, "Are you supposed to be in this collective? Please log in again" , Toast.LENGTH_SHORT).show()
            //Logging the user out
            Utilities().logout(context)
            Log.e("Utilities", "User's collectiveID dosnt match the collectiveID the user is viewing. Logging user out")
            return
        }
    }
}