# Septa Tracker

### Introduction

The Southeastern Pennsylvania Transportation Authority (SEPTA) is a metropolitan and regional transportation agency that operates various forms of public transit—bus, subway and elevated rail, commuter rail, light rail and electric trolleybus in and around Philadelphia, Pennsylvania, United States.
Septa Tracker is an Android application to interface with [SEPTA API](http://www3.septa.org/index.html) to show real-time regional-rail train information.

### Screenshots

<img src="https://github.com/mtuan93/Septa-Tracker/blob/master/app.png" width="200">
<img src="https://github.com/mtuan93/Septa-Tracker/blob/master/start.png" width="200">
<img src="https://github.com/mtuan93/Septa-Tracker/blob/master/auto.png" width="200">
<img src="https://github.com/mtuan93/Septa-Tracker/blob/master/train.png" width="200">
<img src="https://github.com/mtuan93/Septa-Tracker/blob/master/map.png" width="200">

### How the app works

There is only one class, `MainActivity`, that will handle the following sub-tasks:

* `getStations`: is an `AsyncTask`. It will read all the available stations names from `http://www3.septa.org/hackathon/Arrivals/station_id_name.csv`, and store them in an `ArrayList`. This task is handled in `doInBackground`. After `doInBackground` is executed, `onPostExecute` is called to set up two "auto complete" input field. One will be to store source station and another one is for storing destination station.

* `search_button` : on the main screen, there is a search button to search for all the available train that go from selected source station to selected destination station. The button will be attached an onClick listener to read source station and destination station, then format them and pass to `getTrainList`.

* `getTrainList` : is an `AsyncTask`. In `doInBackground`, it will use the live SEPTA API `http://www3.septa.org/hackathon/TrainView/` to read and store all the currently running trains with associated source and destination. In `onPostExecute`, it will actually find all the trains that match the users' selected source and destination and store them as well as their coordinates values into a `HashMap` for displaying them in Google Map View later.

* `getTimes` : is an `AsyncTask`. In `doInBackground`, it will use the live SEPTA API `http://www3.septa.org/hackathon/RRSchedules/{trainNumber}` to find and store the live time of the train based on its train number. In `onPostExecute`, it will parse the JSON data above into fields: "Train Number", "Source Station", "Destination Station", "Departure Time", and "Arrival Time" and stored them inside a HashMap. Finally, it will set up a `SimpleAdapter` to display a list view of each matched train with associated fields above.

* `getTrainInfo` : is an `AsycTask` and similar to `getTrainList`. In `onPostExecute`, it will handle a toaste to toast the stop that the train most recently departed, the time it was scheduled to depart that station, and the time that it actually departed that station. `getTrainInfo` is called when the user long-press on one of the matched train in the list view to display the above info.

Finally, in `onCreate`, there is a `View Map` button that will trigger set up a map (`MapFragment`) with the matched train displayed as a train icon. 

### Additional Development Tools:
* IDE: Android Studio.
* Helper Libraries: gson-2.3.1.jar, SEPTA API.

### Demo:
There are two easy ways to check this application out:
* Since the app is developed in Android Studio, you can clone this app repository and import it into your Android Studio. Then either run it with an emulator or on an actual device (what I do). More information on how to do this is availabe [here](https://developer.android.com/tools/building/building-studio.html).
* You can download the `apk` file [here](https://github.com/mtuan93/Septa-Tracker/raw/master/app-debug.apk) and install it into your android device. Since this is an unregistered debug version, make sure that you enable `Unknown sources` in `Security` section of the phone.