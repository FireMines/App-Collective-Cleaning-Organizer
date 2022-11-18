package com.example.collectivecleaningorganizer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.collectivecleaningorganizer.fragments.CollectiveFragment
import com.example.collectivecleaningorganizer.fragments.FriendsFragment
import com.example.collectivecleaningorganizer.fragments.TasksFragment
import com.example.collectivecleaningorganizer.ui.collective.CollectiveActivity
import com.example.collectivecleaningorganizer.ui.login.LoginActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_task_overview.*


class MainActivity : AppCompatActivity() {

    private val tasksFragment = TasksFragment()
    private val collectiveFragment = CollectiveFragment()
    private val friendsFragment = FriendsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        replaceFragment(tasksFragment)

        bottom_navigator.setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.tasks -> replaceFragment(tasksFragment)
                R.id.friends -> replaceFragment(friendsFragment)
                R.id.collective -> replaceFragment(collectiveFragment)
            }
            true
        }

        /*
        db.collection("usersExample").get().addOnSuccessListener { users ->
            for (user in users) {
                //println(document)
                usersExampleCollection[user.id] = user
            }
            println(usersExampleCollection["nBSu5tDXO9LuXEnKjsNR"]?.get("collectiveID"))
        }

         */

        // Sends the user straight to the login activity
        startActivity(Intent(this, LoginActivity::class.java))

        /*
        val intentAddTask: Intent = Intent(this,CreateTaskActivity::class.java)
        startActivity(intentAddTask)

         */
        /*
        val newIntent = Intent(this, FriendsActivity::class.java)
        startActivity(newIntent)

         */
    }

    private fun replaceFragment(fragment: Fragment) {
        if (fragment != null) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(com.google.android.material.R.id.fragment_container_view_tag, fragment)
            transaction.commit()
        }
    }


}