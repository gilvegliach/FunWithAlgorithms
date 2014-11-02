package it.gilvegliach;

import static it.gilvegliach.Utils.checkCondArg;
import static it.gilvegliach.Utils.checkNonNullArg;

import java.util.List;
import java.util.ArrayList;

public class PrimeList {
	public static ArrayList<Long> sieve(long n) {
		checkCondArg(0L <= n && n < 4294967295L, 
			"n must be in [0, 2^32-2]");
		
		ArrayList<Long> primes = new ArrayList<Long>();
		if (n < 3L) return primes;
		
		primes.add(2L);
		if (n < 4L) return primes;
		// Post condition: n in [4, 2^32-2]
		
		// We must sieve the numbers in [1..n-1]:  if n is odd, then n-1 is even
		// and therefore not prime. In this case we can restrict ourselves to
		// [1..n-2]. We set n to the last number we could sieve (inclusive)
		n = (n - 1) - (n & 1);
		// Post condition: n is odd
		
		// Safe cast because n <= 2^32 - 2
		int size = (int)((n + 1) >> 1);
		// Maps:  number --> index
		//          i    --> (i - 1) / 2
		//       2i + 1  <--    i
		BitArray arr = new BitArray(size);
		
		// Start sieving
		for (long i = 3L; i * i <= n; i += 2L) {
			// Skip composite numbers
			if (arr.get((int) ((i - 1) >> 1)) == 1) {
				continue;
		 	}
			for (long j = i * i; j <= n; j += i) {	
				// Skip even numbers
				if ((j & 1) == 0) {
					 continue;
				}
				arr.set((int) ((j - 1) >> 1));
			}						
		}
		
		long count = 1;
		// Gather primes
		for (long i = 3; i <= n; i += 2) {
			if (arr.get((int) ((i - 1) >> 1)) == 0) {
				//primes.add((long)i);
				count++;
			}
		}
		System.out.println("Count: " + count);
		
		return primes;
	}
	
	static class BitArray {
		private final int mSize;
		private final int[] mArr;
		
		BitArray(int size) {
			mSize = size;
			
			// sz = ceil(size / 32)
			// Maps:  index --> (arr-index, bit)
			//          k   --> (k / 32, k % 32)
			//                = (k >> 5, k & 0x1F)
			int sz = (size & 0x1F) == 0 ? 0 : 1;
			sz += size >> 5;
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
		ArrayList<Long> primes = sieve(n);
		//printCompact(primes);
	}
}


