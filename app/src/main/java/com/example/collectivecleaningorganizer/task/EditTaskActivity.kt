package com.example.collectivecleaningorganizer.task

import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.example.collectivecleaningorganizer.R
import kotlinx.android.synthetic.main.activity_add_task.*

class EditTaskActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)
        taskPageTitle.text = "Edit Task Page"
        saveOrCreateButton.text = "Save"



    }
}