package org.example;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Log {
    public static int tab = 0;
    public static int logLevel = 10;
    public static PrintWriter logFile;

    public static void out(String message) {
        Log.out(1, message);
    }

    public static void out(Integer msgLevel, String message) {
        if (logLevel >= msgLevel) {
            for (int i = 0; i < tab; i++) {
                System.out.print("    ");
                logFile.print("    ");
            }
            System.out.println(message);
            logFile.println(message);
        }
    }

    public static void outNB(String message) {
        Log.outNB(4, message);
    }

    public static void outNB(Integer msgLevel, String message) {
        if (logLevel >= msgLevel) {
            for (int i = 0; i < tab; i++) {
                System.out.print("    ");
                logFile.print("    ");
            }
            System.out.print(message);
            logFile.print("    ");
        }
    }

    public static void setLevel(Integer level) {
        logLevel = level;
    }

    public static void br() {
        System.out.println("");
        logFile.println("");
    }

    public static void inc() {
        tab++;
    }

    public static void dec() {
        tab--;
    }

    public static void reset() {
        tab = 0;
    }

    public static void openLog(String dir) {
        try {
            logFile = new PrintWriter(dir);
        } catch (FileNotFoundException ex) {

        }
    }

    public static void closeLog() {
        logFile.close();
    }
}
