import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import it.gilvegliach.PrimeList;

public class PrimeListTest {
    final static String dataDir = "src/test/resources/";
    
    @Test(expected = IllegalArgumentException.class)
    public void sieveNegative() {
        PrimeList.sieve(-1, false);
    }
    
    @Test
    public void sieveTwo() {
        long count = PrimeList.sieve(2, false);
        assertEquals(0, count);
    }
    
    @Test
    public void sieveOutputOneHundred() {
        PrintStream out = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        try {
            PrimeList.sieve(100, true);
            assertEquals(
                "2,3,5,7,11,13,17,19,23,29,31,37,41,43,47,53,59,61,67,71,73,79,83,89,97\n",
                baos.toString());
        } finally {
            System.setOut(out);
        }
    }
    
    // Speed test: give it 5 seconds
    @Test(timeout = 5000)
    public void sieveOneHundredThousand() {
        long count = PrimeList.sieve(100000, false);
        // Result found with wolfram alpha, see:
        // http://www.wolframalpha.com/input/?i=PI%2899999%29
        assertEquals(9592, count);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void mainNoArgs() {
        PrimeList.main(new String[] { });
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void mainTooManyArgs() {
        PrimeList.main(new String[] {"foo", "bar"});
    }
    
    @Test
    public void mainAverageCase() {
        PrintStream out = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        try {
            PrimeList.main(new String[] { dataDir + "prime_list1.txt"});
            assertEquals(
                "2,3,5,7\n" +
                "2,3,5,7,11,13,17,19\n" +
                "2,3,5,7,11,13,17,19,23,29,31,37,41,43,47,53,59,61,67,71,73,79,83,89,97\n",
                baos.toString());
        } finally {
            System.setOut(out);
        }
    }
}
