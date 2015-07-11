#README

These projects solve the following coding challenges:
 - https://www.codeeval.com/public_sc/33/
 - https://www.codeeval.com/public_sc/46/

The input must be /strictly/ formatted according to the specifications of the 
exercises: mind especially trailing spaces/empty lines.


## Requirements

1. java, javac version 6 or above and installed in $PATH
2. bash to run the scripts

Build with:
```sh
    ./build.sh
```

Run test with:
```sh
    ./run_tests.sh
```
 
Clean with:
```sh
    ./clean.sh
```

## Comments

Both exercises have been *extremely* optimized. 

1. The first exercises is quite straightforward, so I let the code speak for 
itself. To run an average case example:

	```sh
    java -cp gen it.gilvegliach.DoubleSquare data/double_square1.txt
	```
To run it on the maximum input possible:

    ```sh
    time java -cp gen it.gilvegliach.DoubleSquare data/double_square2.txt
	```
	
2. The second exercise is more sofisticated and all the tricks and bit fiddling are to achieve performance on the brink of the theoretical limit. It is
basically the old good Eratosthenes' sieve with segmentation and 
parallelization: the former to lower the memory limit to O(sqrt(n)), the second one to speed it up. A bounded thread pool has been written to avoid out of memory errors; memory is saved also using a bit array instead of arrays. The time complexity is pseudolinear, that is O(n log(log(n))). To run it in an average case:

	```sh
    java -cp gen it.gilvegliach.PrimeList data/prime_list1.txt
	```
	
To run it on the maximum input possible I *strongly* suggest that you re-build
the project with the output flag set to false (`sieve(n, false)`). This is 
because going up to the order of billion, the I/O becomes a serious bottleneck.
I could not write a variant as the specs are really specific on the
requirements. See the code to know what to comment in and out to re-build the
project. Finally, run the project with:

    ```sh
    time java -cp gen it.gilvegliach.PrimeList data/prime_list2.txt
    ```

Then, this output should corrispond to PI(4294967293), where PI is the
mathematical prime-counting function. (Note the -1) Check the value on
[Wolfram Alpha](http://www.wolframalpha.com/input/?i=PI%284294967293%29).

The total time spent in the method is less than 18 seconds on my Macbook Pro 15'
late 2008 (Core 2 Duo 2.4 Ghz), and about 7 seconds on my work's Macbook Pro 15'
Retina (Core i7 quad-core 2.5 Ghz).