README


For executable files:

****** How to compile and execute source code?

In command line go the folder src and type: 

javac *.java

This will generate some *.java file
(under the same folder as *.java)

Then type in: java CacheSimulator_start config.txt trace1.txt

#the first parameter following CacheSimulator_start is config file, and after that is trace file, be sure to type in this two parameter, otherwise you will get a error when you run program
The above command will allow you to execute the project 

The class file will be generate in src folder.


****** How to run the program using complied *.class file?

1. Unzip the submission file and open the folder “exec” in folder CacheSimulator.

2. in command line, please type
   java CacheSimulator_start config.txt trace1.txt
#the first parameter following CacheSimulator_start is config file, and after that is trace file, be sure to type in this two parameter, otherwise you will get a error when you run program

3. there are two ways to use debug mode: 
	
	1) in command line, please type
		java CacheSimulator_start config.txt trace1.txt debug
	2) in config file, modify the last parameter of the file, located in the end of the file, to 1, then run command 
		java CacheSimulator_start config.txt trace1.txt
#Warning: if debug mode switch in config file is 1, then though in command line you may not type in debug parameter, but the result will still be under debug mode, please be sure this digital is right, we highly recommend you set this digital to 0, then you could switch debug mode only through command line

#the first parameter after CacheSimulator_start is config file, then next to it is trace file, the last parameter is debug mode switch

4.all the variables needs to be determined in the file config.txt
For format of parameter example in the config.txt:
P = 1
N1 = 2
N2 = 3
K = 2
A1 = 0
A2 = 0
B = 1
D2 = 7
DM = 100
S = 0
DEBUG = 1

For source Code files:

*source code is located in src folder



About Java version

We use java 1.8 so that maybe java 1.8 needs to be installed if compilation of the source code announce error. 