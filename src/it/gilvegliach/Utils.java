package it.gilvegliach;

import java.io.Closeable;
import java.io.IOException;

public class Utils {
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