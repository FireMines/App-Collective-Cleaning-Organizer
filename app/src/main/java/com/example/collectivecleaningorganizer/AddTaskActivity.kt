package com.example.collectivecleaningorganizer

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.DatePicker
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_add_task.*
import java.util.*

class AddTaskActivity : AppCompatActivity() {
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)
        saveOrCreateButton.setOnClickListener{
            //Calling the function used to create the task by adding it to the DB
            createTask()
        }








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