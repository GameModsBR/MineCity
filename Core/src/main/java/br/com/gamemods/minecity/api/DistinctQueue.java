package br.com.gamemods.minecity.api;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractQueue;
import java.util.Iterator;

public class DistinctQueue<E> extends AbstractQueue<E>
{
    private int size;
    private Node current;
    private Node last;

    @NotNull
    @Override
    public Iterator<E> iterator()
    {
        return new Iterator<E>()
        {
            Node last = null;
            Node next = current;

            @Override
            public boolean hasNext()
            {
                return next != null;
            }

            @Override
            public E next()
            {
                E result = next.element;
                next = next.next;
                return result;
            }
        };
    }

    @Override
    public boolean isEmpty()
    {
        return current == null;
    }

    @Override
    public int size()
    {
        return size;
    }

    @Contract("null->fail")
    @Override
    public synchronized boolean offer(E e)
    {
        if(e == null)
            throw new NullPointerException();

        Node search = current;
        while(search != null)
        {
            if(search.element.equals(e))
                return false;
            search = search.next;
        }

        Node add = new Node();
        add.element = e;

        if(last == null)
            current = last = add;
        else
            last = last.next = add;

        size++;

        return true;
    }

    @Override
    public synchronized E poll()
    {
        Node current = this.current;
        if(current == null)
            return null;

        size--;
        if(current.next == null)
            this.current = last = null;
        else
            this.current = current.next;

        return current.element;
    }

    @Override
    public E peek()
    {
        Node current = this.current;
        return current == null? null : current.element;
    }

    private class Node
    {
        E element;
        Node next;
    }
}
