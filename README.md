# RowerPlus

This app is part of a larger project that includes an MCU with its firmware available [here](https://github.com/paolo-projects/rowerplus-stm32)

This app has to be connected through an USB-OTG cable to the MCU that has been flashed with the RowerPlus firmware.
The MCU then has to be wired to the ergometer sensors and will start to send data to the app. Review the [repo](https://github.com/paolo-projects/rowerplus-stm32/blob/main/README.md) README for more info.
The app will display the parameters computed from the data sent by the mcu and record them.

## The app

This android app displays the data from a modified ergometer where the sensors have been installed, wired to the stm32 mcu and the custom firmware flashed onto it.
The purpose of this app is to convert a cheap ergometer with little or wrong parameters displayed to a tool capable of outputting convincing values. In addition to that,
the app currently allows real-time recording of the data and later reviewing it through the screens. The data can also be exported to excel spreadsheet.

## Connecting to the STM32 Board

The phone has to be connected to the stm32 mcu through the builtin StLink USB Serial interface with an USB OTG adapter. Edit the VID/PID accordingly if you have a
different device than mine. The device will start receiving the data and displaying it straight away.

## Heart Rate chest strap

The app can additionally be connected to an Heart Rate chest strap through the Bluetooth Low-Energy protocol (BLE). It should potentially recognize and read 
the values from most of the BLE products out there, but I only tested it with mine so I don't really know.

The app will show the HR BPM values and record them as well for later review. By entering your age in the app settings the app will show which [HR zones](https://www.polar.com/blog/running-heart-rate-zones-basics/) (1, 2, 3, 4, 5)
you've been training in and relative percentages when opening the workout summary.

## License

This software is licensed under GNU GPLv3. Check the LICENSE file for more info
