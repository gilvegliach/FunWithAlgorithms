package it.gilvegliach;

import static it.gilvegliach.Utils.checkCondArg;
import static it.gilvegliach.Utils.closeQuietly;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;

/** Solves https://www.codeeval.com/public_sc/33/ */
public class DoubleSquare {
    /** 
     * Counts in how many ways <code>n</code> can be
     * written as a sum of two squares. The forms
     * <code>sˆ2 + t^2</code> and <code>t^2 + s^2</code>
     * are counted only once. 
     *  
     * Runs <code>sqrt(n / 2) + 1</code> times 
     * <code>Math.sqrt()</code> and uses <code>O(1)</code>
     * memory. 
     */
    public static int countDecompositions(int n) {
        checkCondArg(n >= 0, "n must be >= 0");
        
        // Let n = s^2 + t^2, then:
        //    s <= sqrt(n / 2)     <=>
        //    s^2 <= n / 2         <=>
        //    n - s^2 >= n - n / 2 <=>
        //    t >= sqrt(n / 2)
        // Therefore is enough to count the decompositions
        // up to sqrt(n / 2).
        int count = 0;
        int k = (int) Math.sqrt(n / 2.0);
        for (int t = 0; t <= k; t++) {
            int q = n - t * t;
            int s = (int) Math.sqrt(q);
            if (s * s == q) {    // q is a perfect square
                count++;
            }
        }
        return count;
    }
    
    public static void main(String[] args) {
        checkCondArg(args.length == 1, 
            "args[0] must be a pathname and the only argument");
        
        int lineNumber = 1;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(args[0]));
            String line = br.readLine();
            int n = Integer.parseInt(line);
            checkCondArg(1 <= n && n <= 100, "n must be in [1, 100]");
            lineNumber++;
            
            while (n-- > 0 && (line = br.readLine()) != null) {
                int k = Integer.parseInt(line);
                // k < 2147483647 checked in parseInt
                checkCondArg(k >= 0, "x must be in [0, 2147483647]");
                int count = countDecompositions(k);
                lineNumber++;
                System.out.println(count);
            }
        } catch (NumberFormatException e1) {
            System.err.println("Line " + lineNumber + 
                ": integer cannot be parsed");
            e1.printStackTrace(System.err);
        } catch (IllegalArgumentException e2) {
            System.err.println("Line " + lineNumber + 
                ": n must be in [1, 100], x in [0, 2147483647]");
            e2.printStackTrace(System.err);
        } catch (FileNotFoundException e3) {
            System.err.println("File not found, was: " + args[0]);
            e3.printStackTrace(System.err);
        } catch (IOException e4) {
            System.err.println("Error reading file");
            e4.printStackTrace(System.err);
        } finally {
            closeQuietly(br);
        }
    }
}