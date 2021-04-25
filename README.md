# Project Limb Rescue

Technical Team:
Michael Montelone,
Nathan Newcomer,
Daniel Simpkins,
Cole Swartz

Sponsors:
Lynne Brophy, MSN, RN-BC, ARPN-CNS, AOCN
Carlo Contreras, MD, FASC

## Project Mission

Gather data on the water content of a limb using non-invasive wearable devices to assist in an early-warning system for patients at risk of lymphedema.

## Application Components

### Database

The application stores reading data in an SQLite database using the [Room persistence library](https://developer.android.com/training/data-storage/room); the associated files can be found in the "db" folder. In total, the database manages four primary tables:

**Device**: A device is a type of smart technology, such as a smartwatch or smart scale, that contains one or more sensors. One ‘device’ entry refers to a type of device, rather than a single particular watch or scale, although it does specify a device placed on a specific limb or combination of limbs.

**Reading**: A reading is a single instance of raw data collected from a type of sensor, on a particular device, about a given limb, during a single session. It numerically represents some output recorded by the sensor.

**Sensor**: A sensor is a device that takes readings of health data. It is situated in devices and may produce readings. One ‘sensor’ entry refers to a type of sensor, rather than a single particular one on a device.

**Session**: A session is a continuous period of data collection begun and ended by user input. It is made up of readings, and may read from multiple sensors and/or multiple devices over the same time period.

There also exist three entities (**DeviceContainsSensor**, **SessionMeasuresSensor** **SessionReadsFromDevice**) that manage the many-to-many relationships between rows in the above tables (for instance, one device may contain many sensor, or one session may read from multiple devices).

Database information from these tables can be retrieved using the methods in their associated DAO (data access object) classes. These methods return Java objects corresponding to the retrieved data, in the form of Guava ListenableFutures (to maintain separation from the UI thread).

### Graph

The application uses [Androidplot](http://androidplot.com/) to graph data. The graph is shown after the user records a session or selects a session form the history screen.

#### Implementation

To display data on the graph, the application must put the data as x and y arrays into a bundle, then send the bundle to the fragment. The GraphFragment requires a List<Number> to create the graph, but we can only put long[] and double[] into the bundle.
This means that to pass data to the graph, we must turn the List<> into a primitive array manually (since Java can't do it) then turn that back into a List<>.

### History Screen

The history screen is composed of the HistoryFragment, which implements a RecyclerView. HistoryAdapter handles the display and reuse of items in the list. HistoryAdapter uses HistoryHolder to hold data for individual elements.
When an item is clicked, HistoryHolder create the DataAnalysisActivity and passes it its session. This then graphs that particular session.

### Watch

The `wear` module in the project contains the app that goes on the WearOS watch. Currently only the Fossil Gen 5 is supported, however any watch with a PPG sensor can be supported trivially. The algorithm for `wear` app is as follows:

```
1. Wait for the start signal.
2. Begin reading at 30Hz.
3. Send the stored readings as a JSON object.
4. Repeat to step 1.
```

It should be noted that the PPG sensor uses an analog signal of 0-5V that is then digitialized into a floating point number so all numbers are relative to the sensor.

Additionally, the PPG sensor can be set to any frequency range as specified by the driver. However, it was seen in research that as the frequency of the sensor increased, the accuracy decreased and the DC channel of the sensor dropped out (around 50Hz).
This is a hardware limitation and cannot be solved by software. So, a compromise of 30Hz was made to get both frequency and accuracy.

## Installation

Binary files (.apk) were provided with each release of this app. It is recommended to install the latest release.

### Mobile App

Installation for the Android Phone app is easy. Simply download the apk file to your phone (either by USB or downloading it from the phone's web browser) and open it. The phone will automatically open the APK installer and begin installing. You may find that android has a popup regarding installing downloaded apks. It will redirect to a settings page where you can enable the feature for installing downloaded apps. After enabling this, you can reopen the apk and install it.

### WearOS App

#### Prerequisites

Installing the WearOS application is slightly more cumbersome. You'll need a few things:

1. Android Studio downloaded to a computer (optionally you can just download `adb` although this is more advanced).
2. A local router that doesn't block connecting to peers.
3. The `wear-release.apk` file downloaded to your computer.

#### Enable developer settings and connect to ADB

If you have already enabled developer options on the watch and know the watches IP, you can skip this next section.

To enable developer settings, go to Settings->System->About and tap the Build Number 5 times.
A new menu will be available in the main settings page.

Enable WiFi and connect the watch to the same access point (i.e. router) as the debugging computer. Go to
Developer Settings->Enable ADB then tap Debug Over WiFi. After a few moments, the IP address of
the watch will be displayed under Debug Over WiFi.

On the debugging computer, open a command console and type the below command to connect the watch
to the debugger:

```bash
adb connect <ip_of_the_watch>:<port_of_the_watch>
```

The watch will ask for confirmation. Click `OK` or `Always allow`. You may see a message on the command prompt that
authorization failed. As long as the watch asked for confirmation you can ignore this.

#### Installing

Open the folder in file explorer containing your apk. Our goal is to open a command prompt for this folder, so from the File Explorer app click the top bar as if you were going to enter a folder on your hard drive to navigate to, but instead type `cmd`. This will create a new command prompt for that folder.

If you haven't already, connect the watch to `adb` as described above. Then enter `adb -s <ip_of_the_watch>:<port_of_the_watch> install ./wear-release.apk`.

Example:

```cmd
C:\ProjectLimbRescue> adb connect 192.168.0.2:5555
...
C:\ProjectLimbRescue> adb -s 192.168.0.2:5555 install ./wear-release.apk
...
Some success message hopefully.
```
