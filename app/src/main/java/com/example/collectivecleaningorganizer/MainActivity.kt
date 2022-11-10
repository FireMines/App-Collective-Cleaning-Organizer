package com.example.collectivecleaningorganizer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.collectivecleaningorganizer.ui.collective.CollectiveActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {
    private val db = Firebase.firestore


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
        startActivity(Intent(this, CollectiveActivity::class.java))

        /*
        val intentAddTask: Intent = Intent(this,AddTaskActivity::class.java)
        startActivity(intentAddTask)

         */
        /*
        val newIntent = Intent(this, FriendsActivity::class.java)
        startActivity(newIntent)

         */



    }

     fun checkIfUserIsInACollective(userID : String) {

        db.collection("usersExample").document(userID).get().addOnSuccessListener { document ->
            val collectiveName = document.data?.get("collectiveID").toString()

            println(collectiveName)
        }



    }

}