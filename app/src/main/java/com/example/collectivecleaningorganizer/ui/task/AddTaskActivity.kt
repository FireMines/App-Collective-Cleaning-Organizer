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
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import com.example.collectivecleaningorganizer.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_add_task.*
import kotlinx.android.synthetic.main.popup_with_edittext.view.*
import java.util.*

class AddTaskActivity : AppCompatActivity() {
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        val userID = intent.getStringExtra("uid")
        if (userID == null) {
            Log.d("TaskOverview: Error", "the userID is null")
            return
        }

        saveOrCreateButton.setOnClickListener{
            //Calling the function used to create the task by adding it to the DB
            createTask()
        }
        val itemList = arrayListOf<String>()
        itemList.add("tomas")
        itemList.add("tomas")
        val adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice,itemList)
        membersListView.adapter = adapter



        back_btn.setOnClickListener{
            val intentTaskPage: Intent = Intent(this, TaskOverviewActivity::class.java)
            intentTaskPage.putExtra("uid",userID)
            startActivity(intentTaskPage)
        }
        db.collection("users").document(userID).collection("category").get().addOnSuccessListener { tasks ->
            for (task in tasks) {
                Log.d(ContentValues.TAG, "${task.id} => ${task.data}")
                //Log.d("entries here: ", task.data)
                //tasklist.add(TaskModel(task.data["name"] as String, task.data["dueDate"] as String, task.data["description"] as String))
                //Log.d("Hallaballa", tasklist.toString())
                //adapter.notifyDataSetChanged()
            }
        }







    }
    fun createPopUpToCreateNewCategory(view: View) {
        val alertDialogBuilder : AlertDialog.Builder = AlertDialog.Builder(this)
        val layoutInflater : LayoutInflater = layoutInflater
        alertDialogBuilder.setTitle("With EditText")
        val dialogLayout = layoutInflater.inflate(R.layout.popup_with_edittext, null)
        val editText  = dialogLayout.popupEditText
        alertDialogBuilder.setView(dialogLayout)
        alertDialogBuilder.setPositiveButton("OK",DialogInterface.OnClickListener { dialogInterface, i ->  })
        alertDialogBuilder.setNegativeButton("Cancel", null)
        alertDialogBuilder.show()
    }

    private fun createTask() {
        val userID = intent.getStringExtra("uid")
        if (taskName.text.toString() == "") {
            Toast.makeText(this, "Please write a task name in order to create the task", Toast.LENGTH_SHORT).show()
        }

        val task = hashMapOf(
            "name" to taskName.text.toString(),
            "description" to taskDescription.text.toString(),
            "dueDate" to taskDueDate.text.toString()
        )
        if (userID == null) {
            Log.d("Create task: Error", "the userID is null")
            return
        }
        db.collection("users").document(userID).collection("tasks")
            .add(task)
            .addOnSuccessListener { documentReference ->
                Log.d("Create task: DB success", "Add task to the DB with the id: $documentReference")
            }
            .addOnFailureListener {
                Log.d("Create task: DB failure", "Failed to add the task")
            }
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