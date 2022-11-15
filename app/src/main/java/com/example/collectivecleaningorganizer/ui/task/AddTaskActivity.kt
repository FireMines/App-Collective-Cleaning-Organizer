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
import com.example.collectivecleaningorganizer.Database
import com.example.collectivecleaningorganizer.R
import com.example.collectivecleaningorganizer.ui.collective.CollectiveActivity
import com.example.collectivecleaningorganizer.ui.collective.ResultListener

import com.example.collectivecleaningorganizer.userCollectiveData
import com.example.collectivecleaningorganizer.userData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_add_task.*
import kotlinx.android.synthetic.main.popup_with_edittext.view.*
import java.lang.Exception
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


        back_btn.setOnClickListener{

            this.finish()


        }

        //Log.e("test", taskCategories.selectedItem.toString())
        /*
        taskCategories.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                Log.e("test", "${p0?.getItemAtPosition(p2)}")
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                return
            }

        }

         */

        showCategoriesToChooseFrom()
        showMembersToAssign()

    }
    fun showCategoriesToChooseFrom() {
        //Creating a variable for an array list


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
    fun showMembersToAssign() {
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

        val inputEditTextField = EditText(this)
        val dialog = AlertDialog.Builder(this)
            .setTitle("New category")
            .setMessage("Enter the name of the category you want to create")
            .setView(inputEditTextField)
            .setPositiveButton("Create") { _, _ ->
                if (inputEditTextField.text.toString() == "") {
                    Toast.makeText(this, "The category wasnt created as the name was empty", Toast.LENGTH_LONG).show()
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


    private fun createTask() {
        val userID = intent.getStringExtra("uid")
        if (taskName.text.toString() == "") {
            Toast.makeText(this, "Please write a task name in order to create the task", Toast.LENGTH_LONG).show()
        }
        //Retrieving the task array stored in the collective DB cache
        var tasksArray : ArrayList<MutableMap<String,String>>
        if (userCollectiveData[0]?.data?.get("tasks") == null) {
            tasksArray = ArrayList<MutableMap<String,String>>()
            Log.d("AddTaskActivity()","No tasks in the collective")

        }
        else {
            tasksArray = userCollectiveData[0]?.data?.get("tasks") as ArrayList<MutableMap<String, String>>
        }
        //Creating a map for tasks
        val task = mutableMapOf<String,String>()
        //Adding the task information into the map

        task["name"] = taskName.text.toString()
        task["description"] = taskDescription.text.toString()
        task["dueDate"] = taskDueDate.text.toString()

        //Adding the task to the task array
        tasksArray.add(task)
        //Creating a hashmap with the field tasks and the value is the tasksArray
        val addDataToDB = hashMapOf(
            "tasks" to tasksArray
        )
        if (userID == null) {
            Log.d("Create task: Error", "the userID is null")
            return
        }
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
