package org.notaris;

import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

public class KVServerTest {

//    @Test
//    public static void main(String[] args) {
//        // Create the Runnable objects
//        Runnable task1 = new MyTask1();
//        Runnable task2 = new MyTask2();
//        Runnable task3 = new MyTask3();
//        Runnable task4 = new MyTask4();
//
//        // Create the Thread objects
//        Thread thread1 = new Thread(task1);
//        Thread thread2 = new Thread(task2);
//        Thread thread3 = new Thread(task3);
//        Thread thread4 = new Thread(task4);
//
//        // Start the threads
//        thread1.start();
//        thread2.start();
//        thread3.start();
//        thread4.start();
//    }
}

class MyTask1 implements Runnable {
    public void run() {
        String[] args = {"-a", "127.0.0.1", "-p", "9997"};
        KVServer server1 = new KVServer();
        try {
            server1.main(args);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

class MyTask2 implements Runnable {
    public void run() {
        String[] args = {"-a", "127.0.0.1", "-p", "9998"};
        KVServer server1 = new KVServer();
        try {
            server1.main(args);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

class MyTask3 implements Runnable {
    public void run() {
        String[] args = {"-a", "127.0.0.1", "-p", "9999"};
        KVServer server1 = new KVServer();
        try {
            server1.main(args);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

class MyTask4 implements Runnable {
    public void run() {
        String[] args = {"-a", "127.0.0.1", "-p", "10000"};
        KVServer server1 = new KVServer();
        try {
            server1.main(args);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}