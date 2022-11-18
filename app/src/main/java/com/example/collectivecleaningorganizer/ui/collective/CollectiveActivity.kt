package com.example.collectivecleaningorganizer.ui.collective

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.example.collectivecleaningorganizer.*
import com.example.collectivecleaningorganizer.R
import com.example.collectivecleaningorganizer.ui.task.TaskOverviewActivity
import com.example.collectivecleaningorganizer.ui.utilities.DatabaseRequestListener
import com.example.collectivecleaningorganizer.ui.utilities.ResultListener
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_collective.*
import java.lang.Exception


class CollectiveActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    private var tag = "CollectiveActivity"
    private var userID = userData[0]?.id.toString()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collective)

        Database().getAllCollectivesFromDB()

        val collectiveID = userData[0]?.data?.get("collectiveID")
        if (collectiveID != null) {
            //Database().databaseDataChangeListener("collective", collectiveID.toString(), userCollectiveData)
            startActivity(Intent(this, SpecificCollectiveActivity::class.java))
            return
        }

    }

    fun createCollective(view:View) {
        val inputEditTextField = EditText(this)
        val dialog = AlertDialog.Builder(this)
            .setTitle("collective")
            .setMessage("Enter the name of the collective you want to create")
            .setView(inputEditTextField)
            .setPositiveButton("OK") { _, _ ->
                //Calling a function to generate a unique collectiveID
                generateUniqueCollectiveID(inputEditTextField.text.toString())
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()

    }



    private fun generateUniqueCollectiveID(collectiveName : String) {
        val userID = userData[0]?.id.toString()

        //Creating a random 4 digit number
        val randomNumber: String = String.format("%04d", (0..9999).random())


        //Creating a unique collective ID consisting of the collective name and the random number
        val collectiveId = "$collectiveName#$randomNumber"


        if (userID == null) {
            Log.e(tag, "the userID is null")
            return
        }
        //Checking if there is a collective stored in the DB with the same collectiveID
        if (collectiveDocuments.contains(collectiveId)) {
            Log.e(tag, "There already exists a collective with the same collectiveID. Recalling the function to generate a new ID")
            generateUniqueCollectiveID(collectiveName)
        }
        else {
            Log.d("createCollective()", "There is no existing collective with the same collectiveID. Adding the collective to DB")
            //Adding the newly created collective to the DB
            addCollectiveToDB(collectiveName,collectiveId, userID)
        }

    }

    fun joinCollectiveByCollectiveID(view:View) {
        Log.e(tag, "IS IT WORKING?")
        val enteredCollectiveID : String = collectiveIdEditText.text.toString()
        if (enteredCollectiveID.isBlank()) {
            Toast.makeText(this, "Please enter a valid collective id", Toast.LENGTH_SHORT).show()
            return
        }
        Database().getDataFromDB("collective", enteredCollectiveID, object :DatabaseRequestListener {
            override fun onSuccess(data: MutableMap<String, Any?>?) {
                //Checking if the given collective ID doesn't exist
                if (data == null) {
                    Toast.makeText(this@CollectiveActivity, "Please enter a valid collective id", Toast.LENGTH_SHORT).show()
                    Log.d(tag, "Cannot find the a collective with the given id")
                    return
                }
                val membersMapData : MutableMap<String, String> = data["members"] as MutableMap<String, String>?
                    ?: return

                //Adding the user to the membersMapData, with the role of member
                membersMapData[userID] = "Member"
                Database().updateValueInDB("collective", enteredCollectiveID, "members", membersMapData, object : ResultListener {
                    override fun onSuccess() {

                        Database().updateValueInDB("users", userID, "collectiveID", enteredCollectiveID, object : ResultListener {
                            override fun onSuccess() {
                                Toast.makeText(this@CollectiveActivity, "You have successfully joined the collective!", Toast.LENGTH_LONG).show()
                                startTaskOverviewActivity(userID)
                                Log.d(tag, "Success in adding collective id to user")
                            }

                            override fun onFailure(error: Exception) {
                                Toast.makeText(this@CollectiveActivity, "Failure to join collective. Error: $error", Toast.LENGTH_LONG).show()
                                Log.e(tag, "Failure in adding collective id to user", error)
                            }

                        })
                    }
                    override fun onFailure(error: Exception) {
                        Toast.makeText(this@CollectiveActivity, "Failure to join collective. Error: $error", Toast.LENGTH_LONG).show()
                        Log.e(tag, "Failure in adding member to the collective member's map", error)

                    }

                })



            }

            override fun onFailure(error: Exception) {
                Toast.makeText(this@CollectiveActivity, "Failure to send request to database. Error: $error", Toast.LENGTH_LONG).show()
                Log.e(tag, "Database request failure", error)
            }

        })


    }



    private fun addCollectiveToDB(collectiveName: String,collectiveID : String, userID:String) {
        val members = mutableMapOf<String,String>()
        val requests = mutableMapOf<String,String>()

        members[userID] = "Owner"

        val collectiveInfo = hashMapOf(
            "name" to collectiveName,
            "members" to members,
            "requests" to requests
        )
        Database().addToDB("collective", collectiveID, collectiveInfo, object : ResultListener {
            override fun onSuccess() {
                Log.d(tag, "Collective successfully added to DB!")


                //Calling addCollectiveIDToUser() function to add the collectiveID to the userData
                addCollectiveIDToUser(collectiveID,userID)
            }

            override fun onFailure(error: Exception) {
                Log.e(tag, "Error adding collective to DB", error)
            }

        })

    }
    private fun addCollectiveIDToUser(collectiveID: String,userID: String) {
        //val collectiveInfo = hashMapOf(
        //    "collectiveID" to collectiveID
        // )
        Database().updateValueInDB("users", userID, "collectiveID", collectiveID, object : ResultListener {
            override fun onSuccess() {
                Log.d(tag, "Successfully added the collectiveID to user $userID")

                //Starting the SpecificCollectiveActivity
                startTaskOverviewActivity(userID)
            }

            override fun onFailure(error: Exception) {
                Log.e(tag, "Error adding collectiveID to user: $userID", error)
            }

        })

    }
    private fun startTaskOverviewActivity(userID:String) {
        val intent = Intent(this, TaskOverviewActivity::class.java)
        //Adding the userID to the intent
        intent.putExtra("uid", userID)
        startActivity(intent)
    }

}