package com.lab.twelve;

import java.io.*;
import java.util.*;

/**
 *  The class CountNumberInFile
 *
 * @author Chaitanya Patel
 * @author Aishwarya Lad
 */
public class CountNumberInFile2 {

	public static void main(String[] args) {
		//Reading from command line the user given parameters
		int totalNoOfThreads = Integer.parseInt(args[0]);
		String directory = args[1];
		String current="";
		try {
			current = new File( "." ).getCanonicalPath();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		String absPath = new File(directory).getAbsolutePath();
		try(BufferedReader br = new BufferedReader(new FileReader(new File(absPath)));) {
			int buffer = 0;
			//For larger threads initial buffer size to be read and divided is bigger
			if(totalNoOfThreads > 50) {
				buffer = 10000000;
			}else {
				buffer =  1000000;
			}
			char[] theChars = new char[buffer];
			int charsRead;
			/***---------------------***/
			Storage<Integer> totalNumberCounts = new Storage<>();
			/***---------------------***/
			for(int i = 0; i < 10; i++)
				totalNumberCounts.add(0);
			int step = 0;
			while((charsRead = br.read(theChars, 0, theChars.length)) != -1) {

				NumberCounter2[] nCounter = new NumberCounter2[totalNoOfThreads];
				int start = 0;
				int end = charsRead/totalNoOfThreads;
				for(int threadCount = 0; threadCount < totalNoOfThreads; threadCount++ ) {
					//Based on thread count, reading file from an offset(start) to end position.
					nCounter[threadCount] = new NumberCounter2(Arrays.copyOfRange(theChars,start,end));
					start = end;
					end = end + charsRead/totalNoOfThreads;
					nCounter[threadCount].start();

					//System.out.println(start +" : " +end);
				}

				//the main thread waits for all the threads to finish before it terminates.
				for(int threadCount = 0; threadCount < totalNoOfThreads; threadCount++ ) {
					nCounter[threadCount].join();
				}

				//All the threads update their counter array into one final array one by one (synchronously)
				//to avoid concurrent resource access.
				synchronized (totalNumberCounts) {
					//for(int threadCount = 0; threadCount < totalNoOfThreads; threadCount++ )
					for(int threadCount = 0; threadCount < totalNoOfThreads; threadCount++ ) {
						//for(int index = 0; index < totalNumberCounts.size(); index++) {
						Iterator<Integer> iter= totalNumberCounts.iterator();
						int counter = 0;
						while (iter.hasNext()) {
							Integer oldCount = iter.next();
							int newCount = nCounter[threadCount].numberCounts.get(counter);
							totalNumberCounts.set(counter, oldCount + newCount);
							counter++;
						}
					}
				}
			}
			printResult(totalNumberCounts);

		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage() + " : "+ e.getStackTrace());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println(e.getMessage() + " : "+ e.getStackTrace());
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.out.println(e.getMessage() + " : "+ e.getStackTrace());
			e.printStackTrace();
		}catch (Exception e) {
			System.out.println(e.getMessage() + " : "+ e.getStackTrace());
			e.printStackTrace();
		}
	}


	/**
	 *  Prints the result
	 *
	 * @param totalNumberCounts int array
	 */
	private static void printResult(Storage totalNumberCounts) {
		//prints the final count of digits in pi
		System.out.println("Digit:"+"  "+"Count");
		for (int i = 0; i <= 9; i++){
			if((int) totalNumberCounts.get(i) > 0)
				System.out.println(i+"\t"+ totalNumberCounts.get(i));
		}
		//System.out.println(Arrays.toString(totalNumberCounts));
	}

}
