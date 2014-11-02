package it.gilvegliach;

import static it.gilvegliach.Utils.checkCondArg;
import static it.gilvegliach.Utils.checkNonNullArg;

import java.util.List;
import java.util.ArrayList;

public class PrimeList {
	private static final String SEPARATOR = ",";
	private static final int SEGMENT_SIZE = 
		1 << 23; // bit arrays of 1 mb 
	private static final int N_CPUS = 
		Runtime.getRuntime().availableProcessors();
	
	public static long sieve(long n, boolean output) {
		checkCondArg(0L <= n && n < 4294967295L, 
			"n must be in [0, 2^32-2]");
		
		ArrayList<Long> primes = new ArrayList<Long>();
		// TODO:if (n < 3L) return primes;
		
		///primes.add(2L);
		// TODO:if (n < 4L) return primes;
		// Post condition: n in [4, 2^32-2]
		
		
		
		// We must sieve the numbers in [1..n-1]:  if n is odd, then n-1 is even
		// and therefore not prime. In this case we can restrict ourselves to
		// [1..n-2]. We set n to the last number we could sieve (inclusive)
		n = (n - 1) - (n & 1);
		// Post condition: n is odd
		
		// Now we calculate the limit of the first pass of the sieve: on small
		// n's we set limit = n, otherwise we apply a parallelized segmented
		// multistep algorithm with limit = sqrt(n);
		boolean largeN = true;
		long limit = largeN ? (long) Math.sqrt(n) : n;
		
		// Safe cast because n <= 2^32 - 2
		int size = (int)((n + 1) >> 1);
		// Maps:  number --> index
		//          i    --> (i - 1) / 2
		//       2i + 1  <--    i
		BitArray arr = new BitArray(size);
		
		// Start sieving
		for (long i = 3L; i * i <= limit; i += 2L) {
			// Skip composite numbers
			if (arr.get((int) ((i - 1) >> 1)) == 1) {
				continue;
		 	}
			for (long j = i * i; j <= limit; j += i) {	
				// Skip even numbers
				if ((j & 1) == 0) {
					 continue;
				}
				arr.set((int) ((j - 1) >> 1));
			}						
		}
		
		primes = new ArrayList<Long>();
		// Gather primes
		for (long i = 3; i <= limit; i += 2) {
			if (arr.get((int) ((i - 1) >> 1)) == 0) {
				primes.add(i);
			}
		}
		
		// Output the initial segment, if need be
		if (output) {
			for (long i = 0; i < size; i++) {
				System.out.print(i);
				if (i < size - 1) {
					System.out.print(SEPARATOR);
				}
			}
			System.out.println();
	    }
		
		long count = primes.size();
		// For large n's we are not yet done: we need to sieve all the remaining
		// segments
		if (largeN) {			
			// TODO: parallelize
			
			// Set to to be the next odd number after the last hi
			long lo = limit + 1;
			lo = (lo & 1) == 0 ? lo + 1 : lo;
			
			// Pre condition: lo is odd
			while (lo <= n) {
				long hi = lo + SEGMENT_SIZE - 1;
				hi = (hi & 1) == 0 ? hi + 1 : lo;
				hi = hi <= n ? hi : n;   // hi = min(hi, n)
				// Post condition: lo, hi are odd
				
				// size = hi - lo + 1 (odd)
				arr = sieveSegment(primes, lo, hi);
				for (long i = lo; i <= hi; i += 2) {
					// Map:  number --> index
					//         i    --> (i - lo) / 2
					if (arr.get((int) ((i - lo) >> 1)) == 0) {
						count++;
						// TODO: check
						if (output) {
							System.out.print("" + i + SEPARATOR);
						}
					}
				}
				lo = hi + 2;
				// Post condition: lo is odd
			}
		}
		
		// Add 1 because 2 is prime
		return count + 1;
	}
	
	// Pre condition: lo, hi are odd
	private static BitArray sieveSegment(ArrayList<Long> primes, 
			long lo, long hi) {
		BitArray seg = new BitArray((int) (hi - lo + 1));  
		int len = primes.size();
		
		for (int i = 0; i < len; i++) {
			// Pre condition: p is odd (prime)
			long p = primes.get(i);
			long r = lo % p;
			long first = (r == 0) ? lo : lo - r + p;
			first = (first & 1) == 0 ? first + p : first;
			// Post condition: first is odd
			
			for (long j = first; j <= hi; j += p) {
				if ((j & 1) == 0) {
					 continue;
				}
				seg.set((int) ((j - lo) >> 1));
			}				
		}
		return seg;
	}
	
	static class BitArray {
		private final int mSize;
		private final int[] mArr;
		
		BitArray(int size) {
			mSize = size;
			
			// sz = ceil(size / 32)
			int sz = (size & 0x1F) == 0 ? 0 : 1;
			sz += size >> 5;
			
			// Maps:  index --> (arr-index, bit)
			//          k   --> (k / 32, k % 32)
			//                = (k >> 5, k & 0x1F)
			mArr = new int[sz];
		}
		
		int get(int i) {
			return (mArr[i >> 5] >> (i & 0x1F))  &  1;
		}
		
		void set(int i) {
			mArr[i >> 5]  |=  (1 << (i & 0x1F));
		}
		
		void clear(int i) {
			mArr[i >> 5]  &=  ~(i << (i & 0x1F));
		}
		
		int size() {
			return mSize;
		}
	}
	
	public static <T> void printCompact(List<T> list) {
	    checkNonNullArg(list, "list must not be null");
		
		int size = list.size();
		for (int i = 0; i < size; i++) {
			System.out.print(list.get(i));
			if (i < size - 1) {
				System.out.print(",");
			}
		}
		System.out.println();
	}
	
	public static void main(String args[]) {
		long n = Long.parseLong(args[0]);
		long count = sieve(n, false);
		System.out.println("Count: " + count);
	}
}


