package com.lab.twelve;

import java.util.ArrayList;
import java.util.List;

/**
 *  The fork class
 */
class Fork {

    /**
     *  The eat method
     */
    public void eat(){
        System.out.println("Don't think, just eat!");
    }
}

/**
 *  The Philosopher class
 */
class Philosopher extends Thread {

    private int id;
    private Fork leftFork;
    private Fork rightFork;

    /**
     *  Constructor
     *
     * @param leftFork
     * @param rightFork
     */
    public Philosopher(int id, Fork leftFork, Fork rightFork){
        this.id = id;
        this.leftFork = leftFork;
        this.rightFork = rightFork;
    }

    /**
     *  The run method
     */
    @Override
    public void run() {
        try {
            synchronized (leftFork){
                System.out.println("Philosopher "+this.id+" picks left fork");
                Thread.sleep(100);
                synchronized (rightFork){
                    System.out.println("Philosopher "+this.id+" picks right fork");
                    Thread.sleep(100);
                    rightFork.eat();
                    System.out.println("Philosopher "+this.id+" keeps right fork down");
                }
                System.out.println("Philosopher "+this.id+" keeps left fork down");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

/**
 *  The class DinnerTable.
 */
public class DinnerTable {

    /**
     *  The main method simulates the Dinning Philosopher problem to create a deadlock.
     *
     * @param args Number of philosophers
     */
    public static void main(String[] args){
        int noOfPhilosophers = Integer.parseInt(args[0]);
        List<Fork> forks = new ArrayList<>();

        //Creating list of Fork objects on the Dinner table
        for(int i = 0; i < noOfPhilosophers; i++){
            forks.add(new Fork());
        }

        //Seating Philosophers on the Dinner table and making them pick up forks, first left then right.
        for(int i = 0; i < noOfPhilosophers; i++){
            //Last fork for last philosopher is the first fork
            if(i == noOfPhilosophers-1){
                new Philosopher(i, forks.get(i), forks.get(0)).start();
            }else{
                //Right fork for each philosopher is the next fork
                new Philosopher(i, forks.get(i), forks.get(i + 1)).start();
            }
        }
    }

}

