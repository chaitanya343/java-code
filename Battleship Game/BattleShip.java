package com.lab.four;

import java.io.File;
import java.util.Scanner;

/**
 * This program emulates the game BattleShip.
 * Users chooses a file which has a battlefield as per te rules of the game
 *
 * @author      Chaitanya Patel
 * @author      Aishwarya Lad
 */
public class BattleShip {

    /**
     * The main function
     * The game assumes the file contains a square of string indicated by integers between 0 and 4
     * The numbers signify the following:
     *  0 - Water
     *  1 - Boat Sunk
     *  2 - Boat (Horizontal)
     *  3 - Boat (Vertical)
     *
     *  The game asks for input of target coordinates from user till all boats are sunk
     */
    public static void main( String[] args ) {
        int fieldLength = 0;

        //Taking file name from user
        String inputPath = "/Users/chaitanya/Code/Java/Week4/";
        String fileName = "battlefield.txt";
        System.out.println("Enter file name of battlefield :");
        Scanner fileNameInput = new Scanner(System.in);
        if(fileNameInput.hasNextLine()){
            String temp = fileNameInput.nextLine();
            if(!temp.isEmpty() || !temp.contains(".txt")) {
                fileName = temp;
            }else{
                System.out.println("Using default file name: "+fileName);
            }
        }

        try{
            //Reading input file of the battlefield
            Scanner scanner = new Scanner(new File(inputPath+fileName));
            if (!scanner.hasNextLine()) {
                System.out.println("File is empty!");
                return;
            }
            String firstRow = scanner.nextLine();
            //Taking the value of fieldLength dynamically based on the input file
            fieldLength = firstRow.length();
            int[][] battlefield = new int[fieldLength][fieldLength];
            //Creating a 2 D integer array of the battlefield
            for (int j = 0; j < fieldLength; j++) {
                battlefield[0][j] = Integer.parseInt(String.valueOf(firstRow.charAt(j)));
            }
            for (int i = 1; i < fieldLength; i++) {
                if (scanner.hasNextLine()) {
                    String row = scanner.nextLine();
                    for (int j = 0; j < fieldLength; j++) {
                        battlefield[i][j] = Integer.parseInt(String.valueOf(row.charAt(j)));
                    }
                }
            }
            System.out.println("Battlefield formation");
            printBattlefield(battlefield, fieldLength);

            Scanner input = new Scanner(System.in);

            while(boatExists(battlefield, fieldLength)) {
                //Some boat/s exist in the battlefield which haven't sunk yet. Game not over yet.
                System.out.print("Enter row target coordinate: ");
                int row = input.nextInt();

                System.out.print("Enter column target coordinate: ");
                int column = input.nextInt();

                System.out.println("Target: (" + row + "," + column + ")");

                int targetPositionValue = battlefield[row][column];
                if (targetPositionValue == 2) {
                    //Boat is horizontal
                    int leftCounter = -1;
                    int rightCounter = -1;
                    do {
                        leftCounter++;
                        if (column - leftCounter - 1 < 0)
                            break;
                    }
                    while (battlefield[row][column - leftCounter - 1] == targetPositionValue);

                    do {
                        rightCounter++;
                        if (column + rightCounter + 1 > fieldLength - 1)
                            break;
                    }
                    while (battlefield[row][column + rightCounter + 1] == targetPositionValue);
                    //The boat length is calculated but first searching for the number of cells with the same value on the
                    // left and the right and adding the cell that was hit itself
                    int boatLength = leftCounter + rightCounter + 1;

                    //If boatLength is even and either of the counter are 0,
                    // i.e. the point is a edge of even boat, so we sink that point
                    // i.e. set it to 1 to convert even boat to odd boat
                    if (boatLength % 2 == 0 && (leftCounter == 0 || rightCounter == 0)) {
                        System.out.println("Even Edge Hit!");
                        battlefield[row][column] = 1;
                    }
                    //If boatLength is odd and both the counter are equal that means we have hit the middle of an odd length boat,
                    // hence we sink the entire boat i.e. set it to 1
                    if (boatLength % 2 != 0 && leftCounter == rightCounter) {
                        System.out.println("Odd Middle Hit!");
                        for (int k = 0; k < leftCounter * 2 + 1; k++) {
                            battlefield[row][(column - leftCounter) + k] = 1;
                        }
                    }
                    //If both the counters are unequal and non zero that means we have hit the boat neither in the middle,
                    // nor in the edge hence we display that boat is hit, but it doesn't sink
                    if (leftCounter != rightCounter && leftCounter != 0 && rightCounter !=0 ) {
                        System.out.println("Hit the boat body!");
                    }
                } else if (targetPositionValue == 3) {
                    //Boat is vertical
                    int upCounter = -1;
                    int downCounter = -1;
                    do {
                        upCounter++;
                        if (row - upCounter - 1 < 0)
                            break;
                    }
                    while (battlefield[row - upCounter - 1][column] == targetPositionValue);

                    do {
                        downCounter++;
                        if (row + downCounter + 1 > fieldLength - 1)
                            break;
                    }
                    while (battlefield[row+ downCounter + 1][column] == targetPositionValue);

                    //The boat length is calculated but first searching for the number of cells with the same value on the
                    // top and bottom and adding the cell that was hit itself
                    int boatLength = upCounter + downCounter + 1;

                    //If boatLength is even and either of the counter are 0,
                    // i.e. the point is a edge of even boat, so we sink that point
                    // i.e. set it to 1 to convert even boat to odd boat
                    if (boatLength % 2 == 0 && (upCounter == 0 || downCounter == 0)) {
                        System.out.println("Even Edge Hit!");
                        battlefield[row][column] = 1;
                    }
                    //If boatLength is odd and both the counter are equal that means we have hit the middle of an odd length boat,
                    // hence we sink the entire boat i.e. set it to 1
                    if (boatLength % 2 != 0 && upCounter == downCounter) {
                        System.out.println("Odd Middle Hit!");
                        for (int k = 0; k < upCounter * 2 + 1; k++) {
                            battlefield[row - upCounter + k][(column)] = 1;
                        }
                    }
                    //If both the counters are unequal and non zero that means we have hit the boat neither in the middle,
                    // nor in the edge hence we display that boat is hit, but it doesn't sink
                    if (upCounter != downCounter && upCounter != 0 && downCounter !=0 ) {
                        System.out.println("Hit the boat body!");
                    }
                }
            }
            System.out.println("Winner winner chicken dinner!");
            printBattlefield(battlefield, fieldLength);

            fileNameInput.close();
            input.close();
            scanner.close();
        }catch(Exception e){
            System.out.println("Unexpected Error!");
            e.printStackTrace();
        }
    }

    /**
     * Checks if any boats are remaining in battlefield returns true if any boat
     * exists in the battlefield which is not sunk yet and false otherwise
     *
     * @param    battlefield           The 2 D array of integers
     * @param    fieldLength       Size of the field
     * @return   boolean           Boolean flag if any boat exists or not
     */
    private static boolean boatExists(int[][] battlefield, int fieldLength) {
        for (int i = 0; i < fieldLength; i++) {
            for (int j = 0; j < fieldLength; j++) {
                if (battlefield[i][j] > 1)
                    return true;
            }
        }
        return false;
    }

    /**
     * Prints the entire battlefield
     *
     * @param    battlefield           The 2 D array of integers
     * @param    fieldLength       Size of the field
     */
    private static void printBattlefield(int[][] battlefield, int fieldLength) {
        for (int i = 0; i < fieldLength; i++) {
            for (int j = 0; j < fieldLength; j++) {
                System.out.print(battlefield[i][j] + " ");
            }
            System.out.print("\n");
        }
    }
}
