package com.example.collectivecleaningorganizer.ui.collective

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.core.view.iterator
import com.example.collectivecleaningorganizer.R
import com.example.collectivecleaningorganizer.ui.utilities.OnDataChange
import com.example.collectivecleaningorganizer.ui.utilities.Utilities
import kotlinx.android.synthetic.main.activity_specific_collective.*
import kotlinx.android.synthetic.main.collective_member_row.view.*
/**
 * A BaseAdapter class used to create a custom adapter for the collectiveMembersListView used in the SpecificCollectiveActivity and its corresponding layout file.
 * @param context is the Activity that is used
 * @param membersMap is a MutableMap of the members class, holding all the member username and their role in the collective
 * @param username is the usernames of the collective members
 * @param roleList is the list of the collective roles
 * @param changeMemberRolePermission is a boolean used to know if the user changes his own role
 * @param onDataChange is a custom listener used to listen for role changes
 * @return BaseAdapter The custom built adapter
 */
class CollectiveMembersAdapter(val context: Activity, val membersMap :MutableMap<String,String>, val username :String,
                               val roleList: MutableList<String>, var changeMemberRolePermission : Boolean,
                               val onDataChange: OnDataChange
) : BaseAdapter() {
    //Initializing an arraylist for the member names which are the map keys from the membersMap parameter
    private val memberNameList : ArrayList<String> = ArrayList(membersMap.keys)

    //Initializing an arraylist for the member roles which are the map values from the membersMap parameter
    private var memberRoleList : ArrayList<String> = ArrayList(membersMap.values)

    //Initializing a tag used for logging to know which file the log message came from
    private val tag : String = "CollectiveMembersAdapter"

    /**
     * A function used to get the amount of rows/items.
     * @return Int returns the number of rows/items
     */
    override fun getCount(): Int {
        return memberNameList.size

    }
    /**
     * A function used to get item from the adapter
     * @param p0 is an Int used to get a item in the adapter which is represented as a number
     * @return returns the name of the member in the collective found in the Adapter at the param position.
     */
    override fun getItem(p0: Int): Any {
        return memberNameList[p0]

    }
    /**
     * A function used to get the item's ID in the adapter.
     * @param p0 is an Int used to get a item in the adapter which is represented as a number
     * @return 0
     */
    override fun getItemId(p0: Int): Long {
        return 0

    }
    /**
     * A function used to get view that is modified, in terms of inflate the current view and add a textView and a spinner inside a listview row.
     * @param p0 is an Int that represents the row in the adapter.
     * @param p1 is the view if we have one
     * @param p2 is the viewgroup if there is one
     * @return View returning the newly modified view.
     */
    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        //Inflating the context activity layout
        var inflater = context.layoutInflater

        //Inflating it with the collective_member_row layout
        var view1 = inflater.inflate(R.layout.collective_member_row, null)

        //Initializing the textview from the collective_member_row layout
        var collectiveMemberName = view1.collectiveMemberTextView

        //Initializing the spinner from the collective_member_row layout
        val roleSpinner = view1.collectiveRolesSpinner

        //Creating an array adapter for the spinner
        val spinnerAdapter = ArrayAdapter<String>(context,android.R.layout.simple_dropdown_item_1line,roleList)

        //Setting the name from the memberNameList into the textView that we have initialized.
        collectiveMemberName.text = memberNameList[p0]

        //Attaching the adapter we created for the spinner to the spinner itself
        roleSpinner.adapter = spinnerAdapter

        //Getting the member's role position in the spinner
        var rolePositionInSpinner = spinnerAdapter.getPosition(memberRoleList[p0])

        //If the member's role is not in the role list, set the role shown in spinner as the lowest role
        if (rolePositionInSpinner == -1) {
            rolePositionInSpinner = roleList.size-1
        }

        //Disabling spinner if the user doesn't have permission to change member roles
        if (!changeMemberRolePermission) {
            roleSpinner.isEnabled = false
        }

        //Setting the spinner selection value to the member's role which was found in memberRoleList
        roleSpinner.setSelection(rolePositionInSpinner)

        //Initializing an Adapterview onitemselectedlistener for the spinner
        roleSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                return
            }
            @SuppressLint("LongLogTag")
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                //If the selected item is the same as it was before, don't do anything
                if (memberRoleList[p0] == parent.getItemAtPosition(position)) {
                    return
                }

                //Initializing a variable that shows the amount of Owners in the collective
                val amountOfOwnersInCollective : Int? = memberRoleList.groupingBy { it }.eachCount()["Owner"]

                //If there for some reason are 0 owners, make the first member a owner
                if (amountOfOwnersInCollective == null) {
                    Log.e(tag, "Getting the value null when retrieving the amount of owners in the collective")
                    return
                }

                //Handling if there is only one owner left and the user tries to change the owner to a member role.
                if (amountOfOwnersInCollective <= 1 && memberRoleList[p0] == "Owner" && parent.getItemAtPosition(position) == "Member") {
                    parent.setSelection(rolePositionInSpinner)
                    Toast.makeText(context, "There has to be at least 1 Owner in the collective", Toast.LENGTH_LONG).show()
                    Log.d(tag, "Failed to change user's role form owner to member. Reason: There has to be at least 1 owner in the collective")
                    return
                }
                //Initializing a title used for the confirmation dialog
                val title : String = "Changing role confirmation"
                //Initializing a message with an empty string, used for the confirmation dialog
                var message : String = ""
                //Checking if the logged in user wants to change its own role from Owner to Member
                if (memberRoleList[p0] == "Owner" && parent.getItemAtPosition(position) == "Member" && memberNameList[p0] == username) {
                    //Initializing the message variable with a message confirming that the logged in user will lose abilities
                    message = "Are you sure you want to change your own role from Owner to Member? You will lose the ability " +
                    "to delete the collective and remove members"
                }
                //Checking if the logged in user wants to change a member's role from Owner to Member
                else if (memberRoleList[p0] == "Owner" && parent.getItemAtPosition(position) == "Member" ) {
                    //Initializing the message variable with a message confirming that member will lose abilities
                    message = "Are you sure you want to change ${memberNameList[p0]}'s from Owner to Member? The person will lose the ability " +
                            "to delete the collective and remove members"
                }
                //Else checking if the logged in user wants to change the role of a member from Member to Owner
                else  {
                    //Initializing the message variable with a message confirming that member will gain abilities
                    message = "Are you sure you want to change ${memberNameList[p0]}'s from Member to Owner? The person will gain the ability " +
                            "to delete the collective and remove members"
                }

                //Creating an confirmation alert dialog confirming if the user wants to change someone's role or not
                Utilities().alertDialogBuilder(context, title, message,null)
                    .setPositiveButton("Confirm ") { _, _ ->
                        try {
                            //Getting the selected role from the spinner
                            val selectedRole = parent.getItemAtPosition(position).toString()

                            //Updating the member's new role in the memberRoleList with the selectedRole
                            memberRoleList[p0] = selectedRole

                            //Updating the member's role in the membersMap with the selectedRole
                            membersMap[memberNameList[p0]] = memberRoleList[p0]

                            //If the user's own rank is changed from owner to Member, the user will lose the ability to change member roles.
                            if (membersMap[username] != "Owner") {
                                //Iterating through the collectiveMembersListView and disabling the spinner used for changing roles
                                for (context in context.collectiveMembersListView.iterator()) {
                                    //Changing the changeMemberRolePermission to false to avoid
                                    //the spinners being enabled again due to scrolling in the listView triggers the getview() function
                                    changeMemberRolePermission = false

                                    //Disabling the spinner
                                    context.collectiveRolesSpinner.isEnabled = false
                                }
                            }
                            //Sending a success msg to user
                            Toast.makeText(context, "Successful in changing the member's role", Toast.LENGTH_SHORT).show()

                            //Attaching the onDataChange interface with the spinner listener and add the updated membersMap
                            onDataChange.collectiveMemberRolesChanged(membersMap)
                        }
                        catch (error : Exception) {
                            Log.e(tag, "An error occured when trying to change the role")
                            parent.setSelection(rolePositionInSpinner)
                        }
                    }
                    .setNegativeButton("Cancel") { _, _ ->
                        //Setting the selected value back to what it was
                        parent.setSelection(rolePositionInSpinner)
                    }
                    .create()
                    .show()
            }

        }

        return view1
    }


}