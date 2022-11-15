package com.example.collectivecleaningorganizer.ui.collective

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.BaseAdapter
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.core.view.forEach
import com.example.collectivecleaningorganizer.Database
import com.example.collectivecleaningorganizer.R
import com.example.collectivecleaningorganizer.ui.login.CreateUserActivity
import com.example.collectivecleaningorganizer.userCollectiveData
import com.example.collectivecleaningorganizer.userData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.model.SnapshotVersion
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_specific_collective.*
import java.lang.Exception

class SpecificCollectiveActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    private val tag = "SpecificCollectiveActivity"
    private val collectiveMembersMap : MutableMap<String,String> = userCollectiveData[0]?.data?.get("members") as MutableMap<String, String>
    private val collectiveID : String = userCollectiveData[0]?.id.toString()
    private val userID = userData[0]?.id.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_specific_collective)

        val roleList : MutableList<String> = mutableListOf<String>("Owner","Member")
        val collectiveName : String = userCollectiveData[0]?.data?.get("name").toString()


        var isUserAnOwner : Boolean = checkIfUserIsAnOwner(collectiveMembersMap[userID].toString(), roleList[0])

        val adapter : BaseAdapter = CollectiveMembersAdapter(this, collectiveMembersMap,userID,roleList, isUserAnOwner,
            object : OnDataChange {
                @SuppressLint("LongLogTag")
                override fun collectiveMemberRolesChanged(updatedMembersMap: MutableMap<String, String>) {

                    if (!checkIfUserIsAnOwner(updatedMembersMap[userID].toString(), roleList[0])) {
                        isUserAnOwner = false
                        Log.d(tag,"User is no longer an owner. Removing all owner abilities")
                    }

                }
            })

        //Setting the contents of the layout
        collectiveNameTextView.text = collectiveName
        collectiveIDTextView.text = collectiveID
        collectiveMembersListView.adapter = adapter

    }
    fun leaveCollective(view:View) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Leaving confirmation")
            .setMessage("Are you sure you want to leave the collective?")
            .setPositiveButton("Yes") { _, _ ->

                Database().updateValueInDB("collective", collectiveID,"members",collectiveMembersMap,null)
                Database().updateValueInDB("users", userID,"collectiveID",null, object:ResultListener {
                    @SuppressLint("LongLogTag")
                    override fun onSuccess() {
                        Log.d(tag, "the collectiveID has successfully been removed from the user")
                        //Starting the specificCollectiveActivity
                        startActivity(Intent(this@SpecificCollectiveActivity, CollectiveActivity::class.java))

                    }
                    @SuppressLint("LongLogTag")
                    override fun onFailure(error: Exception) {
                        Log.e(tag, "Failed to remove the collectiveID from the user", error)
                    }
                })

            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    fun checkIfUserIsAnOwner(userRole : String, ownerRole : String ) : Boolean {
        //If the user isn't an owner, then they cant remove members, change roles or delete collection
        if (userRole.lowercase() != ownerRole.lowercase()) {
            //Removing the delete collection button as the user doesn't have permission for it
            deleteCollectiveButton.visibility = View.GONE
            //Removing the delete member button as the user doesn't have permission for it
            deleteMemberButton.visibility = View.GONE
            return false
        }
        return true
    }
}



