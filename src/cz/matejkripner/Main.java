package cz.matejkripner;

import java.io.*;

/**
 * Main class which gets required information from the user and starts the {@link Program} class.
 *
 * @author Matej Kripner <kripnermatej@gmail.com>; google.com/+MatejKripner
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("BRAINFUCK INTERPRET");
        System.out.println("author: Matej Kripner <kripnermatej@gmail.com>; please see google.com/+MatejKripner\n");
        try {
            new Main().launch(args);
        } catch (IOException e) {
            error("cannot read from file: " + e.getMessage());
        }
    }

    public void launch(String[] args) throws IOException {
        if (args.length < 2)
            error("Specify the source code file, the input file and the output file(optional) in that order");
        InputStream input = new FileInputStream(args[1]);
        PrintStream output = (args.length > 2) ? new PrintStream(new FileOutputStream(args[2]), false, "UTF-8") : System.out;
        Program program = new Program(readFile(args[0]));
        System.out.println("Program run\n------------------------------------");
        program.run(input, output);

        input.close();
        output.close();
    }

    private String readFile(String name) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader input = new BufferedReader(new FileReader(name))) {
            input.lines().forEach(content::append);
        }
        return content.toString();
    }

    public static void error(String msg) {
        System.err.println("ERROR: " + msg);
        System.exit(-1);
    }
}