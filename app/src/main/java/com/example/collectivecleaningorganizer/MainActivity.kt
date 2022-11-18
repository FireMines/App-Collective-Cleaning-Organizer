package com.example.collectivecleaningorganizer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.collectivecleaningorganizer.ui.collective.CollectiveActivity
import com.example.collectivecleaningorganizer.ui.login.LoginActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_task_overview.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

}