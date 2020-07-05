package com.algo.first;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.IntStream;

/**
 * @author Chaitanya Patel (cp2263)
 */
public class Stable {

    /**
     * The main function
     *
     * @param args
     */
    public static void main(String[] args) {
        int size = 1;

        try(Scanner sc = new Scanner(new File(args[0]))) {
            PrintStream out=new PrintStream(new FileOutputStream(args[1]));
            while (sc.hasNextLine()) {
                String line = sc.nextLine().strip().trim();
                if(line.isEmpty() || line.startsWith("#")){
                    continue;
                }
                if(Character.isDigit(line.charAt(0))){
                    size = Integer.parseInt(String.valueOf(line.charAt(0)));
                    out.println(size);
                    break;
                }
            }

            int[][] manPref = new int[size][size];
            int[][] womanPref = new int[size][size];

            while (sc.hasNextLine()) {
                String line = sc.nextLine().strip().trim();
                if(line.isEmpty() || line.startsWith("#")){
                    continue;
                }
                String[] lineArray = line.replaceAll("\\s+","-").split("-");
                if (lineArray[0].startsWith("m")){
                    int manIndex = Integer.parseInt(lineArray[0].substring(1))-1;
                    manPref[manIndex] = IntStream.range(1, size + 1)
                            .map(i -> Integer.parseInt(lineArray[i])-1)
                            .toArray();
                }
                if (lineArray[0].startsWith("w")){
                    int womanIndex = Integer.parseInt(lineArray[0].substring(1))-1;
                    womanPref[womanIndex] = IntStream.range(1, size + 1)
                            .map(i -> Integer.parseInt(lineArray[i])-1)
                            .toArray();
                }
                //Printing cleaned file contents
                StringBuilder cleanLine = new StringBuilder();
                for(String l : lineArray) {
                    cleanLine.append(l).append(" ");
                }
                out.println(cleanLine.toString());
            }
            int[] currentMatches = stableMatching(size, manPref, womanPref);
            for (int i=0; i<size; i++){
                out.println("m"+(i+1)+" w"+(currentMatches[i]+1));
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * The stable matching function
     *
     * @param size
     * @param manPref
     * @param womanPref
     * @return
     */
    private static int[] stableMatching(int size, int[][] manPref, int[][] womanPref) {
        List<Integer> allMenBornFree = new ArrayList<>(size);
        for(int s=0; s<size; s++)
            allMenBornFree.add(s);
        List<Integer> freeMen = new ArrayList<>(allMenBornFree);

        int[] next = new int[size];
        for(int s=0; s<size; s++)
            next[s] = manPref[s][0];

        int[] current = new int[size];
        for(int s=0; s<size; s++)
            current[s] = -1;

        while(freeMen.size()>0){
            Integer freeMan = freeMen.get(0);
            int nextWoman = next[freeMan];
            int indexOfNextWomanOnFreeMansPrefList = indexOf(manPref[freeMan], nextWoman);
            int currentManOfThisWoman = current[nextWoman];
            if(currentManOfThisWoman == -1){
                //Engaging this free man and this free woman
                freeMen.remove(freeMan);
                current[nextWoman] = freeMan;
                if(freeMen.size()>0)  //Final FIX! Add condition for if freeMen are zero
                    next[freeMan] = manPref[freeMan][indexOfNextWomanOnFreeMansPrefList+1];
            }else{
                //This woman already has a match
                int[] prefOfThisWoman = womanPref[nextWoman];
                int prefOfCurrentMan = indexOf(prefOfThisWoman,currentManOfThisWoman);
                int prefOfFreeMan = indexOf(prefOfThisWoman,freeMan);
                if(prefOfCurrentMan<prefOfFreeMan){
                    //This woman prefers her current match over the free man
                    next[freeMan] = manPref[freeMan][indexOfNextWomanOnFreeMansPrefList+1];
                }else if(prefOfFreeMan<prefOfCurrentMan){
                    //This woman prefers the free man over her current match
                    freeMen.remove(freeMan);
                    freeMen.add(currentManOfThisWoman);
                    current[nextWoman] = freeMan;
                    next[freeMan] = manPref[freeMan][indexOfNextWomanOnFreeMansPrefList+1];
                }
            }
        }

        return current;
    }

    /**
     * Finds index of element in array
     *
     * @param current
     * @param valueToFindIndexOf
     * @return
     */
    public static int indexOf(int[] current, int valueToFindIndexOf){
        for(int i=0; i<3; i++){
            if(current[i]==valueToFindIndexOf){
                return i;
            }
        }
        return -1;
    }
}
