// Brian Moon
// COP4520
// PA3 - Problem 1
// TheBirthdayPresentsParty.java

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.LinkedBlockingQueue;


public class TheBirthdayPresentsParty extends Thread {
  public static Node head;
  public static int numServants = 4;
  public static int numPresents = 500000;
  public static Random random = new Random();
  public PresentList listOfPresents = new PresentList();
  public static LinkedBlockingQueue<Integer> bagOfPresents;
  private AtomicBoolean minotaurWantsMeToCheckForPresent = new AtomicBoolean(false);
  public static AtomicIntegerArray unorderedBagOfPresents = new AtomicIntegerArray(numPresents);

  
  // node
  static class Node {
    Integer presentID;
    int key;
    Node next;
    ReentrantLock lock = new ReentrantLock();
    Node (Integer presentID) {
      this.presentID = presentID;
      this.key = presentID.hashCode();
    }
  }   
 
  // servants grab presents from bag until its empty
  public void run() {
    // get random present from unordered bag
    while (bagOfPresents.size() > 0) {
      Integer randomPresent = bagOfPresents.poll();
      if (randomPresent != null) {
        listOfPresents.addPresent(randomPresent);   
        // Minotaur randomly asks workers to check if a present exists
        if (minotaurWantsMeToCheckForPresent.compareAndSet(true, false)) listOfPresents.containsPresent(random.nextInt(numPresents));
        // workers remove presents from list of presents
        listOfPresents.removeFirstPresentFromSortedList();
        // activate listOfPresents.removePresent(randomPresent); to remove any present from the list
      }
      
    }
  }  
  
  public class PresentList {
    // add present to linked list
    public boolean addPresent(Integer presentNumber) {
      int key = presentNumber.hashCode();
      while (true) {
        Node pred = head;
        Node curr = pred.next;
        while (curr.key < key) {
          pred = curr;
          curr = curr.next;
        }
        pred.lock.lock();
        curr.lock.lock();
        try {
          if (validate(pred, curr)) {
            if (curr.key == key) {
              return false;
            } else {
              Node node = new Node(presentNumber);
              node.next = curr;
              pred.next = node;
              // System.out.println("Added " + presentNumber);
              return true;
            }
          }
        } finally {
          pred.lock.unlock();
          curr.lock.unlock();
        }
      }
    }

    // remove the first present from linked list
    public boolean removeFirstPresentFromSortedList() {
      while (true) {
        Node pred = head;
        Node curr = pred.next;
        pred.lock.lock();
        curr.lock.lock();
        try {
          if (validate(pred, curr)) {
            if (curr.next != null) {
              // System.out.println("Thank You Guest Number " + curr.presentID + " ! :)");
              pred.next = curr.next;
              return true;
            } else {
              return false;
            }
          }
        } finally {
          pred.lock.unlock();
          curr.lock.unlock();
        }
      } 
    }    

    // remove present from anywhere in the linked list -- if needed
    public boolean removePresent(Integer presentID) {
      int key = presentID.hashCode();
      while (true) {
        Node pred = head;
        Node curr = pred.next;
        while (curr.key < key) {
          pred = curr;
          curr = curr.next;
        }
        pred.lock.lock();
        curr.lock.lock();
        try {
          if (validate(pred, curr)) {
            if (curr.key == key) {
              pred.next = curr.next;
              // System.out.println("Thank You Guest Number " + presentID + " ! :)");
              return true;
            } else {
              return false;
            }
          }
        } finally {
          pred.lock.unlock();
          curr.lock.unlock();
        }
      }
    }

    // check if gift is present in linked list
    public boolean containsPresent(Integer presentID) {
      // System.out.println("Thread checking for Minotaur " + currentThread().getId());
      int key = presentID.hashCode();
      while (true) {
        Node pred = head;
        Node curr = pred.next;
        while (curr.key < key) {
          pred = curr;
          curr = curr.next;
        }
        pred.lock.lock();
        curr.lock.lock();
        try {
          if (validate(pred, curr)) {
            // System.out.println("Contains " + presentID);
            return (curr.key == key);
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
  // main method
  public static void main(String[] args) {
    // start time
    long startTime = System.currentTimeMillis();
    System.out.println("\nThe Minotaur Birthday Presents Party Has Begun!!\n");
    // create head and tail for linked list
    head = new Node(Integer.MIN_VALUE);
    head.next = new Node(Integer.MAX_VALUE);

    // create unordered bag of presents for workers to select from
    ArrayList<Integer> tempBag = new ArrayList<Integer>();
    for (int i = 0; i < numPresents; ++i) {
      tempBag.add(i);
    }
    Collections.shuffle(tempBag);
    bagOfPresents = new LinkedBlockingQueue<>(tempBag);

    // create servant threads
    TheBirthdayPresentsParty[] minotaursServants = new TheBirthdayPresentsParty[numServants];
    for (int servantNumber = 0; servantNumber < numServants; ++servantNumber) {
      minotaursServants[servantNumber] = new TheBirthdayPresentsParty();
      minotaursServants[servantNumber].start();
    }

    // Minotaur randomly asks workers to look for a present in the list
    Integer randomWorker = random.nextInt(numServants);
    while (bagOfPresents.size() > 0) {
      minotaursServants[randomWorker].minotaurWantsMeToCheckForPresent.set(true);
      randomWorker = random.nextInt(numServants);
    }

    // wait for servants/threads to finish their work
    for (int servantNumber = 0; servantNumber < numServants; ++servantNumber) {
      try {
        minotaursServants[servantNumber].join();
      } catch(Exception e) { e.printStackTrace(); }
    }

    Node current = head.next;
    int numPresentsRemaining = 0;
    while (current.next != null) {
      System.out.println(current.key);
      current = current.next;
      numPresentsRemaining++;
    }
    
    // print results
    long endTime = System.currentTimeMillis();
    System.out.println("Number of presents added: " + numPresents);
    System.out.println("Number of 'Thank You' cards left to write: " + numPresentsRemaining + "\n");
    System.out.println("Execution Time: " + (endTime - startTime) + "ms");
  }
}