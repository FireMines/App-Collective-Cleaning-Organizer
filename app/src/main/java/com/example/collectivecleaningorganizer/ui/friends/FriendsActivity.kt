package com.example.collectivecleaningorganizer.ui.friends

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.iterator
import com.example.collectivecleaningorganizer.database.Database
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

    /**
     * Initializes the ui by adding all friends present in the database to the scrollview
     */
    private fun init(){
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

    /**
     * Adds a friend to the scroll view in the ui
     * @param name is the friend to be added
     */
    private fun addFriendScroll(name: String){
        val view = layoutInflater.inflate(R.layout.friend, null)
        view.FriendName.text = name
        view.ButtonRemoveFriend.setOnClickListener{
            removeFriend(name)
        }
        FriendsScroll.addView(view)
    }

    /**
     * Informs the user if the database is not reached
     */
    private fun dbError(){
        Toast.makeText(
            this@FriendsActivity,
            "Error when communicating with the database",
            Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * Removes select friend from the ui and the database of both persons
     * The user will be asked if they are certain they want to remove the friend in order to avoid "misclicks"
     * @param name friend to be removed
     */
    private fun removeFriend(name:String) {
        Utilities().alertDialogBuilder(
            this,
            "Remove friend",
            "Are you sure you want to remove " + name + " from your friendslist?",
            null
        )
            .setPositiveButton("yes") { _, _ ->
                val i = FriendsScroll.iterator()
                while (i.hasNext()) {
                    val check = i.next()
                    if (check.FriendName.text.toString() == name) {
                        i.remove()
                    }
                }
                val username = Database.userData[0]?.data?.get("username").toString()
                val uId = Database.userData[0]?.id.toString()
                //Removes friend from own database field
                Database().getFriendsFromDB("users", uId, object : FriendListListener {
                    override fun onSuccess(friendList: ArrayList<String>) {
                        val it = friendList.iterator()
                        while (it.hasNext()) {
                            if (it.next().toString() == name) {
                                it.remove()
                            }
                        }
                        Database().updateValueInDB(
                            "users",
                            uId,
                            "Friends",
                            friendList,
                            object : ResultListener {
                                override fun onSuccess() {

                                }

                                override fun onFailure(error: Exception) {
                                    Log.e(tag, "Failure to update value in database")
                                    dbError()
                                }
                            })
                    }

                    override fun onFailure(error: Exception) {
                        Log.e(tag, "Failure to get friends")
                        dbError()
                    }
                })
                //Removes friend from friend's database field
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
                                Database().updateValueInDB(
                                    "users",
                                    uId,
                                    "Friends",
                                    friendList,
                                    object : ResultListener {
                                        override fun onSuccess() {
                                            Toast.makeText(
                                                this@FriendsActivity,
                                                "Friend removed",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                        override fun onFailure(error: Exception) {
                                            Log.e(tag, "Failure to update value in database")
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
            }
            .setNegativeButton("No", null)
            .create()
            .show()
    }

    /**
     * Sends a friend request to a user so long as:
     * The user is not already your friend
     * You have not already sent a request to this user
     * The field is not empty
     * The user is not yourself
     * The user exists
     * The user has not already sent a friend request to you
     */
    private fun sendRequest(){
        val userName = Database.userData[0]?.data?.get("username").toString()
        val userId = Database.userData[0]?.id.toString()
        if (FriendRequest.text.toString() == ""){
            Toast.makeText(
                this@FriendsActivity,
                "The field cannot be empty",
                Toast.LENGTH_SHORT
            ).show()
        }
        else if (FriendRequest.text.toString().lowercase() == userName){
            Toast.makeText(
                this@FriendsActivity,
                "You cannot become friends with yourself",
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
                            Database().getFriendRequestListFromDB("users", userId, object : FriendListListener{
                                override fun onSuccess(friendList: ArrayList<String>) {
                                    val it = friendList.iterator()
                                    var dupe = false
                                    while (it.hasNext()) {
                                        if (it.next().toString() == FriendRequest.text.toString().lowercase()) {
                                            dupe = true
                                        }
                                    }
                                    if (!dupe) {
                                        Database().getFriendsFromDB(
                                            "users",
                                            uId,
                                            object : FriendListListener {
                                                override fun onSuccess(friendList: ArrayList<String>) {
                                                    val it = friendList.iterator()
                                                    var dupe = false
                                                    while (it.hasNext()) {
                                                        if (it.next().toString() == userName) {
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
                                                                                .toString() == userName
                                                                        ) {
                                                                            dupe = true
                                                                        }
                                                                    }
                                                                    if (!dupe) {
                                                                        friendList.add(userName)
                                                                        Database().updateValueInDB(
                                                                            "users",
                                                                            uId,
                                                                            "FriendRequests",
                                                                            friendList,
                                                                            object :
                                                                                ResultListener {
                                                                                override fun onSuccess() {
                                                                                    Toast.makeText(
                                                                                        this@FriendsActivity,
                                                                                        "Request sent",
                                                                                        Toast.LENGTH_SHORT
                                                                                    ).show()
                                                                                }

                                                                                override fun onFailure(error: Exception) {
                                                                                    Log.e(tag, "Failure to update value in database")
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
                                                                    Log.e(tag, "Failure to get friends"
                                                                    )
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
                                    else{
                                        Toast.makeText(
                                            this@FriendsActivity,
                                            "This person has already sent a friend request to you",
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

    /**
     * This function changes the activity to friendrequests
     */
    private fun seeRequests(){
        val newIntent = Intent(this, FriendRequestsActivity::class.java)
        startActivity(newIntent)
        this.finish()
    }
}