package main;

import java.io.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

import board.LevelService;
import logging.ConsoleLogger;
import logging.VerySimpleFormatter;

public class ClientMain {

    private static final Logger LOGGER = ConsoleLogger.getLogger(ClientMain.class.getSimpleName());

    public static void main(String[] args) throws IOException {
        //try {
        ConsoleLogger.logInfo(LOGGER, "Hello from Hell. I am sending this using the error output stream");
        LevelService levelService = new LevelService(System.in);
        ConsoleLogger.logInfo(LOGGER, levelService.toString());
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
