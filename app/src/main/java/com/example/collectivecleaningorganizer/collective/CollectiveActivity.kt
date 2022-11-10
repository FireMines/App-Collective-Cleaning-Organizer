package com.example.collectivecleaningorganizer.collective

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.collectivecleaningorganizer.*
import com.example.collectivecleaningorganizer.R
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_collective.*
import kotlinx.android.synthetic.main.popup_with_edittext.*
import kotlinx.android.synthetic.main.popup_with_edittext.view.*
import kotlinx.coroutines.tasks.await


class CollectiveActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    private var collectiveDocuments = mutableMapOf<String,QueryDocumentSnapshot>()
    private var userDocumentData = mutableMapOf<String, DocumentSnapshot>()
    private var tettst = mutableListOf<DocumentSnapshot>()
    private var tag = "CollectiveActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collective)

        val userID = intent.getStringExtra("uid")
        getAllCollectivesFromDB()
        getUserDocumentDataFromDB()


        /*
        if (userData.get(userID)?.contains("collectiveID") == true && userData[userID]?.get("collectiveID") != null) {
            val intentSpecificCollectiveActivity :Intent = Intent(this, SpecificCollectiveActivity::class.java)
            intentSpecificCollectiveActivity.putExtra("collectiveID", userData[userID]?.get("collectiveID").toString())
            startActivity(intentSpecificCollectiveActivity)
            return
        }

         */


        //println(MainActivity().usersExampleCollection["NTNU32"]?.data?.values.)

        //Constant listener to listen if the collection has been updated
        //val adapter : BaseAdapter = CollectiveMembersAdapter()
        //membersListView.adapter
        sendCollectiveJoinRequestButton.setOnClickListener {
            //println(collectiveDocuments.get("NTNU322"))
            //println(collectiveDocuments.get("NTNU#32")?.get("members"))
            //println(tettst)

            //val collectiveMembers = collectiveDocuments.get("NTNU#32")?.get("members") as MutableMap<String, String>
            /*
            db.collection("usersExample").get().addOnSuccessListener { tasks ->

                println(tasks.documents)
            }

             */




        }
    }

    //WHEN IT COMES TO CHECK IF CATAGORY EXISTS ALREADY
    //Get all the documents with their IDs and add them to a list/map.
    //Compare the documentID in the list with the collectiveID.

    fun getAllCollectivesFromDB() {
        db.collection("collective").get().addOnSuccessListener { documents ->
            for (document in documents) {
               //println(document)
                collectiveDocuments[document.id] = document
            }
        }

    }
    @SuppressLint("LongLogTag")
    fun getUserDocumentDataFromDB() {
        db.collection("usersExample").document("nBSu5tDXO9LuXEnKjsNR")
            .get()
            .addOnSuccessListener { document ->
                Log.d(tag, "Success in getting user's document data")
                userDocumentData[document.id] = document
            }
            .addOnFailureListener { e ->
                Log.e(tag, "Error retrieving user's document data", e)
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



    fun generateUniqueCollectiveID(collectiveName : String) {
        val userID = intent.getStringExtra("uid")

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
            addCollectiveToDB(collectiveId, userID)
        }

    }


    private fun addCollectiveToDB(collectiveID : String, userID:String) {
        val members = mutableMapOf<String,String>()

        members[userID] = "Owner"

        val collectiveInfo = hashMapOf(
            "members" to members
        )
        db.collection("collective").document(collectiveID).set(collectiveInfo)
            .addOnSuccessListener {
                Log.d(tag, "Collective successfully added to DB!")
                //Calling addCollectiveIDToUser() function to add the collectiveID to the userData
                addCollectiveIDToUser(collectiveID,userID)
            }
            .addOnFailureListener { e -> Log.e(tag, "Error adding collective to DB", e) }

    }
    private fun addCollectiveIDToUser(collectiveID: String,userID: String) {
        db.collection("users").document(userID).set("collectiveID" to collectiveID)
            .addOnSuccessListener {
            Log.d(tag, "Successfully added the collectiveID to user $userID")
            }
            .addOnFailureListener { e ->
                Log.e(tag, "Error adding collectiveID to user: $userID", e) }
    }

}