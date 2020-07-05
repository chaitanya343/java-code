package com.lab.twelve;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *  The class NumberCounter
 *
 * @author Chaitanya Patel
 * @author Aishwarya Lad
 */

public class NumberCounter2 extends Thread{
	/**
	 *  The class Number Counter Thread class.
	 *  Assumptions: This class is tested using classCountNumberInFile class.
	 *
	 *
	 **/
	/***---------------------***/
	Storage<Integer> numberCounts = new Storage<>();
	/***---------------------***/

	char[] theChars = new char[20];

	/**
	 * The constructor
	 *
	 */
	public NumberCounter2(char [] arr) {
		this.theChars = arr;
	}

	/**
	 * Default constructor
	 *
	 *
	 */
	public NumberCounter2() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * The overridden run method, counts no of digits and stores in numberCount array.
	 * Each thread has an individual numberCount array
	 */
	@Override
	public void run() {
		for(int i = 0; i < 10; i++)
			numberCounts.add(0);
		for(int i = 0;i < theChars.length; i++) {
			if(Character.isDigit(theChars[i])) {
				Integer value = numberCounts.get(theChars[i] - '0');
				numberCounts.set(theChars[i] - '0', value+1);
			}
		}
	}

}



