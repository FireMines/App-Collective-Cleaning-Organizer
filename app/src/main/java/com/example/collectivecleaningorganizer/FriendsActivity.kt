package com.example.collectivecleaningorganizer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.iterator
import kotlinx.android.synthetic.main.activity_friends.*
import kotlinx.android.synthetic.main.friend.view.*


class FriendsActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)
        init()
    }

    private fun init(){
        //Hent venner fra database og vis i scrollview

        //Testdata
        addFriendScroll("Jeff")
        addFriendScroll("Greg")
        addFriendScroll("Humphrey Figglebottom")

    }

    private fun addFriendScroll(name: String){
        val view = layoutInflater.inflate(R.layout.friend, null)
        view.FriendName.text = name
        view.ButtonRemoveFriend.setOnClickListener{
            removeFriend(name)
        }
        FriendsScroll.addView(view)
    }

    private fun removeFriend(name:String){
        //Er du sikker alert maybe?
        //Om navn ikke er unikt kan en id legges til i hvert element og brukes isteden
        val i = FriendsScroll.iterator()
        while (i.hasNext()){
            val check = i.next()
            if (check.FriendName.text.toString() == name){
                i.remove()
                //Also remove from database entry
            }
        }
    }
}