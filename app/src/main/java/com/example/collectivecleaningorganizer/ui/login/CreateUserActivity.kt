package com.example.collectivecleaningorganizer.ui.login

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.collectivecleaningorganizer.Database
import com.example.collectivecleaningorganizer.R
import com.example.collectivecleaningorganizer.ui.collective.CollectiveActivity
import com.example.collectivecleaningorganizer.ui.task.TaskOverviewActivity
import com.example.collectivecleaningorganizer.ui.utilities.ResultListener
import com.example.collectivecleaningorganizer.ui.utilities.UniqueUsernameListener
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_create_user.*
import kotlinx.android.synthetic.main.activity_view_task.*
import java.lang.Exception


class CreateUserActivity : AppCompatActivity() {
    private var tag: String = "CreateUserActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
        init()

        // Takes the user back to the Login Activity
        createUserBack_btn.setOnClickListener{
            this.finish()
        }
    }

    private fun init(){
        CreateUserButton.setOnClickListener{
            addUser()
        }
    }

    private fun addUser(){
        //Se i database om email allerede er i bruk

        //Se i database om brukernavn er unikt, dersom dette er et krav

        //Les alle tekstfelt og sørg for at confirm password og password har lik verdi

        //Send inn data til database dersom over er gyldig

        //En eller annen konfirmasjonsskjerm dersom handlingene over forekommer plettfritt

        //Opprett heller en error streng som fylles ettersom med krav
        if(CreatePassword.text.toString() == "" || CreateConfirmPassword.text.toString() == "" || CreateUser.text.toString() == "" || CreateName.text.toString() == "" || CreateEmail.text.toString() == ""){
            Toast.makeText(this@CreateUserActivity, "All fields must be filled", Toast.LENGTH_SHORT).show()
        }
        else if (CreateName.text.toString().length > 20){
            Toast.makeText(this@CreateUserActivity, "Username is too long", Toast.LENGTH_SHORT).show()
        }
        else if (CreateName.text.toString().length < 6){
            Toast.makeText(this@CreateUserActivity, "Username is too short", Toast.LENGTH_SHORT).show()
        }
        else if (CreatePassword.text.toString() != CreateConfirmPassword.text.toString()){
            Toast.makeText(this@CreateUserActivity, "Entered passwords are not the same", Toast.LENGTH_SHORT).show()
        }
        //Helt tomt crasher appen foreløpig
        else {
            Database().checkUniqueUsername(
                CreateName.text.toString().lowercase(),
                object : UniqueUsernameListener {
                    override fun onSuccess(unique: Boolean) {
                        if (unique) {
                            val auth = FirebaseAuth.getInstance()

                            //val auth = FirebaseAuth.getInstance()
                            //Sjekk at suksess
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

                                    val uid = hashMapOf(
                                        "uid" to auth.uid.toString()
                                    )
                                    Database().addToDB(
                                        "usernames",
                                        CreateName.text.toString().lowercase(),
                                        uid,
                                        object : ResultListener {
                                            override fun onSuccess() {
                                                //Navn lagt til ett av stedene
                                                Log.e(tag, "Bruh")
                                            }

                                            override fun onFailure(error: Exception) {
                                                Log.e(tag, "Failure with listener")
                                            }
                                        })

                                    val username = hashMapOf(
                                        "username" to CreateName.text.toString().lowercase()
                                    )
                                    Database().addToDB(
                                        "users",
                                        auth.uid.toString(),
                                        username,
                                        object : ResultListener {
                                            override fun onSuccess() {
                                                //Navn lagt til ett av stedene
                                                Log.e(tag, "Bruh")
                                            }

                                            override fun onFailure(error: Exception) {
                                                Log.e(tag, "Failure with listener")
                                            }
                                        })

                                    login()

                                } else {
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
                        Toast.makeText(
                            this@CreateUserActivity,
                            "Error communicating with database",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }

    /**
     * A function that is used to retrieve the user data and store it in a cache.
     * This function calls on another function and listens for a callback
     * @param userID is the ID of the user
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
                }
            })
    }

}