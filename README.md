# Collective cleaning organizer 

An app to help members of a collective housing to better clean and organize there collective.


## How to run

To run this application you need to use the NTNU VPN.

This application is written in kotlin and uses the Gradle build system.
To run the app import the project into android studio by using "Import Project".


## Firebase

This application uses two Firebase components:

* Firestore is used to store all data about different collectives and users.
* Firebase Authentification is used to gain secure authentification by use of email and password. 


## Features

The application has the following features:

* Create a user
* Log in
* Create a collective
* Join a collective by either writing the ID of the collective they want to join or accept an invite to join a collective
* Create tasks where you write the task name, description, due date, assign members, and the category of the task.
* Edit a task’s details
* Create a category
* Delete a category
* View all tasks for the collective
* View tasks assigned to only the logged in use
* Sort tasks by selected category
* Send friend requests and accept friend requests
* Remove friends
* Invite members to collective by either username or from friends list
* Leave collective
* If user is an owner, they will have additional collective functionalities such as changing the roles of members, deleting the collective, and removing member
