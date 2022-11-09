package com.example.collectivecleaningorganizer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore.MediaColumns.DOCUMENT_ID
import android.util.Log
import android.view.View
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_collective.*
import kotlinx.coroutines.tasks.await


class CollectiveActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collective)







        sendCollectiveJoinRequestButton.setOnClickListener {

        }
    }
    fun checkIfUserIsInACollective() {

        val documents = db.collection("usersExample").document("bolt32").get().addOnSuccessListener { document ->
            val collectiveName = document.data?.get("collectiveID").toString()
            println(collectiveName)
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
        //Checking if the there is a collective in the DB with the same collectiveId
        db.collection("collective").document(collectiveId)
            .get()
            .addOnSuccessListener { document ->
                //If there is no data found from the collectiveID, the collective dosnt exist
                if (document.data == null) {
                    Log.d("createCollective()", "There is no existing collective with the same collectiveID. Adding the collective to DB")
                    addCollectiveToDB(collectiveId, userID)
                }//Else data is found which means that there is an existing collective with the same collectiveID
                else {
                    Log.e("createCollective()", "There already exists a collective with the same collectiveID. Recalling the function to generate a new to generate a new ID")
                    createCollective(createCollectiveButton)
                }
            }
            .addOnFailureListener { e ->
                Log.e("createCollective()", "Error checking if a collective exists", e)
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