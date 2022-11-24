package com.example.collectivecleaningorganizer.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.collectivecleaningorganizer.database.Database
import com.example.collectivecleaningorganizer.R
import com.example.collectivecleaningorganizer.ui.collective.CollectiveActivity
import com.example.collectivecleaningorganizer.ui.task.TaskOverviewActivity
import com.example.collectivecleaningorganizer.ui.utilities.ResultListener
import com.example.collectivecleaningorganizer.ui.utilities.UniqueUsernameListener
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_create_user.*
import java.lang.Exception


class CreateUserActivity : AppCompatActivity() {
    private var tag: String = "CreateUserActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
        init()
    }

    /**
     * Adds eventlistener for all buttons
     */
    private fun init(){
        // Takes the user back to the Login Activity
        createUserBack_btn.setOnClickListener{
            this.finish()
        }

        CreateUserButton.setOnClickListener{
            addUser()
        }
    }

    /**
     * Informs the user if the database is not reached
     */
    private fun dbError(){
        Toast.makeText(
            this@CreateUserActivity,
            "Error when communicating with the database",
            Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * Adds user authentication to the database if the fields are filled in properly:
     * All fields must be filled
     * Password and confirm password must be similar
     * The username given is between 4 and 15 characters long
     * The username is unique
     * The email is unique
     * The given password is of adequate strength
     */
    private fun addUser(){
        if(CreatePassword.text.toString() == "" || CreateConfirmPassword.text.toString() == "" || CreateUser.text.toString() == "" || CreateName.text.toString() == "" || CreateEmail.text.toString() == ""){
            Toast.makeText(this@CreateUserActivity, "All fields must be filled", Toast.LENGTH_SHORT).show()
        }
        else if (CreateName.text.toString().length > 15){
            Toast.makeText(this@CreateUserActivity, "Username is too long", Toast.LENGTH_SHORT).show()
        }
        else if (CreateName.text.toString().length < 4){
            Toast.makeText(this@CreateUserActivity, "Username is too short", Toast.LENGTH_SHORT).show()
        }
        else if (CreatePassword.text.toString() != CreateConfirmPassword.text.toString()){
            Toast.makeText(this@CreateUserActivity, "Entered passwords are not the same", Toast.LENGTH_SHORT).show()
        }
        else {
            Database().checkUniqueUsername(
                CreateName.text.toString().lowercase(),
                object : UniqueUsernameListener {
                    override fun onSuccess(unique: Boolean) {
                        if (unique) {
                            val auth = FirebaseAuth.getInstance()
                            //Creates auth user needed to log in
                            auth.createUserWithEmailAndPassword(
                                CreateEmail.text.toString(),
                                CreatePassword.text.toString()
                            ).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    println("UID:" + task.result.user?.uid)
                                    Toast.makeText(
                                        this@CreateUserActivity,
                                        "User created",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    //Adds the username to a collection holding usernames and user ids
                                    val uid = hashMapOf(
                                        "uid" to auth.uid.toString()
                                    )
                                    Database().addToDB(
                                        "usernames",
                                        CreateName.text.toString().lowercase(),
                                        uid,
                                        object : ResultListener {
                                            override fun onSuccess() {

                                            }

                                            override fun onFailure(error: Exception) {
                                                Log.e(tag, "Failure with listener")
                                                dbError()
                                            }
                                        })

                                    //Adds user to the database
                                    val username = hashMapOf(
                                        "username" to CreateName.text.toString().lowercase()
                                    )
                                    Database().addToDB(
                                        "users",
                                        auth.uid.toString(),
                                        username,
                                        object : ResultListener {
                                            override fun onSuccess() {

                                            }

                                            override fun onFailure(error: Exception) {
                                                Log.e(tag, "Failure with listener")
                                                dbError()
                                            }
                                        })

                                    login()

                                } else {
                                    //Password to weak or email not unique
                                    Toast.makeText(
                                        this@CreateUserActivity,
                                        "Email is not unique or password is too weak",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            //Username not unique
                            Log.e(tag, "Username is not unique")
                            Toast.makeText(
                                this@CreateUserActivity,
                                "Username is not unique",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(error: Exception) {
                        Log.e(tag, "Failure to connect to database")
                        dbError()
                    }
                })
        }
    }

    /**
     * A function that is used to retrieve the user data and store it in a cache.
     * This function calls on another function and listens for a callback
     */
    private fun login(){
        val userID = FirebaseAuth.getInstance().uid.toString()
        Database().retrieveDataAndAddToCache("users", userID, Database.userData, object : ResultListener {
            //if onSuccess() is called back, it means that the retrieval of user data and storing it to a cache was successful
            override fun onSuccess() {
                Log.d(tag, "Successfully retrieved the user data and stored it in a cache")
                //Starting a data change listener for the userData
                Database().databaseDataChangeListener("users", userID, Database.userData, "userdata",null)
                val collectiveID = Database.userData[0]?.data?.get("collectiveID")


                //Checking if the user is apart of a collective or not
                if (collectiveID == null) {
                    //initializing an intent for CollectiveActivity
                    val intent = Intent(this@CreateUserActivity, CollectiveActivity::class.java)
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
                dbError()
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
            Database.userCollectiveData,
            object : ResultListener {
                //if onSuccess() is called back, it means that the retrieval of user collective data and storing it to a cache was successful
                override fun onSuccess() {
                    Log.d(tag, "Successfully retrieved the collective data and stored it in a cache")

                    //Initializing am intent for the TaskOverviewActivity
                    val intent = Intent(this@CreateUserActivity, TaskOverviewActivity::class.java)
                    //Adding the userID to the intent
                    intent.putExtra("uid", userID)
                    startActivity(intent)
                }
                //if onFailure() is called back, it means that the retrieval of user collective data and storing it to a cache was a failure
                override fun onFailure(error: Exception) {
                    Log.e(tag, "An error occurred while trying to retrieve the collective data.", error)
                    dbError()
                }
            })
    }

}