package com.example.boardtogether

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.example.boardtogether.databinding.ActivityFriendsprofileBinding
import com.example.boardtogether.venneprofil.FriendProfileModel

class FriendProfileActivity : HamburgerBaseActivity() {
    //private lateinit var button             : Button
    private lateinit var binding                : ActivityFriendsprofileBinding
    private lateinit var name                   : TextView
    private lateinit var username               : TextView
    private lateinit var profilepicture         : ImageView
    private lateinit var profiledescription     : TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFriendsprofileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setTitle("Friends profile")

        //setContentView(R.layout.activity_friendsprofile)

        name = findViewById(R.id.profileName_tv)
        username = findViewById(R.id.friendUsername_tv)
        profiledescription = findViewById(R.id.profileDescription_tv)
        profilepicture = findViewById(R.id.vennebilde_iv)

        intent?.let {
            val profil = intent.extras?.getParcelable("profil") as FriendProfileModel?
            if (profil != null) {
                name.text = profil.name
                username.text = profil.username
                //profilbeskrivelse.text = profil.brukerBeskrivelse
                profilepicture.setImageResource(profil.picture)
            }
        }
        //button = findViewById(R.id.button)
        //button.setOnClickListener {
        //    val intent = Intent(this, FoodMenuActivity::class.java)
        //    startActivity(intent)
        //}
    }

}