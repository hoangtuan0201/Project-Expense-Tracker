Project Expense Tracker Apps

Project expense tracker apps are helping the modern business as well as project management industry. These apps lay out the spending which does not take any risk of overspending a budget because all the costs are tracked. For example, if a user is tracking expenses for a project like "Office Renovation" or "Marketing Campaign," they should be able to create and manage expenses specifically tied to that project.

You are tasked with creating a project expense tracker app that allows their managers to record expense details of a certain project. The app will store details on the phone but can later upload them to a server so participants can view expense information and add further information.


A separate hybrid app should be developed for users. The features the apps should support are given below:

Features a) to f) are to be implemented as a native Android app coded in Java.  
Features g) and h) are to be implemented as a hybrid app coded using Xamarin/MAUI.

Your final app is the culmination of all your hard work on this course, which should become a strong addition to your programming portfolio. You should produce an app that is well-designed, robust and useful. The GUI design should be clean, simple to navigate, and operate smoothly without sluggishness or crashes. The app should not require instructions or a manual to use.

Part A – Implementation (80%)

Figure 1 A diagram to show the interactions among different components

Description of the application
The overall application contains two mobile apps: one for Admin and one for Customers.

Admin App (Android Native)
Enter details of a project and overall design of the app (10%):
Users must be able to enter all of the following fields. Required fields mean that the user must enter something in this field; otherwise, they will be prompted with an error message. Optional fields mean that the user can enter something if they wish, but they will not get an error message if nothing is entered.

The user should be able to enter the following details of a project:

Project ID/Code - Required field
Project Name - Required field
Project Description - Required field
Start Date - Required field
End Date - Required field
Project Manager/Owner - Required field
Project Status (Active, Completed, On Hold) - Required field
Project Budget - Required field
Special requirements (e.g., "Projector, Wi-Fi, Catering") - Optional field
Client/Department Information - Optional field
Any additional fields of your own invention – be creative!


Validation and confirmation:

The app will check the input, and if the user doesn’t enter anything in one of the required fields, the app should display an error message (e.g., “Please enter the event name”).
Once the details have been accepted by the app (e.g., no required fields were missing), it should display the event details back to the user for confirmation.
The user should be able to review the entered details and go back to edit any information if needed.

Store, view and delete project details or reset the database (10%)

All the details entered by the user should initially be stored on the device in an SQLite database.

The user should be able to list all the details of all projects that have been entered into the app, edit or delete individual projects.

Manage expense (15%)

The expense represents an expense activity taking place. A user should then be possible to add/edit/delete expenses to a project, including the following details:

Expense ID - Required field
Date of Expense - Required field
Amount - Required field
Currency - Required field
Type of Expense - Required field
Travel
Equipment
Materials
Services
Software/Licenses
Labour costs
Utilities
Miscellaneous
Payment Method - Required field
Cash
Credit Card
Bank Transfer
Cheque
Claimant (a person making a claim for the expense) - Required field
Payment Status (Paid, Pending, Reimbursed) - Required field
Description - Optional field
Location - Optional field

The user must be able to enter multiple schedules for a single event. The app should store all details entered on the device in an SQLite database. It should be possible for a user to select an event and display all schedules.


Search (10%)

The user should be able to search for a project in the database by name, description. Ideally the user should be able to enter the first few letters of the name and display all matching classes.
Advanced search options will allow searching for all projects with the following criteria: date, status, owner. It should be possible for a user to select an item from the resulting search list and to display its full details.

Upload details to a cloud-based web service (15%)

The user should be able to upload all the details of projects stored on the mobile device to a cloud- based web service. It should be possible for all projects to be uploaded at once.

Note that:
The app should check the status if there is a network and be able to make connection to the internet before uploading data.
Your application should handle the situation that if data in the local database is changed, it should be synchronised with the cloud database. If you can’t do this, just ignore it.

Extra features to Admin App

Add additional features to the Android app (5%)

Features a) to e) are the core requirements for the app. If you have implemented these and want to add some additional features, then you may. Any enhancements should be implemented in addition to NOT instead of the core requirements.  The idea is that these features stretch your skills, so be prepared to do your own research and feel free to show off!  You can think of your own enhancements.  Here are some possible examples:
Allow photos taken by the camera to be added to the data stored
Pick up the location automatically from the user's location
Anything you can think of – again be creative!






User App (Hybrid App)

Create a hybrid app of the app using .NET MAUI or other hybrid technology (10%)

Implement an app using a hybrid technology for use by users wishing to add their expenses. This app should connect to the cloud service that you created for the Android Admin app. The user should be able to view a list of available projects by connecting to the cloud service.
It should also be possible to search for projects by the name, date.
-Implement an GUI form (using MAUI or relevant cross-platform technologies) to input project expenses:
Connect to the cloud Database to retrieve list of projects and choose a project in the project list (by searching project using the name/date)
Add expenses to the chosen project (building a cross-platform form similar to question c) )
-Put the results in your report: screenshots for this functionalities, copying and pasting your source code

Extra features to User App

Implement setting favourite projects functionality in the hybrid app (5%)

The user should be able to set favourite projects so that they can quickly access these projects when needed.
