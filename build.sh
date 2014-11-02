#!/bin/bash
SRC=src/it/gilvegliach
TEST=test

echo Building projects
javac -d gen ${SRC}/Utils.java
javac -d gen -cp gen ${SRC}/DoubleSquare.java
javac -d gen -cp gen ${SRC}/PrimeList.java

echo Building tests
javac -d gen -cp gen:junit/* ${TEST}/DoubleSquareTest.java
javac -d gen -cp gen:junit/* ${TEST}/PrimeListTest.java