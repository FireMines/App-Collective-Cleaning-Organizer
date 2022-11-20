package com.example.collectivecleaningorganizer.ui.task


import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.collectivecleaningorganizer.Database
import com.example.collectivecleaningorganizer.R
import kotlinx.android.synthetic.main.activity_create_task.back_btn
import kotlinx.android.synthetic.main.activity_edit_task.*
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


        //Initializing a mutable map for the task information
        val taskInformation = mutableMapOf<String,Any>()
        //var tasksArray : ArrayList<MutableMap<String,Any>>

        //Initializing the ID of the collective the user is apart of retrieved from the user data

        setContentView(R.layout.activity_edit_task)
        setTitle("Task description page")

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
        if (editTaskDueDate.text != "") {
            //Splitting the taskDueDate to get the day, month and year
            val dueDate : List<String> = editTaskDueDate.text.split("/")

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
            editTaskDueDate.setText("$pickedDay/${pickedMonth.inc()}/$pickedYear")

        }, year, month, day)

        //Not allowing the dialog to show past dates
        datePickerDialog.datePicker.minDate = calendar.getTimeInMillis();

        //Showing the datepicker dialog
        datePickerDialog.show()
    }

}
