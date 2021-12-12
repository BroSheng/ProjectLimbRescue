# Project Limb Rescue Web

Technical Team:
Sheng Yao,
Yi Chen,
Dominic Nappi,
Christopher Egolf

Sponsors:
Lynne Brophy, MSN, RN-BC, ARPN-CNS, AOCN
Carlo Contreras, MD, FASC

## Project Mission

Gather data on the water content of a limb using non-invasive wearable devices to assist in an early-warning system for patients at risk of lymphedema.

## Application Components

### Data

The application stores reading data in the backend application. In total, there are four classes to store the infomation.

**Device**: A device is a type of smart technology, such as a smartwatch or smart scale, that contains one or more sensors. One ‘device’ entry refers to a type of device, rather than a single particular watch or scale, although it does specify a device placed on a specific limb or combination of limbs.

**Reading**: A reading is a single instance of raw data collected from a type of sensor, on a particular device, about a given limb, during a single session. It numerically represents some output recorded by the sensor.

**Sensor**: A sensor is a device that takes readings of health data. It is situated in devices and may produce readings. One ‘sensor’ entry refers to a type of sensor, rather than a single particular one on a device.

**Session**: A session is a continuous period of data collection begun and ended by user input. It is made up of readings, and may read from multiple sensors and/or multiple devices over the same time period.

There also exist three entities (**DeviceContainsSensor**, **SessionMeasuresSensor** **SessionReadsFromDevice**) that manage the many-to-many relationships between rows in the above tables (for instance, one device may contain many sensor, or one session may read from multiple devices).

### Watch

The `wear` module in the project contains the app that goes on the WearOS watch. Currently only the Fossil Gen 5 is supported, however any watch with a PPG sensor can be supported trivially. The algorithm for `wear` app is as follows:

```
1. Wait for the start signal from backend.
2. Begin reading at 30Hz.
3. Send the stored readings as a JSON object.
4. Repeat to step 1.
```

It should be noted that the PPG sensor uses an analog signal of 0-5V that is then digitialized into a floating point number so all numbers are relative to the sensor.

Additionally, the PPG sensor can be set to any frequency range as specified by the driver. However, it was seen in research that as the frequency of the sensor increased, the accuracy decreased and the DC channel of the sensor dropped out (around 50Hz).
This is a hardware limitation and cannot be solved by software. So, a compromise of 30Hz was made to get both frequency and accuracy.

## Installation

Binary files (.apk) were provided with each release of this app. It is recommended to install the latest release.

### WearOS App

#### Prerequisites

Installing the WearOS application is slightly more cumbersome. You'll need a few things:

1. Android Studio downloaded to a computer (optionally you can just download `adb` although this is more advanced).
2. A local router that doesn't block connecting to peers.
3. The `wear.apk` file downloaded to your computer.

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

If you haven't already, connect the watch to `adb` as described above. Then enter `adb -s <ip_of_the_watch>:<port_of_the_watch> install ./wear.apk`.

Example:

```cmd
C:\ProjectLimbRescue> adb connect 192.168.0.2:5555
...
C:\ProjectLimbRescue> adb -s 192.168.0.2:5555 install ./wear.apk
...
Some success message hopefully.
```
