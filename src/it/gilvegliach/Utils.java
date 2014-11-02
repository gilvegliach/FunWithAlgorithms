package it.gilvegliach;

import java.io.Closeable;
import java.io.IOException;

public class Utils {
    public static <T> void checkNonNullArg(T arg, String mess) {
        if (arg == null) {
            throw new IllegalArgumentException(mess);
        }
    }
    
    public static <T> void checkCondArg(boolean condition, String mess) {
        if (!condition) {
            throw new IllegalArgumentException(mess);
        }
    }
    
    
    /** Closes an array of Closeables gobbling exceptions */
    public static void closeQuietly(Closeable... closeables) {
        if (closeables == null) return;
        for (Closeable c : closeables) {
            if (c != null) {
                try {
                    c.close();
                } catch (IOException ignored) {
                    
                }
            }
        }
    }
}