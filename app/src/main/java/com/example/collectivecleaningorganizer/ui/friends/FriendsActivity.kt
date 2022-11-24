package com.example.collectivecleaningorganizer.ui.friends

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.iterator
import com.example.collectivecleaningorganizer.Database
import com.example.collectivecleaningorganizer.R
import com.example.collectivecleaningorganizer.ui.utilities.FriendListListener
import com.example.collectivecleaningorganizer.ui.utilities.ResultListener
import com.example.collectivecleaningorganizer.ui.utilities.StringListener
import com.example.collectivecleaningorganizer.ui.utilities.Utilities
import kotlinx.android.synthetic.main.activity_friends.*
import kotlinx.android.synthetic.main.friend.view.*
import java.lang.Exception


class FriendsActivity : AppCompatActivity(){
    private var tag: String = "FriendsActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)
        SendRequestButton.setOnClickListener{
            sendRequest()
        }
        SeeRequestsButton.setOnClickListener{
            seeRequests()
        }
        init()

       //Handles the applications navigation
       Utilities().navigation(this, R.id.friends, bottom_navigator_friends)

    }


    private fun init(){
        //Hent venner fra database og vis i scrollview

        val uId = Database.userData[0]?.id.toString()

        Database().getFriendsFromDB("users", uId, object : FriendListListener {
            override fun onSuccess(friendList: ArrayList<String>) {
                val it = friendList.iterator()
                while (it.hasNext()) {
                    addFriendScroll(it.next().toString())
                }
            }
            override fun onFailure(error: Exception) {
                Log.e(tag, "Failure to get friends")
                dbError()
            }

        })

    }

    private fun addFriendScroll(name: String){
        val view = layoutInflater.inflate(R.layout.friend, null)
        view.FriendName.text = name
        view.ButtonRemoveFriend.setOnClickListener{
            removeFriend(name)
        }
        FriendsScroll.addView(view)
    }

    private fun dbError(){
        Toast.makeText(
            this@FriendsActivity,
            "Error when communicating with the database",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun removeFriend(name:String){
        //Er du sikker alert maybe?
        //Om navn ikke er unikt kan en id legges til i hvert element og brukes isteden
        val i = FriendsScroll.iterator()
        while (i.hasNext()){
            val check = i.next()
            if (check.FriendName.text.toString() == name){
                i.remove()
            }
        }
        val username = Database.userData[0]?.data?.get("username").toString()
        val uId = Database.userData[0]?.id.toString()
        Database().getFriendsFromDB("users", uId, object : FriendListListener{
            override fun onSuccess(friendList: ArrayList<String>) {
                val it = friendList.iterator()
                while (it.hasNext()) {
                    if (it.next().toString() == name) {
                        it.remove()
                    }
                }
                    Database().updateValueInDB("users", uId, "Friends", friendList, object : ResultListener {
                        override fun onSuccess() {

                        }

                        override fun onFailure(error: Exception) {
                            Log.e(tag, "Failure with listener")
                            dbError()
                        }
                    })
                }
            override fun onFailure(error: Exception) {
                Log.e(tag, "Failure to get friends")
                dbError()
            }
        })

        Database().getUid(name, object : StringListener {
            override fun onSuccess(uId: String) {
                Database().getFriendsFromDB("users", uId, object : FriendListListener {
                    override fun onSuccess(friendList: ArrayList<String>) {
                        val it = friendList.iterator()
                        while (it.hasNext()) {
                            if (it.next().toString() == username) {
                                it.remove()
                            }
                        }
                        Database().updateValueInDB("users", uId, "Friends", friendList, object : ResultListener {
                                override fun onSuccess() {

                                }

                                override fun onFailure(error: Exception) {
                                    Log.e(tag, "Failure with listener")
                                    dbError()
                                }
                            })
                    }

                    override fun onFailure(error: Exception) {
                        Log.e(tag, "Failure to get friends")
                        dbError()
                    }
                })
            }

            override fun onFailure(error: Exception) {
                Log.e(tag, "Failure to get user id")
                dbError()
            }
        })
        Toast.makeText(
            this@FriendsActivity,
            "Friend removed",
            Toast.LENGTH_SHORT
        ).show()
    }


    private fun sendRequest(){
        //Steder som nevner userId burde endres til cache
        //val userId = FirebaseAuth.getInstance().uid.toString()
        val userId = Database.userData[0]?.data?.get("username").toString()
        //val idArr = arrayListOf<String>()
        //idArr.add(userId.toString())
        //FriendRequest.text.toString()
        if (FriendRequest.text.toString() == ""){
            Toast.makeText(
                this@FriendsActivity,
                "The field cannot be empty",
                Toast.LENGTH_SHORT
            ).show()
        }
        else {
            Database().getUid(
                FriendRequest.text.toString().lowercase(),
                object : StringListener {
                    override fun onSuccess(uId: String) {
                        if (uId == "") {
                            Toast.makeText(
                                this@FriendsActivity,
                                "The user does not exist",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Database().getFriendsFromDB(
                                "users",
                                uId,
                                object : FriendListListener {
                                    override fun onSuccess(friendList: ArrayList<String>) {
                                        val it = friendList.iterator()
                                        var dupe = false
                                        while (it.hasNext()) {
                                            if (it.next().toString() == userId) {
                                                dupe = true
                                            }
                                        }
                                        if (!dupe) {
                                            Database().getFriendRequestListFromDB(
                                                "users",
                                                uId,
                                                object : FriendListListener {
                                                    override fun onSuccess(friendList: ArrayList<String>) {
                                                        val it = friendList.iterator()
                                                        var dupe = false
                                                        while (it.hasNext()) {
                                                            if (it.next()
                                                                    .toString() == userId
                                                            ) {
                                                                dupe = true
                                                            }
                                                        }
                                                        if (!dupe) {
                                                            friendList.add(userId)
                                                            Database().updateValueInDB(
                                                                "users",
                                                                uId,
                                                                "FriendRequests",
                                                                friendList,
                                                                object : ResultListener {
                                                                    override fun onSuccess() {
                                                                        Toast.makeText(
                                                                            this@FriendsActivity,
                                                                            "Request sent",
                                                                            Toast.LENGTH_SHORT
                                                                        ).show()
                                                                    }

                                                                    override fun onFailure(error: Exception) {
                                                                        Log.e(tag, "Failure with listener")
                                                                        dbError()
                                                                    }
                                                                })
                                                        } else {
                                                            Toast.makeText(
                                                                this@FriendsActivity,
                                                                "You have already sent a request to this user",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    }

                                                    override fun onFailure(error: Exception) {
                                                        Log.e(tag, "Failure to get friends")
                                                        dbError()
                                                    }
                                                })
                                        } else {
                                            Toast.makeText(
                                                this@FriendsActivity,
                                                "This person is already your friend",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }

                                    override fun onFailure(error: Exception) {
                                        Log.e(tag, "Failure to get friend requests")
                                        dbError()
                                    }

                                })
                        }
                    }
                    override fun onFailure(error: Exception) {
                        Log.e(tag, "Failure to get user id")
                        dbError()
                    }
                })
        }
    }
    private fun seeRequests(){
        val newIntent = Intent(this, FriendRequestsActivity::class.java)
        startActivity(newIntent)
    }
}