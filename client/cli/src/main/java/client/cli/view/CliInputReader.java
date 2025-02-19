package client.cli.view;

import client.view.InputReader;

import java.util.Scanner;

/**
 * requires some input to the user, via cli/terminal or other input stream reader
 */
public class CliInputReader implements InputReader {

    private final Scanner in;

    CliInputReader() {
        this.in = new Scanner(System.in);
    }

    /**
     * @param message the text to show to the user
     * @return an integer inserted by the user
     */
    @Override
    public int requestInt(String message) {
        System.out.println(System.lineSeparator() + ">>> " + message);
        return in.nextInt();
    }


    @Override
    public String requestString(String message) {
        System.out.println(System.lineSeparator() + ">>> " + message);
        return in.next();
    }


    @Override
    public String requestCommand(String message) {
        return in.nextLine();
    }
}
