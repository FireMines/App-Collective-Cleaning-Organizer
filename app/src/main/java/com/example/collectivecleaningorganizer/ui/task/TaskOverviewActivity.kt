package com.example.collectivecleaningorganizer.ui.task

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.allViews
import androidx.core.view.get
import androidx.core.view.iterator
import androidx.core.view.size
import androidx.recyclerview.widget.RecyclerView
import com.example.collectivecleaningorganizer.*
import com.example.collectivecleaningorganizer.ui.collective.ResultListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_task_overview.*
import kotlinx.android.synthetic.main.activity_view_task.view.*
import kotlinx.android.synthetic.main.task_layout.view.*
import java.lang.Exception


class TaskOverviewActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_overview)
        val userID = intent.getStringExtra("uid")
        if (userID == null) {
            Log.d("TaskOverview: Error", "the userID is null")
            return
        }

        val collectiveID = userData[0]?.data?.get("collectiveID")
        if (collectiveID == null) {
            //Start collective activity
        }
        //Starting a listener for the collective and listens for any changes done to the collective data
        Database().databaseDataChangeListener("collective", collectiveID.toString(), userCollectiveData, object : ResultListener {
            override fun onSuccess() {
                dbSync(userID)
            }

            override fun onFailure(error: Exception) {
                Log.e("TaskOverviewActivity", "Failure with listener")
            }
        })

        val intentAddTaskPage: Intent = Intent(this, AddTaskActivity::class.java)
        intentAddTaskPage.putExtra("uid",userID)


        // Starts addTaskActivity when clicking on Add Task button
        add_btn.setOnClickListener {
            startActivity(intentAddTaskPage)
        }

        // Deletes tasks marked in checkbox when clicked
        delete_btn.setOnClickListener {
            val collectiveTasks : ArrayList<MutableMap<String,String>>? = userCollectiveData[0]?.data?.get("tasks") as ArrayList<MutableMap<String, String>>?
            val collectiveID = userData[0]?.data?.get("collectiveID").toString()

            // Goes through all checked tasks and deletes them
            for (i in rv_todo.size -1 downTo  0) {
                if (rv_todo.get(i).checkBox.isChecked) {
                    collectiveTasks?.removeAt(i)
                    Database().updateValueInDB("collective", collectiveID,"tasks", collectiveTasks,null)
                }
            }
        }

        // passes user id to intent
        val intentLogoutPage: Intent = Intent(this, LogOutActivity::class.java)
        intentLogoutPage.putExtra("uid",userID)

        // Sends user to logout activity when clicking on settings wheel
        logoutImageView.setOnClickListener {
            startActivity(intentLogoutPage)
        }
    }

    private fun dbSync(userID : String) {
        removeAllRecipes()
        //Retrieving user's tasks stored in the cached user collective data
        val collectiveTasks : ArrayList<MutableMap<String,String>>? = userCollectiveData[0]?.data?.get("tasks") as ArrayList<MutableMap<String, String>>?

        //Checking and handling if the cached data doesn't have any tasks
        if (collectiveTasks == null) {
            Log.d("TaskOverviewActivity","No tasks in the collective")
            return
        }

        //Iterating through the collectiveTasks array
        for (task in collectiveTasks) {
            val view = layoutInflater.inflate(R.layout.task_layout, null)
            view.task_tv.text = task["name"]
            view.duedate_tv.text = task["dueDate"]
            val desc = task["description"].toString()
            view.task_tv.setOnClickListener{
                openTaskPage(view.task_tv.text.toString(),view.duedate_tv.text.toString(), desc, task["category"].toString(), task["assigned"].toString(), userID)
            }
            rv_todo.addView(view)
            Log.e("Tasks:", task.entries.toString())
        }
    }

    private fun openTaskPage(name : String, dueDate : String, description : String, category : String, assigned : String, userID: String) {
        val newIntent = Intent(this, TaskActivity::class.java)

        newIntent.putExtra("uid",userID)
        newIntent.putExtra("name",name)
        newIntent.putExtra("dueDate", dueDate)
        newIntent.putExtra("description", description)
        newIntent.putExtra("assigned", assigned)
        newIntent.putExtra("category", category)

        startActivity(newIntent)
    }


    private fun removeAllRecipes(){
        val i = rv_todo.iterator()
        while (i.hasNext()){
            i.next()
            i.remove()
        }
    }
}

