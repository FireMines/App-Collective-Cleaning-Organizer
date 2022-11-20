package com.example.collectivecleaningorganizer.ui.task

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.core.view.iterator
import androidx.core.view.size
import com.example.collectivecleaningorganizer.Database
import com.example.collectivecleaningorganizer.LogOutActivity
import com.example.collectivecleaningorganizer.R
import com.example.collectivecleaningorganizer.ui.collective.CollectiveActivity
import com.example.collectivecleaningorganizer.ui.friends.FriendsActivity
import com.example.collectivecleaningorganizer.ui.utilities.ResultListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_task_overview.*
import kotlinx.android.synthetic.main.task_layout.view.*


class TaskOverviewActivity : AppCompatActivity() {
    private var username = Database.userData[0]?.get("username").toString()
    var userID = Database.userData[0]?.id.toString()

    private val db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_overview)
        if (userID == null) {
            Log.d("TaskOverview: Error", "the userID is null")
            return
        }


        val collectiveID = Database.userData[0]?.data?.get("collectiveID")
        if (collectiveID == null) {
        }

        //Starting a listener for the collective and listens for any changes done to the collective data
        Database().databaseDataChangeListener("collective", collectiveID.toString(), Database.userCollectiveData,"collectiveData", object : ResultListener {
            override fun onSuccess() {
                dbSync(userID)
            }

            override fun onFailure(error: Exception) {
                Log.e("TaskOverviewActivity", "Failure with listener")
            }
        })

        val intentAddTaskPage: Intent = Intent(this, CreateTaskActivity::class.java)
        intentAddTaskPage.putExtra("uid",userID)


        // Starts addTaskActivity when clicking on Add Task button
        add_btn.setOnClickListener {
            startActivity(intentAddTaskPage)
        }

        // Deletes tasks marked in checkbox when clicked
        delete_btn.setOnClickListener {
            val collectiveTasks : ArrayList<MutableMap<String,String>>? = Database.userCollectiveData[0]?.data?.get("tasks") as ArrayList<MutableMap<String, String>>?
            val collectiveID = Database.userData[0]?.data?.get("collectiveID").toString()

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

        val navigationBarView = findViewById<BottomNavigationView>(R.id.bottom_navigator)
        navigationBarView.selectedItemId = R.id.taskOverView

        navigationBarView.setOnItemSelectedListener { it ->
            when(it.itemId) {
                R.id.taskOverView -> {
                    true
                }
                R.id.collective -> {
                    startActivity(Intent(this, CollectiveActivity::class.java))
                    true
                }
                R.id.friends -> {
                    startActivity(Intent(this, FriendsActivity::class.java))
                }
            }
            false
        }




    }

    private fun dbSync(userID : String) {
        removeAllRecipes()
        //Retrieving user's tasks stored in the cached user collective data
        val collectiveTasks : ArrayList<MutableMap<String,String>>? = Database.userCollectiveData[0]?.data?.get("tasks") as ArrayList<MutableMap<String, String>>?

        // The sorted list of ONLY the current logged in users tasks
        val sorted = collectiveTasks?.filter{ s->
            val test = s["assigned"] as ArrayList<String>
            test.contains(username)}?.toList()


        //Checking and handling if the cached data doesn't have any tasks
        if (collectiveTasks == null) {
            Log.d("TaskOverviewActivity","No tasks in the collective")
            return
        }

        // Show all tasks when database updates and on start of app
        showTasks(userID ,collectiveTasks)

        // Show all tasks in collective
        allTasksButton.setOnClickListener {
            showTasks(userID ,collectiveTasks)
        }

        // Show all tasks assigned to the logged in user
        myTaskButton.setOnClickListener {
            showTasks(userID ,sorted)
        }
    }

    /**
     * This is a function that opens the Task page
     * @param name is the name of the task you want to enter
     * @param dueDate is the due date of the task you want to enter
     * @param description is the description of the task you want to enter
     * @param category is the category of the task you want to enter
     * @param assigned is which person(s) assigned to the task you want to enter
     * @param index is the index of the task you are entering
     * @param userID is the userID of the current user
     */
    private fun openTaskPage(name : String, dueDate : String, description : String, category : String, assigned : ArrayList<String>, index : Int, userID: String) {
        val newIntent = Intent(this, TaskActivity::class.java)

        newIntent.putExtra("uid",userID)
        newIntent.putExtra("name",name)
        newIntent.putExtra("dueDate", dueDate)
        newIntent.putExtra("description", description)
        newIntent.putExtra("assigned", assigned)
        newIntent.putExtra("category", category)
        newIntent.putExtra("index", index)

        startActivity(newIntent)
    }

    /**
     * Removes all recipes
     */
    private fun removeAllRecipes(){
        val i = rv_todo.iterator()
        while (i.hasNext()){
            i.next()
            i.remove()
        }
    }

    /**
     * Adds all tasks you want to show to the view and makes them enterable when clicked
     * @param userId is the id of the logged in user
     * @param collectiveTasks is all the tasks in the collective
     */
    private fun showTasks(userID: String, collectiveTasks: List<MutableMap<String, String>>?) {
        rv_todo.removeAllViews()
        for (task in collectiveTasks!!) {
            val view = layoutInflater.inflate(R.layout.task_layout, null)
            view.task_tv.text = task["name"]
            view.duedate_tv.text = task["dueDate"]
            val desc = task["description"].toString()
            view.task_tv.setOnClickListener{
                Log.e("index", collectiveTasks[collectiveTasks.indexOf(task)]["assigned"].toString())
                val assignedMembers : ArrayList<String> = collectiveTasks[collectiveTasks.indexOf(task)]["assigned"] as ArrayList<String>
                openTaskPage(view.task_tv.text.toString(),view.duedate_tv.text.toString(), desc, task["category"].toString(), assignedMembers, collectiveTasks.indexOf(task), userID)
            }
            rv_todo.addView(view)
            Log.e("Tasks:", task.entries.toString())
        }
    }
}

