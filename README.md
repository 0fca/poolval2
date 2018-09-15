# poolval2
Alexis - minimalistic postgres benchmark

# What is this?

Alexis is just a simple CLI-based program for benchamrking postgres database.

# Features

Alexis allows you to:
* do one single test,
* do n tests in row,
* change configuration,
* view average time for all probes taken, where probe is execution of command: standardBench or \s
* using aliases instead of full commands.

# What I used

Alexis is built in pure Java 1.8 using JDBC for postgres. I used Maven as a dependency system. 
Project was created in NetBeans 8.2. Application was tested on real production environment on Cent OS 7 as an host OS and postgres-10(in pgpool).

# Commands
There two ways you can "talk" to Alexis.
There are full names of commands and shortcuts for them also called aliases.
For example: printHelp is a full command and \h or \? or :h are aliases, test them to get more information about commands.

# Easter egg
There is one or two...? Maybe three... whatever, none of them is really dangerous until you have less then 3GBs of RAM onf the system...


# Development

PoolVal2 as it is, does not contain public API or sth like this, but you can always add your own classes with your own logic.
Then just add those three things to make it work:
* add method which body will be using you code in InputController(doesnt matter if public or private as I did it),
* add flag with any name, important is that predefined value of the flag **MUST** be a name of the method in InputController,
* _optional_: Add record to the _aliases_ map in InputController so your logic will be accessible using aliases such as: \h, :h etc.
And its all, it just works.

Any upgrades, additional features, bug fixes and so on will be very appreciated.
