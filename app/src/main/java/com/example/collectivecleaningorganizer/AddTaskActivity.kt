package com.example.collectivecleaningorganizer

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.DatePicker
import kotlinx.android.synthetic.main.activity_add_task.*
import java.util.*

class AddTaskActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)


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
            month = dueDate.get(1).toInt()
            year = dueDate.get(2).toInt()
        }
        val datePickerDialog = DatePickerDialog(this,DatePickerDialog.OnDateSetListener { datePicker : DatePicker, pickedYear: Int, pickedMonth: Int, pickedDay : Int ->
            //Setting the date into the EditText element with the id "taskDueDate"
            taskDueDate.setText("$pickedDay/$pickedMonth/$pickedYear")

        }, year, month, day)

        //Not allowing the dialog to show past dates
        datePickerDialog.datePicker.minDate = calendar.getTimeInMillis();

        //Showing the datepicker dialog
        datePickerDialog.show()

    }
}