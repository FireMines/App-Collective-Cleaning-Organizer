package com.example.collectivecleaningorganizer.ui.utilities


import kotlin.Exception

/**
 * An interface that used to know if the collective member's role data is changed. Used as a custom event listener
 */
interface OnDataChange {
    /**
     * A function that is triggered when the role is changed
     * @param updatedMembersMap is the membersmap containing the updated role data
     */
    fun collectiveMemberRolesChanged(updatedMembersMap: MutableMap<String, String>)
}

/**
 * An interface used to know if the result of a function that uses the interface. Used as a custom event listener
 */
interface ResultListener {
    /**
     * Function that is triggered on success of the listener
     */
    fun onSuccess()

    /**
     * Function that is triggered on failure
     * @param error is the exception
     */
    fun onFailure(error :Exception)
}

/**
 * An interface used to listen for the database function that requests the user's friendlist. Used as a custom event listener
 */
interface FriendListListener {
    /**
     * A function that is triggered on success
     * @param friendList is the friendList retrieved from the database
     */
    fun onSuccess(friendList : ArrayList<String>)

    /**
     * Function that is triggered on failure
     * @param error is the exception
     */
    fun onFailure(error :Exception)
}

/**
 * An interface used to listen for the database function that requests the usernames data. Used as a custom event listener
 */
interface UniqueUsernameListener {
    /**
     * Function that is triggered on successful database request
     * @param unique true or false depending on if the username is unique
     */
    fun onSuccess(unique : Boolean)
    /**
     * Function that is triggered on failure
     * @param error is the exception
     */
    fun onFailure(error :Exception)
}

/**
 * An interface used to listen for the database function that requests the userID of a user. Used as a custom event listener
 */
interface StringListener{
    /**
     * Function that is triggered on successful database request
     * @param uId is the userID of the username the database request was done for
     */
    fun onSuccess(uId : String)
    /**
     * Function that is triggered on failure
     * @param error is the exception
     */
    fun onFailure(error :Exception)
}

/**
 * A function used to listen for the database function that retrieves data from the DB. Used as a custom event listener
 */
interface DatabaseRequestListener {
    /**
     * Function that is triggered on successful database request
     * @param data is the data retrieved from the database request
     */
    fun onSuccess(data : MutableMap<String,Any?>?)
    /**
     * Function that is triggered on failure
     * @param error is the exception
     */
    fun onFailure(error :Exception)
}