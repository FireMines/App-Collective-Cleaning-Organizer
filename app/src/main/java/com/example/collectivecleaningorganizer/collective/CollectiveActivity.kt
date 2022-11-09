package com.example.collectivecleaningorganizer.collective

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.collectivecleaningorganizer.CreateUserActivity
import com.example.collectivecleaningorganizer.R
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_collective.*


class CollectiveActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    private var collectiveDocuments = mutableMapOf<String,QueryDocumentSnapshot>()
    private var userDocumentData = mutableMapOf<String, DocumentSnapshot>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collective)
        
        getAllCollectivesFromDB()
        getUserDocumentDataFromDB()


        //Constant listener to listen if the collection has been updated
        //val adapter : BaseAdapter = CollectiveMembersAdapter()
        //membersListView.adapter
        sendCollectiveJoinRequestButton.setOnClickListener {
            //println(collectiveDocuments.get("NTNU322"))
            //println(collectiveDocuments.get("NTNU#32")?.get("members"))

            val collectiveMembers = collectiveDocuments.get("NTNU#32")?.get("members") as MutableMap<String, String>
            startActivity(Intent(this, CreateUserActivity::class.java))



        }
    }
    fun checkIfUserIsInACollective() {
        val userID = "nBSu5tDXO9LuXEnKjsNR"
        val documents = db.collection("usersExample").document("bolt32").get().addOnSuccessListener { document ->
            val collectiveName = document.data?.get("collectiveID").toString()
            println(collectiveName)
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
                Log.d("getUserDocumentDataFromDB()", "Success in getting user's document data")
                userDocumentData[document.id] = document
            }
            .addOnFailureListener { e ->
                Log.e("getUserDocumentDataFromDB()", "Error retrieving user's document data", e)
            }
    }



    fun createCollective(view: View) {

        val userID = "testhjguh38u39"//intent.getStringExtra("uid")
        //Getting the name of the collective
        val collectiveName :String = "NTNU"
        //Creating a random 4 digit number
        val randomNumber: String = String.format("%04d", (0..9999).random())



        //Creating a unique collective ID consisting of the collective name and the random number
        val collectiveId = "$collectiveName#$randomNumber"


        if (userID == null) {
            Log.e("createCollective()", "the userID is null")
            return
        }
        //Checking if there is a collective stored in the DB with the same collectiveID
        if (collectiveDocuments.contains(collectiveId)) {
            Log.e("createCollective()", "There already exists a collective with the same collectiveID. Recalling the function to generate a new to generate a new ID")
            createCollective(createCollectiveButton)
        }
        else {
            Log.d("createCollective()", "There is no existing collective with the same collectiveID. Adding the collective to DB")
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
            .addOnSuccessListener { Log.d("addCollectiveToDB()", "Collective successfully added to DB!") }
            .addOnFailureListener { e -> Log.e("addCollectiveToDB()", "Error adding collective to DB", e) }
    }

}