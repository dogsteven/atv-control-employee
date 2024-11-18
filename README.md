#### Introduction

This is a project to manage the playing time of ATVs in an amusement park. The typical scenario is as follows:
1. A player buys one or two tickets for a session.
2. Then a staff presses a button to activate an ATV for that session based on the number of tickets purchased.
3. Then the ATV is activated, and the information about the session is stored in the database.

### ATV Control - Employee

`ATV Control - Employee` is an Android application for employees to use to activate ATVs.
In addition to the amusement parks in fixed locations, on Lunar New Year days, our client may bring some ATVs to the flower markets to operate.
In this case, the wifi can be guaranteed, but the internet is not. Therefore, **the employee application must be able to operate without an internet connection**.
Our client doesn't want to pay any cost for the infrastructure, so we decided to use Firestore as the database because it's free.
Since there's no backend, we wrote the domain logic into the client application.
The Firestore SDK has an amazing offline cache feature, so we didn't need to write any caching logic, helping us save a lot of time.
Finally, the `AndroidID` is used to manage access, so only the employees' phones can use the application.

### ATV Control - Owner

`ATV Control - Employee` is an Android application for the owner for managing ATVs, employees and configuration.
It's just a typical CRUD application with an analytic screen.