package cz.matejkripner;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Represents the program - code, data and pointer. Provides method {@link #run(InputStream, OutputStream)} which
 * interprets the code and use given input and output streams to get and give information as the code specifies.
 * Note that any errors will be logged into the standard error output.
 * <p>
 * The interpreted code has to be written in Brainfuck (see <a href="http://brainfuck.tk">brainfuck.tk</a>).
 *
 * @author Matej Kripner <kripnermatej@gmail.com>; google.com/+MatejKripner
 */
public class Program {

    private static final int MIN_DATA = 100; // most programs will be OK with this
    private static final int MAX_DATA = 30_000;

    private final String code;
    private short[] data;
    private int pointer;

    public Program(String code) {
        this.code = code;
    }

    // badly long, but simple and easy-to-get method
    public void run(InputStream reader, OutputStream output) throws IOException {
        pointer = 0;
        LinkedList<Integer> openedBrackets = new LinkedList<>();
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            switch (c) {
                case '>':
                    pointer++;
                    break;
                case '<':
                    pointer--;
                    break;
                case '+':
                    check(pointer);
                    data[pointer]++;
                    if (data[pointer] == 256) data[pointer] = 0;
                    break;
                case '-':
                    check(pointer);
                    data[pointer]--;
                    if (data[pointer] == -1) data[pointer] = 255;
                    break;
                case '.':
                    check(pointer);
                    output.write(data[pointer]);
                    break;
                case ',':
                    check(pointer);
                    do {
                        data[pointer] = (short) (reader.read() % 256);
                    }
                    while (data[pointer] == 13); // ignores the Windows carriage return on the lines end
                    if (data[pointer] == -1) data[pointer] = 0; // end of the file is interpreted as 0
                    break;
                case '[':
                    if (i < code.length() - 1 && code.charAt(i + 1) == ']') Main.error("empty loop");
                    check(pointer);
                    if (data[pointer] == 0) { // skip to ']'
                        // find enclosing ']'
                        int opened = 1;
                        for (i++; i < code.length() && opened > 0; i++) {
                            if (code.charAt(i) == ']') opened--;
                            else if (code.charAt(i) == '[') opened++;
                        }
                        if (opened > 0) { // enclosing ']' not found
                            Main.error(opened + " opened bracket" + (opened == 1 ? "" : "s"));
                        }
                    } else openedBrackets.add(i); // continue the execution
                    break;
                case ']':
                    check(pointer);
                    if (openedBrackets.isEmpty()) Main.error("missing '['");
                    if (data[pointer] != 0) i = openedBrackets.getLast(); // jump to the right '['
                    else openedBrackets.removeLast(); // remove the '[' and continue execution
                    break;
            }
        }
        if (!openedBrackets.isEmpty())
            Main.error(openedBrackets.size() + " opened bracket" + (openedBrackets.size() == 1 ? "" : "s"));
        output.flush();
    }

    // check if a pointer is valid and eventually extend the data array
    private void check(int index) {
        if (index >= MAX_DATA || index < 0) {
            Main.error("program overflowed its memory");
        } else if (index >= MIN_DATA) {
            if (data == null) data = new short[MAX_DATA];
            else if (data.length < MAX_DATA) extendData();
        } else if (data == null) data = new short[MIN_DATA];
    }

    private void extendData() {
        short[] newData = new short[MAX_DATA];
        System.arraycopy(data, 0, newData, 0, data.length);
        data = newData;
    }
}