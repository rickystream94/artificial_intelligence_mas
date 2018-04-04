package main;

import java.io.*;

import board.LevelService;

public class Main {

    public static void main(String[] args) throws IOException {
        //try {
        System.err.println("Hello from Hell. I am sending this using the error outputstream");
        LevelService w = new LevelService(System.in);
        System.err.println(w);
        //} catch (Exception e) {
        //	System.err.println(e);
        //}
    }
}
