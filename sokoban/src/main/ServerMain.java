package main;

import server.Runner;
import server.CompetitionRunner;

import java.util.Scanner;

public class ServerMain {

    // Working directory when executing this class must be set to sokoban/out/production/sokoban!
    // This is done to allow remote debugging from IntelliJ
    private static final String outputJar = "../../artifacts/sokoban_jar/sokoban.jar";
    private static final String debugOptions = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,quiet=y,address=5005";
    private static final String vmOptions = "-Xmx4g";
    private static final String clientMainClass = ClientMain.class.getCanonicalName();

    // Server options
    private static final String level = LevelChooser.MAByteMe;
    private static final String millisPerAction = "50";
    private static final String timeout = "3000";

    public static void main(String[] args) throws Exception {
        Scanner in = new Scanner(System.in);
        String jarClientCommand = String.format("java -jar %s", outputJar);
        String debugClientCommand = String.format("java %s %s", debugOptions, clientMainClass);
        String clientCommand;

        // Select proper client command depending on debugging preferences
        System.out.println("Debug? Y/N");
        String answer = in.nextLine();
        if (answer.equalsIgnoreCase("y"))
            clientCommand = String.format(debugClientCommand + " %s", vmOptions);
        else
            clientCommand = String.format(jarClientCommand + " %s", vmOptions);

        System.out.println(String.format("Client command: %s", clientCommand));
        System.out.println("Competition? Y/N");
        if (in.nextLine().equalsIgnoreCase("y")) {
            String[] serverArgs = new String[]{
                    "-d",
                    LevelChooser.competitionPath,
                    "-c",
                    clientCommand
            };
            // Launch server
            CompetitionRunner.main(serverArgs);
        } else {
            String[] serverArgs = new String[]{
                    "-l",
                    level,
                    "-g",
                    millisPerAction,
                    "-t",
                    timeout,
                    "-p",
                    "-c",
                    clientCommand
            };
            // Launch server
            Runner.main(serverArgs);
        }

    }
}
