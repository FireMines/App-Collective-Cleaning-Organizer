package com.example.collectivecleaningorganizer.ui.collective

import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import androidx.core.view.get
import com.example.collectivecleaningorganizer.R
import kotlinx.android.synthetic.main.activity_specific_collective.view.*
import kotlinx.android.synthetic.main.collective_member_row.view.*
/**
 * A class used to create a custom adapter for the collectiveMembersListView used in the SpecificCollectiveActivity and its corresponding layout file.
 * @param context is the Activity that is used
 * @param membersMap is a MutableMap of the members class, holding all the member username and their role in the collective
 * @return BaseAdapter
 */
class CollectiveMembersAdapter(val context: Activity, val membersMap :MutableMap<String,String>, val roleList: MutableList<String>) : BaseAdapter() {

    private val memberNameList = ArrayList<String>(membersMap.keys)
    private val memberRoleList = ArrayList<String>(membersMap.values)

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
        val roleSpinner = view1.role_spinner

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

        //Setting the spinner selection value to the member's role which was found in memberRoleList
        roleSpinner.setSelection(rolePositionInSpinner)


        return view1
    }
}