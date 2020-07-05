package com.lab.twelve;

import java.lang.reflect.Array;
import java.util.*;

/**
 *  The class Storage
 *
 * @author Chaitanya Patel
 * @author Aishwarya Lad
 */

public class Storage<E> implements Storable<E>, Iterable<E>{
//Creating an implementation of our collection framework.
	
    private int length = 0;
    private E[] aArray;

    public Storage() {
        this.aArray =  (E[]) Array.newInstance(Object.class, 0);
    }

    public Storage(int initialCapacity) {
        this.aArray =  (E[]) Array.newInstance(Object.class, initialCapacity);
        this.length = initialCapacity;
    }

    
    /**
     * Returns the element at the specified position in this list.
     *
     * @param  index index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException
     */
    @Override
    public E get(int index){
        if(index<this.length) {
            return aArray[index];
        }else{
            throw new IndexOutOfBoundsException("Array index out of bounds");
        }
    }

    
    /**
     * Replaces the element at the specified position in this list with
     * the specified element.
     *
     * @param index index of the element to replace
     * @param element element to be stored at the specified position
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException 
     */
    @Override
    public void set(int index, E element){
        if(index<this.length) {
            aArray[index] = element;
        }else{
            throw new IndexOutOfBoundsException("Array index out of bounds");
        }
    }
    
    
    /**
     * The size of the ArrayList (the number of elements it contains).
     *
     */
    @Override
    public int size() {
        return length;
    }

    /**
     * Appends the specified element to the end of this list.
     *
     * @param e element to be appended to this list
     * @return {@code true} )
     */
    @Override
    public boolean add(E e) {
        E[] tempArray;
        if (length <= aArray.length - 1){
            aArray[length] = e;
            length++;
        }
        else {
            tempArray = (E[]) Array.newInstance(Object.class, aArray.length + 1);
            for (int index = 0; index < aArray.length; index++){
                tempArray[index] = aArray[index];
            }
            this.aArray = (E[]) Array.newInstance(Object.class, aArray.length + 1);
            this.aArray = tempArray;
            aArray[length] = e;
            length++;
        }
        return true;
    }

    @Override
    public Iterator<E> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<E> {

        int cursor = 0;

        /**
         * Returns {@code true} if the iteration has more elements.
         * 
         *
         */
        @Override
        public boolean hasNext() {
            return cursor < size();
        }

        @Override
        public E next() {
            int i = cursor;
            E next = get(i);
            cursor = i + 1;
            return next;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Storage<?> storage = (Storage<?>) o;
        return length == storage.length &&
                Arrays.equals(aArray, storage.aArray);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(length);
        result = 31 * result + Arrays.hashCode(aArray);
        return result;
    }

    public static void main (String args []) {
        Storage<Integer> l = new Storage<>();
        l.add(6);
        l.add(4);
        System.out.println(l.get(0));
        System.out.println(l.get(1));
        l.set(1, 44);
        System.out.println(l.get(1));
        System.out.println(l.size());

        Storage<Integer> l2 = new Storage<>();
        l2.add(6);
        l2.add(44);
        System.out.println(l2.equals(l));

        Storage<String> l3 = new Storage<String>();
//        l2.add("aa");
//        l2.add("44");
        System.out.println(l2.equals(l));

        System.out.println("Iterator test");
        Iterator<Integer> itr = l.iterator();
        System.out.println(itr.next());
        System.out.println(itr.next());
        Iterator<Integer> itr1 = l.iterator();
        while(itr1.hasNext()){
            System.out.println(itr1.next());
        }
    }
}
