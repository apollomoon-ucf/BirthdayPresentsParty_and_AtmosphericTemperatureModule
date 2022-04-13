// Brian Moon
// COP4520
// PA3 - Problem 2
// AtmosphericTemperatureReadingModule.java

/*
   --- --- --- --- ---
  |                   |
  |   PROBLEM SKETCH  |
  |                   |
   --- --- --- --- --- 
  Next Generation Mars Rover
  w/multi-core CPU and 8 temperature sensors

  Problem: 
      • At the end of every hour, compile a report
        contatining the top 5 highest temperatures,
        the top 5 lowest temperatures, and the 10
        minute interval of time when the largest
        temperature difference was observed.
  Idea: 
      • Store temperature readings in a linked list,
        sorted by "time of reading."
            Data needed for parallel linked list:
                * Temperature
                * Time of reading
  Specs: 
      • Eight sensors === Eight threads
      • Temperature will be a random number between
        -100F and 70F
      • Temperature readings are taken every 1 minute by 8 threads
          * 1 minute === 1 iteration
          * 480 readings per report
      • Record 3 hours of temperature data 
          * 3 hourly reports w/480 data points each
*/

import java.util.Random;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.BrokenBarrierException;

public class AtmosphericTemperatureReadingModule extends Thread {
  // public member variables
  public static Node head;
  public static long startTime;
  public static Long endOfTesting;
  public static int tempRangeMax = 70;
  public static int reportNumber1 = 1;
  public static int tempRangeMin = -100;
  public static int numberOfSensors = 8;
  public static CyclicBarrier reportWriter;
  public static Random random = new Random();
  public static AtomicInteger counter = new AtomicInteger(0); 
  public static int numberOfHoursForCollectingTemperatureData = 3;
  public static AtomicInteger maximumDifference = new AtomicInteger(0);
  public SensorDataList listOfTemperatureReadings = new SensorDataList();
  public static AtomicLong maximumDifferenceEndingTime = new AtomicLong();
  public static AtomicLong maximumDifferenceStartingTime = new AtomicLong();
  public static AtomicLong localMaxDifferenceEndingTime = new AtomicLong(-1);
  public static AtomicLong localMaxDifferenceStartingTime = new AtomicLong(-1);
  public static AtomicInteger localMinTemp = new AtomicInteger(tempRangeMax + 1);
  public static AtomicInteger localMaxTemp = new AtomicInteger(tempRangeMin - 1);
  public static PriorityBlockingQueue<Long> threadIDs = new PriorityBlockingQueue<>(8);
  public static PriorityBlockingQueue<Integer> minHeap = new PriorityBlockingQueue<>(6);
  public static PriorityBlockingQueue<Integer> maxHeap = new PriorityBlockingQueue<>(6, Comparator.reverseOrder());

  // node
  static class Node {
    Long key;
    Node next;
    Long timeOfReading;
    Integer temperatureReading; 
    ReentrantLock lock = new ReentrantLock();
    Node (Integer temperatureReading, Long timeOfReading) {
      this.key = timeOfReading;
      this.timeOfReading = timeOfReading;
      this.temperatureReading = temperatureReading;
    }
  }   

  
  // Sensor Data List
  public class SensorDataList {
    // add temperature data to SensorDataList
    public boolean addTempReading(Integer temperatureReading, Long timeOfReading) {
      long key = timeOfReading;
      while (true) {
        Node pred = head;
        if (pred == null || pred.next == null) return false;
        Node curr = pred.next;
        while (curr.key < key) {
          pred = curr;
          curr = curr.next;
        }
        pred.lock.lock();
        curr.lock.lock();
        try {
          if (validate(pred, curr)) {
            Node node = new Node(temperatureReading, timeOfReading);
            node.next = curr;
            pred.next = node;
            if (!maxHeap.contains(temperatureReading)) maxHeap.add(temperatureReading);
            if (maxHeap.size() > 5) maxHeap.poll();
            if (!minHeap.contains(temperatureReading)) minHeap.add(temperatureReading);
            if (minHeap.size() > 5) minHeap.poll();              
            return true;
          }
        } finally {
          pred.lock.unlock();
          curr.lock.unlock();
        }
      }
    }
    
    // validates method
    public boolean validate(Node pred, Node curr) { 
      Node node = head;
      while (node.key <= pred.key) {
        if (node == pred) {
          return pred.next == curr;
        }
        node = node.next;
      }
      return false;
    }
  }

  // run
  public void run() {
    // run for number of hours required for testing
    for (int i = 0; i < numberOfHoursForCollectingTemperatureData; ++i) {
      // take temp reading on each thread for one hour
      for (long minute = 0; minute < 60; ++minute) {
        Integer randomTemperature = random.nextInt(tempRangeMax - (tempRangeMin - 1)) + tempRangeMin;
        listOfTemperatureReadings.addTempReading(randomTemperature, (minute % 60));   
      }
      // 10ms === 1min
      // 10ms per iteration with 60 iterations = 600ms to simulate 60 real-world-minutes
      // If threads exceed the read time alloted, a faulty sensors error will be thrown, and data will be collected for the next hour
      try {
        reportWriter.await(600, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        return;
      } catch (BrokenBarrierException e) {
        return;
      } catch (TimeoutException e) {
        System.out.println("\n ---- Faulty Sensors Error ---- \n");
        return;
      }      
    }
  }  
  
  // main method
  public static void main(String[] args) {
    // start time
    startTime = System.currentTimeMillis();
    
    // create head and tail for linked list
    head = new Node(Integer.MIN_VALUE, Long.MIN_VALUE);
    head.next = new Node(Integer.MAX_VALUE, Long.MAX_VALUE);
    
    // start the Mars Rover
    System.out.println("\nThe Mars Rover is now collecting data on all " + numberOfSensors + " temperature sensors...");
    System.out.println("Please wait...");
    
    // report writer
    reportWriter = new CyclicBarrier(numberOfSensors, new Runnable() { 
      public void run() { 
        // Write report at end of hour
        System.out.println("\nThe Mars Rover has completed collecting and reporting temperature data.");
        System.out.println("----- Mars Rover Report Number " + (reportNumber1) + " ------");
        System.out.println("Lowest 5 Temperatures:");
        while (maxHeap.size() > 0) {
          System.out.println(maxHeap.poll() + "F");
        }
        System.out.println("Highest 5 Temperatures:");
        while (minHeap.size() > 0) {
          System.out.println(minHeap.poll() + "F");
        }
        // get maximum difference, checking each 10-minute interval
        Node startingNode = head.next;       
        Node readingNode = startingNode;
        int maximumTemperatureDifferenceTimeInterval = 10;
        // find maximum difference in 10-minute interval for report
        while (startingNode.next != null) {
          while (readingNode.timeOfReading < (startingNode.timeOfReading + maximumTemperatureDifferenceTimeInterval) && readingNode.timeOfReading < 60 && readingNode.next != null) {
            // update min and max temps and 
            if (readingNode.temperatureReading > localMaxTemp.get()) {
              localMaxTemp.set(readingNode.temperatureReading);
            }
            if (readingNode.temperatureReading < localMinTemp.get()) {
              localMinTemp.set(readingNode.temperatureReading);
            }   
            readingNode = readingNode.next;      
          }
          // if local difference is greater than maximum difference, update values
          int localDifference = Math.abs(localMaxTemp.get() - localMinTemp.get());
          if (localDifference > maximumDifference.get()) {       
            maximumDifference.set(localDifference);
            maximumDifferenceStartingTime.set(startingNode.timeOfReading);
            maximumDifferenceEndingTime.set(startingNode.timeOfReading + maximumTemperatureDifferenceTimeInterval);
          }       
          // update values
          localMinTemp.set(tempRangeMax + 1);
          localMaxTemp.set(tempRangeMin - 1);
          startingNode = startingNode.next; 
          readingNode = startingNode;
          counter.getAndIncrement();
        }
        System.out.println("Number of successful data readings for hour " + reportNumber1 + ": " + counter.getAndSet(0) + "\n");      
        // print maximum temperature difference results
        System.out.println("Maximum Temperature Difference of " + maximumDifference.get() + "F was recorded in the 10-minute interval of [" + maximumDifferenceStartingTime.get() + ", " + maximumDifferenceEndingTime.get() + "] of hour " + reportNumber1++);        
        // reset values
        maximumDifference.set(0);
        maximumDifferenceEndingTime.set(0);         
        maximumDifferenceStartingTime.set(0);
        head = new Node(Integer.MIN_VALUE, Long.MIN_VALUE);
        head.next = new Node(Integer.MAX_VALUE, Long.MAX_VALUE);   
      }
    });    

    // create atmospheric temperature sensor threads
    AtmosphericTemperatureReadingModule[] atomosphericSensors = new AtmosphericTemperatureReadingModule[numberOfSensors]; 

    // start sensor threads
    for (int sensorNumber = 0; sensorNumber < numberOfSensors; ++sensorNumber) {
      atomosphericSensors[sensorNumber] = new AtmosphericTemperatureReadingModule();
      atomosphericSensors[sensorNumber].start();
    }     
   
    // wait for sensor threads to finish before shutting down the program
    for (int sensorNumber = 0; sensorNumber < numberOfSensors; ++sensorNumber) {
      try {
        atomosphericSensors[sensorNumber].join();
      } catch(Exception e) { e.printStackTrace(); }
    }

    // print stats
    long endTime = System.currentTimeMillis();
    System.out.print("\n" + numberOfHoursForCollectingTemperatureData + " hours of data reports are now available above.");
    System.out.println("\nExecution Time: " + (endTime - startTime) + "ms\n");
  }
}