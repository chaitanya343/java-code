package com.lab.twelve; /**
 *  The class NumberCounter
 *
 * @author Chaitanya Patel
 * @author Aishwarya Lad
 */

import java.util.Iterator;
/*
 * Creating an Interface for our collection framework used for storage.
 * 
 * */


public interface Storable<E> {

    public E get(int index);

    public void set(int index, E element);

    public int size();

    public boolean add(E e);

    public Iterator<E> iterator();

    public boolean equals(Object o);

    public int hashCode();
}
