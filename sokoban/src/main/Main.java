package main;

import environment.*;

public class Main {

	public static void main(String[] args) {
		try {
			System.err.println( "Hello from Hell. I am sending this using the error outputstream" );
			World w = new World(System.in);
			System.err.println(w);
		} catch (Exception e) {
			System.err.println(e);
		}
	}
}
