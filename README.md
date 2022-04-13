# The Birthday Presents Party And Atmospheric Temperature Module For Mars Rover

## Program Info

Programming Assignment 3

The Birthday Presents Party And Atmospheric Temperature Module For Mars Rover

Brian Moon

COP4520

UCF

## Instructions

Build The Birthday Presents Party Program:

> javac TheBirthdayPresentsParty.java

Run Program And Output To File:

> java TheBirthdayPresentsParty > TheBirthdayPresentsParty.txt

Build Atmospheric Temperature Module Program:

> javac AtmosphericTemperatureReadingModule.java

Run Program And Output To File:

> java AtmosphericTemperatureReadingModule > AtmosphericTemperatureReadingModule.txt

## The Birthday Presents Party - Program Summary

For The Birthday Presents Party, we want to simulate the process of 4 workers randomly selecting presents from an unordered bag of gifts, adding these presents to a sorted list, and then remove items as 'Thank You' cards are written to each guest. The Minotaur will also randomly call on workers to check if a present with a certain tag number (ID) is in the list or not. Each worker is represented by a thread that is performing any of the above operations.

## The Birthday Presents Party - Program Evaluation

> Efficiency: On an M1, 8-Core CPU, the program runs in ~343ms. For the efficiency of the program, similar results have been produced for both the sequential and parallel versions of the program which may be due to contention on the random contains call. During testing, it was shown that if containsPresent() is called from main, a significant increase in speed is seen, but this contains call would happening from the main thread and not one of the worker thread which does not correctly represent the Minotaur requesting a worker to check if a present is currently located in the list. Therefore, more testing will need to be done in attempt to see gains in efficiency with the current concurrent linked list implementation.

> Correctness: The concurrent linked list used in my program is known as the optimistic list, and it locks two nodes to ensure correctness is maintained during add and removal operations. For the Birthday Presents Party program, several testing methods were used to ensure that the correct gifts were being added to the list, removed from the list, checked for in the list, and also that each thread was performing the task that it was expected to be performing at any given time. To carry out these tests, I logged the results of each add and removal to ensure that the list was properly being modified. I then printed out test lists, checking for unexpected results. I also logged threadIDs as they would enter a section to ensure that multiple threads were regularly entering a method.

## Atmospheric Temperature Module - Program Summary

Welcome to the Atomospheric Temperature Module Program! For this program we wanted to simulate an atmospheric temperature module on a next generation Mars Rover, equipeed with a multi-core CPU and 8 temperature sensors. Each of these temperature sensors are represented by a running thread. For any number of hours, the Atmospheric Temperature Module program will take 8 temperature readings (1 on each temp sensor) every minute, and add these reading to a list that is sorted by time. At the end of each hour, the module will print out a report that contains the 5 lowest and highest temperatures recordded for that hour, as well at the maximum temperature difference observed during any 10-minute interval in that hour. Due to the module not being connected to actual hardware sensors, the temperature readings are randomly generated in the range of -100F to 70F.

## Atmospheric Temperature Module - Program Evaluation

> Efficiency: On an M1, 8-Core CPU, the program runs in ~38ms for 3 hours of temperature data. Testing the efficiency gains with this program are difficult to the fact that each thread represents a temperature sensor reading and logging it's results. Therefore, testing with 1 thread, or 1 sensor, produces less data and will run faster. Having tested the program on different types of CPUs, intel i5 6th gen, i7 8th gen, and M1, I have seen respectable speeds on all platforms.

> Correctness: To store all the temperature data and the time associated with temperature, I used a concurrent linked list known as the optimistic list, storing the required data in each node. This list is sorted by time as the key. The optimisitc list ensures correctness by, in simple terms, locking two nodes during add and removal operations. For highest and lowest temperatures a min heap and max heap were used, respectively, with a size less than 5 to ensure that space and time are considered as we want these operations to be completed in less than a minute. Minutes were simulated using each iteration with a 10ms max as 1 real world minutes, meaning the threads must finish in their alloted time or a faulty sensors error is thrown before moving onto the next data report readings. For the Atmospheric Temperature Module program, several testing methods were used to ensure that temperature readings and their associated times were correctly being added and removed to/from the list, and that the correct data was being saved for the report. To carry out these tests, I logged the results of each add and removal to ensure that the list was properly being modified. I then printed out test lists, checking for unexpected results. Expected output can be found in AtmosphericTemperatureReadingModule.txt
