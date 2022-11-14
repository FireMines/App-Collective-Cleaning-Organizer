package com.example.collectivecleaningorganizer.ui.collective

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import com.example.collectivecleaningorganizer.*
import com.example.collectivecleaningorganizer.R
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_collective.*


class CollectiveActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    private var tag = "CollectiveActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collective)


        val collectiveID = userData[0]?.data?.get("collectiveID")
        if (collectiveID != null) {
            //Database().databaseDataChangeListener("collective", collectiveID.toString(), userCollectiveData)
            startActivity(Intent(this, SpecificCollectiveActivity::class.java))
            return
        }

        sendCollectiveJoinRequestButton.setOnClickListener {


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

    fun sendCollectiveJoinRequest(view:View) {
        val enteredCollectiveID : String = collectiveIdEditText.text.toString()
        if (!collectiveDocuments.contains(enteredCollectiveID)) {
            //Print alert dialog explaning that the entered collectiveID is wrong and to try again
            return
        }
        db.collection("collective").document(enteredCollectiveID).get()
            .addOnSuccessListener { document ->
                if (document.data == null) {
                    //Alert dialog with error message telling the user that the collective dosnt exist
                    return@addOnSuccessListener
                }
                //val requestsMap : document.data.get("requests")
                if (document.data != null) {

                }

            }

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
        db.collection("collective").document(collectiveID).set(collectiveInfo)
            .addOnSuccessListener {
                Log.d(tag, "Collective successfully added to DB!")

                Database().databaseDataChangeListener("collective", collectiveID, userCollectiveData,object : ResultListener {
                    override fun onResult(isAdded: Boolean) {
                        //Calling addCollectiveIDToUser() function to add the collectiveID to the userData
                        addCollectiveIDToUser(collectiveID,userID)
                    }

                })


            }
            .addOnFailureListener { e ->
                Log.e(tag, "Error adding collective to DB", e)
            }

    }
    private fun addCollectiveIDToUser(collectiveID: String,userID: String) {
        val collectiveInfo = hashMapOf(
            "collectiveID" to collectiveID
        )
        db.collection("users").document(userID).set(collectiveInfo)
            .addOnSuccessListener {
            Log.d(tag, "Successfully added the collectiveID to user $userID")

                startActivity(Intent(this, SpecificCollectiveActivity::class.java))
            }
            .addOnFailureListener { e ->
                Log.e(tag, "Error adding collectiveID to user: $userID", e) }
    }

}