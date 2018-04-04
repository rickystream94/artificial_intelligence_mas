package main;

import java.io.*;

import board.LevelService;

public class ClientMain {

    public static void main(String[] args) throws IOException {
        //try {
        System.err.println("Hello from Hell. I am sending this using the error output stream");
        LevelService w = new LevelService(System.in);
        System.err.println(w);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //} catch (Exception e) {
        //	System.err.println(e);
        //}
    }
}
