package com.example.collectivecleaningorganizer.ui.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.collectivecleaningorganizer.Database
import com.example.collectivecleaningorganizer.R
import com.example.collectivecleaningorganizer.ui.collective.CollectiveActivity
import com.example.collectivecleaningorganizer.ui.collective.ResultListener

import com.example.collectivecleaningorganizer.ui.task.TaskOverviewActivity
import com.example.collectivecleaningorganizer.userCollectiveData
import com.example.collectivecleaningorganizer.userData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*
import java.lang.Exception

class LoginActivity : AppCompatActivity() {
    private var tag: String = "LoginActivity"
    // declares instance of firebaseAuth
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Starts ForgotPasswordActivity when clicking on forgotPassword
        forgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
        signup.setOnClickListener {
            startActivity(Intent(this, CreateUserActivity::class.java))
        }
        // initializes instance of FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // activates login function when login button is clicked
        logInButton.setOnClickListener {
            login()
        }
    }

    // function to login user
    private fun login() {
        val email = emailLogin.text.toString()
        val password = passwordLogin.text.toString()

        // uses email and password as login credentials
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.d("log in", "login:success")
                Toast.makeText(this, "Successful login", Toast.LENGTH_SHORT).show()   // displays message to user

                //DB request to retrieve amy user data
                val userID = task.result.user?.uid.toString()
                //Calling function to retrieve the user data from DB and add it to a cache
                retrieveUserDataAndStoreInCache(userID)

            } else {
                Log.w("log in", "login:failure")
                Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
    /**
     * A function that is used to retrieve the user data and store it in a cache.
     * This function calls on another function and listens for a callback
     * @param userID is the ID of the user
     */
    private fun retrieveUserDataAndStoreInCache(userID : String) {
        //Calling retrieveDataAndAddToCache() function to retrieve the user data from DB and add it to a cache
        Database().retrieveDataAndAddToCache("users", userID, userData, object : ResultListener {
            //if onSuccess() is called back, it means that the retrieval of user data and storing it to a cache was successful
            override fun onSuccess() {
                Log.d(tag, "Successfully retrieved the user data and stored it in a cache")
                //Starting a data change listener for the userData
                Database().databaseDataChangeListener("users", userID, userData, "userData",null)

                val collectiveID = userData[0]?.data?.get("collectiveID")


                //Checking if the user is apart of a collective or not
                if (collectiveID == null) {
                    //initializing an intent for CollectiveActivity
                    val intent = Intent(this@LoginActivity,CollectiveActivity::class.java)
                    //Adding the userID to the intent
                    intent.putExtra("uid",userID)
                    startActivity(intent)
                }
                else {
                    //Calling a function to retrieve the user collective data from DB and add it to a cache
                    retrieveUserCollectiveDataAndStoreInCache(collectiveID.toString(), userID)
                }
            }
            //if onFailure() is called back, it means that the retrieval of user data and storing it to a cache was a failure
            override fun onFailure(error: Exception) {
                Log.e(tag, "An error occurred while trying to retrieve the user data.", error)
            }
        })
    }

    /**
     * A function that is used to retrieve the collective data the user is apart of and store it in a cache.
     * This function calls on another function and listens for a callback
     * @param collectiveID is the ID of the collective
     * @param userID is the ID of the user
     */
    private fun retrieveUserCollectiveDataAndStoreInCache(collectiveID : String, userID: String) {
        //Calling retrieveDataAndAddToCache() function to retrieve the user collective data from DB and add it to a cache
        Database().retrieveDataAndAddToCache(
            "collective",
            collectiveID,
            userCollectiveData,
            object : ResultListener {
                //if onSuccess() is called back, it means that the retrieval of user collective data and storing it to a cache was successful
                override fun onSuccess() {
                    Log.d(tag, "Successfully retrieved the collective data and stored it in a cache")

                    //Initializing am intent for the TaskOverviewActivity
                    val intent = Intent(this@LoginActivity, TaskOverviewActivity::class.java)
                    //Adding the userID to the intent
                    intent.putExtra("uid", userID)
                    startActivity(intent)
                }
                //if onFailure() is called back, it means that the retrieval of user collective data and storing it to a cache was a failure
                override fun onFailure(error: Exception) {
                    Log.e(tag, "An error occurred while trying to retrieve the collective data.", error)
                }
            })
    }
}

