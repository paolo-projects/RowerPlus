# RowerPlus

This app is part of a larger project that includes an MCU with its firmware available [here](https://github.com/paolo-projects/rowerplus_mcu)

This app has to be connected through an USB-OTG cable to the MCU that has been flashed with the RowerPlus firmware.
The MCU then has to be wired to the ergometer sensors and will start to send data to the app.
The app will display the parameters computed from the data sent by the mcu and record them.

## What is this?

I made the MCU firmware and the app to be wired to an ergometer that relies on reed switches.

Reed switches are magnetic switches that are open when no magnetic field is close, while they are
closes when a magnetic field is close and strong enough.

They're the most simple ergometers but they are indeed useful to detect the individual strokes and
some parameters related to them. For instance, my ergometer has two reel switches and a certain number
of magnets linked to the string that I pull when rowing. When I pull, switch 1 closes and shortly after switch 2
closes too. Then they open again following the same order and so on until the string is released.
They now close with opposite order, and then open again. The MCU is so able to distinguish if I'm pulling
or releasing.

```
 --->>>> DIRECTION THE STRING IS PULLED --->>>>
--------------------------------------
     XXX          XXX          XXX <----- MAGNETS
---------------------------------------
                 ___  ___
                 | |  | |
                 | |  | |
                  ^    ^
                  |    |
            Switch 1  Switch 2

```

The MCU records additional data that is the time for the "pull part" of the stroke and the number of
magnets that passed by the switches. Through the time and the number of magnets (that is proportional to the length)
you can get a value that is proportional to the speed the string is pulled.