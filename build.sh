#!/bin/bash
SRC=src/it/gilvegliach
echo Building projects
javac -d gen ${SRC}/Utils.java
javac -d gen -cp gen ${SRC}/DoubleSquare.java

echo Building tests
javac -d gen -cp gen:junit/* test/DoubleSquareTest.java