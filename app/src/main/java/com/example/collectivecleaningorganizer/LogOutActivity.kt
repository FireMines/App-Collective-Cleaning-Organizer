package com.example.collectivecleaningorganizer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.collectivecleaningorganizer.ui.login.LoginActivity
import com.example.collectivecleaningorganizer.ui.task.TaskOverviewActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_log_out.*

class LogOutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_out)

        // gets user id passed via intent from TaskOverviewActivity.kt
        val userId = intent.getStringExtra("uid")
        if (userId == null) {
            Log.d("TaskOverview: Error", "the userID is null")
            return
        }

        // takes user back to task overview
        backToTaskOverviewButton.setOnClickListener {
            val intentTaskPage: Intent = Intent(this, TaskOverviewActivity::class.java)
            intentTaskPage.putExtra("uid",userId)
            startActivity(intentTaskPage)
        }

        // when user clicks on logout button
        logOutButton.setOnClickListener {
            logout()
        }
    }

    // logs out the user
    private fun logout() {
        Firebase.auth.signOut()
        Database.listenerMap["userData"]?.remove()
        Database.listenerMap["collectiveData"]?.remove()
        startActivity(Intent(this, LoginActivity::class.java))

        Toast.makeText(this, "Logout Successful",Toast.LENGTH_LONG).show()
    }
}