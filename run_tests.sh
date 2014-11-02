#!/bin/bash

# Test 1
echo Running DoubleSquare tests
java -cp gen:junit/* org.junit.runner.JUnitCore DoubleSquareTest

# Test 2
echo Running PrimeList tests
java -cp gen:junit/* org.junit.runner.JUnitCore PrimeListTest