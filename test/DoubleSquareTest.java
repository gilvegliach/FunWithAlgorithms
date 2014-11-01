import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import it.gilvegliach.DoubleSquare;

public class DoubleSquareTest {
    final static String dataDir = "test/data/";
    
    @Test(expected = IllegalArgumentException.class)
    public void countDecompositionsNegative() {
        DoubleSquare.countDecompositions(-1);
    }
    
    @Test
    public void countDecompositionsZero() {
        int count = DoubleSquare.countDecompositions(0);
        assertEquals(1, count);  // 0 = 0^2 + 0^2
    }
    
    @Test
    public void countDecompositionsTwentyFive() {
        int count = DoubleSquare.countDecompositions(25);
        assertEquals(2, count); // 4^2 + 3^2, 5^2
    }
    
    // Speed test: give it 10 seconds
    @Test(timeout = 10000)
    public void countDecompositionsMaxInt() {
        DoubleSquare.countDecompositions(Integer.MAX_VALUE);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void mainNoArgs() {
        DoubleSquare.main(new String[] { });
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void mainTooManyArgs() {
        DoubleSquare.main(new String[] {"foo", "bar"});
    }
    
    @Test
    public void mainAverageCase() {
        PrintStream out = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        try {
            DoubleSquare.main(new String[] { dataDir + "double_square1.txt"});
            assertEquals(
                "1\n" +
                "2\n" +
                "0\n" +
                "1\n" +
                "1\n", baos.toString());
        } finally {
            System.setOut(out);
        }
    }
}
