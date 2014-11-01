#!/bin/bash
#javac -d gen -cp gen:junit/* test/DoubleSquareTest.java 
java -cp gen:junit/* org.junit.runner.JUnitCore DoubleSquareTest

#java -cp gen DoubleSquare data/double_square1.txt 

#java -cp gen DoubleSquare data/double_square_err1.txt 
#java -cp gen DoubleSquare data/double_square_err2.txt 
#java -cp gen DoubleSquare data/double_square_err3.txt 
#java -cp gen DoubleSquare data/empty.txt
