package main;

import java.io.*;
import java.util.logging.Logger;

import architecture.ClientManager;
import board.BoardReader;
import logging.ConsoleLogger;

public class ClientMain {

    private static final Logger LOGGER = ConsoleLogger.getLogger(ClientMain.class.getSimpleName());

    public static void main(String[] args) throws IOException {
        ConsoleLogger.logInfo(LOGGER, "Launching Sokoban Client Manager");
        new ClientManager().run();
    }
}
