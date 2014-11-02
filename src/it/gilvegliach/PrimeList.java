package it.gilvegliach;

import static it.gilvegliach.Utils.checkCondArg;
import static it.gilvegliach.Utils.checkNonNullArg;
import static it.gilvegliach.Utils.closeQuietly;

import java.util.List;
import java.util.ArrayList;
import java.util.ArrayDeque;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import java.io.FileReader;
import java.io.BufferedReader;

/** Solves https://www.codeeval.com/public_sc/46/ */
public class PrimeList {
    private static final String SEPARATOR = ",";
    private static final int SEGMENT_SIZE = 1 << 23;   // bit arrays of 1 mb 
    private static final int MAX_TASKS_IN_MEMORY = 10; // 10 mb
    private static final int N_CPUS = Runtime.getRuntime().availableProcessors();
    private static final int POOL_SIZE = N_CPUS > 2 ? N_CPUS - 1 : 2;
    private static  long LARGE_N_THRESHOLD = 1000000;  // found empirically
    
    public static long sieve(long n, final boolean output) {
        checkCondArg(0L <= n && n < 4294967295L, "n must be in [0, 2^32-2]");
        
        // No primes in [0..1]
        if (n <= 2) return 0;
        
        // Only 2 in [0..2]
        if (n == 3) {
            if (output) System.out.println("2");
            return 1;
        }
        // Post condition: n > 3
        
        // We must sieve the numbers in [1..n-1]:  if n is odd, then n-1 is even
        // and therefore not prime. In this case we can restrict ourselves to
        // [1..n-2]. We set n to the last number we could sieve (inclusive)
        n = (n - 1) - (n & 1);
        // Post condition: n > 2, odd, and "inclusive"
        
        // Now we calculate the limit of the first pass of the sieve: on small
        // n's we set limit = n, otherwise we apply a parallelized segmented
        // multistep algorithm with limit = sqrt(n);
        boolean largeN = n >= LARGE_N_THRESHOLD;
        long limit = largeN ? (long) Math.sqrt(n) : n;

        ArrayList<Long> primes = sieveInitial(limit);       
        long count = primes.size();
        
        // Output the initial segment, if need be
        if (output) {
            System.out.print(2);
            for (int i = 0; i < count; i++) {
                System.out.print(SEPARATOR + primes.get(i));
            }
        }
        
        // For large n's we are not yet done: we need to sieve all the remaining
        // segments
        if (largeN) {
            long next = limit + 1;
            next = (next & 1) == 0 ? next + 1 : next;
            count += sieveParallel(primes, next, n, output);
        }
        
        // Print last newline
        if (output) System.out.println();
        
        // Add 1 because 2 is prime
        return count + 1;
    }
    
    /** Produces a list of primes up to <code>limit</code> (inclusive). */
    private static ArrayList<Long> sieveInitial(long limit) {
        // Pre condition: limit <= 2^32 - 2
        int size = (int)((limit + 1) >> 1);
        
        // Map:   number --> index
        //          i    --> (i - 1) / 2
        //       2i + 1  <--    i       
        BitArray arr = new BitArray(size);

        // Sieve
        for (long i = 3L; i * i <= limit; i += 2L) {
            // Skip composite numbers
            if (arr.get((int) ((i - 1) >> 1)) == 1) {
                continue;
            }
            for (long j = i * i; j <= limit; j += i) {  
                // Skip even numbers
                if ((j & 1) == 0) continue;
                arr.set((int) ((j - 1) >> 1));
            }                       
        }
        
        // Gather primes
        ArrayList<Long> primes = new ArrayList<Long>();
        for (long i = 3; i <= limit; i += 2) {
            if (arr.get((int) ((i - 1) >> 1)) == 0) {
                primes.add(i);
            }
        }
        return primes;
    }
    
    /** 
     * Counts primes between <code>lo</code> and <code>hi</code> (inclusive),
     * eventually printing them.
     */
    private static long sieveParallel(ArrayList<Long> primes, 
            long lo, long hi, boolean output) {
        // Pre condition: lo, hi are odd
        long n = hi;
        long count = 0;
    
        ManagedThreadPool<SieveSegmentTask, SieveSegmentTask.Result> pool 
            = new ManagedThreadPool<SieveSegmentTask, 
                SieveSegmentTask.Result>(POOL_SIZE, MAX_TASKS_IN_MEMORY);
        
        // Enqueue all tasks
        // Pre condition: lo is odd
        while (lo <= n) {
            hi = lo + SEGMENT_SIZE - 1;
            hi = (hi & 1) == 0 ? hi + 1 : lo;
            hi = hi <= n ? hi : n;   // hi = min(hi, n)
            // Post condition: lo, hi are odd
        
            // Pre condition: hi - lo + 1 (odd)             
            pool.submit(new SieveSegmentTask(primes, lo, hi));
            lo = hi + 2;
            // Post condition: lo is odd
        }
    
        // Fetch results
        SieveSegmentTask.Result res = null;
        while ((res = pool.take()) != null) {
            BitArray arr = res.array;
            lo = res.low;
            hi = res.high;
        
            // Shouldn't be necessary but it doesn't hurt either
            res.free();
        
            for (long i = lo; i <= hi; i += 2) {
                // Map:  number --> index
                //         i    --> (i - lo) / 2
                if (arr.get((int) ((i - lo) >> 1)) == 0) {
                    count++;
                    if (output) System.out.print(SEPARATOR + i);    
                }
            }
        }
        
        pool.shutdown();
        return count;
    }
    
    /** 
     * Sieves a segment between <code>lo</code> and <code>hi</code> (inclusive),
     * using initial primes <code>primes</code> and returning a 
     * {@link BitArray} representing the the new primes in the segment.
     */
    private static BitArray sieveSegment(ArrayList<Long> primes, 
            long lo, long hi) {
        // Pre condition: lo, hi are odd
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
    
    /** 
     * Quick implementation of a thread pool. Use it this way:
     * 1. enqueue all the tasks first from a single thread
     * 2. poll all the results from the same thread: it will block
     *    if not available
     * 3. the tasks will run on a thread pool but only mMaxTaskInMem
     *    will be held in memory. It we submit too many tasks, the 
     *    execution will wait until we retrieve enough results by take().
     */
    static class ManagedThreadPool<T extends Callable<R>, R> {
        private final ExecutorService mPool;
        private final int mMaxTasksInMem;
        private final ArrayDeque<T> mQueuingTasks = new ArrayDeque<T>();
        private final ArrayDeque<Future<R>> mPendingResults =
             new ArrayDeque<Future<R>>();
        
        public ManagedThreadPool(int nThread, int maxTasksInMemory){
            mPool = Executors.newFixedThreadPool(nThread);
            mMaxTasksInMem = maxTasksInMemory;
        }
        
        public synchronized void submit(T task) {
            mQueuingTasks.offer(task);
            submitNext();
        }
        
        private synchronized void submitNext() {
            if (mPool.isShutdown() || mQueuingTasks.isEmpty()) return;
            if (mPendingResults.size() >= mMaxTasksInMem) return;

            final T task = mQueuingTasks.poll();
            Future<R> future = mPool.submit(
                new Callable<R>() {
                    @Override
                    public R call() {
                        R res = null;
                        try {
                            res = task.call();
                            submitNext();
                        } catch(Exception e) {
                            // Not the best policy; only for this exercise
                            System.err.println(
                                "Exception in call()... shutting down");
                            e.printStackTrace();
                            shutdown();
                        }
                        return res;
                    }
            });
            mPendingResults.offer(future);
        }
        
        public R take() {
            Future<R> future = null;
            
            synchronized (this) {
                if (mPendingResults.isEmpty()){
                    return null;
                }
                future = mPendingResults.poll();
            }
            try {
                // !!! Can block! Must be called without the lock held, 
                // otherwise there is a high chance of deadlock
                R result = future.get();
                submitNext();
                return result;
            } catch (Exception e){
                System.err.println("Exception in take()... shutting down");
                e.printStackTrace();
                shutdown();
                return null;
            }
        }
        
        public synchronized void shutdown() {
            mPool.shutdownNow();
        }
    }
    
    /** Wraps a call to {@link #sieveSegment(ArrayList<long>, long, long)} */
    static class SieveSegmentTask implements Callable<SieveSegmentTask.Result> {
        final ArrayList<Long> mPrimes;
        final long mLow;
        final long mHigh;
        
        SieveSegmentTask(ArrayList<Long> primes, long lo, long hi) {
            mPrimes = primes;
            mLow = lo;
            mHigh = hi;
        }
        
        public Result call() {
            BitArray arr = sieveSegment(mPrimes, mLow, mHigh);
            return new Result(arr, mLow, mHigh);
        }
        
        @Override
        public String toString() {
            return "SieveSegmentedTask{ mPrimes.get(0) = " 
                + mPrimes.get(0) + ", mLow = " + mLow 
                + ", mHigh = " + mHigh + "}";
        }
        
        static class Result {
            BitArray array;
            final long low;
            final long high;
            
            Result(BitArray arr, long lo, long hi) {
                array = arr;
                low = lo;
                high = hi;
            }
            
            void free() {
                array = null;
            }
        }
    }
    
    /**
     * Represents an array of booleans in a memory-efficient way. At 
     * construction, all booleans are set to false. 
     */
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
    
    // Parsing here is strict and exception handling is really generic. For a
    // more linient approach check out DoubleSquare.java
    public static void main(String args[]) {
        checkCondArg(args.length == 1, 
            "args[0] must be a pathname and the only argument");
        
        String line = null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(args[0]));
            while ((line = br.readLine()) != null) {
                long n = Long.parseLong(line);
                long count = sieve(n, true);
                // Uncomment this and set the above parameter to false for a
                // speed test. See also README.txt
                // System.out.println("count: " + count);
            }
        } catch (Exception e) {
            System.err.println(
                "File must exist and be formatted according"+
                    " to the specs. Aborting...");
            e.printStackTrace(System.err);
        } finally {
            closeQuietly(br);
        }
    }
}

