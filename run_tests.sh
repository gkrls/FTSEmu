#!/bin/bash
NUM_TESTS=$1
if [ "$NUM_TESTS" = "" ];
then
	echo 'No number of tests specified. Running 1';
fi
echo -ne "Running test 1" of $NUM_TESTS \\r
java -jar TDS-0.1.jar -ver 2 -n 64 -csv -c 12 -f -l 2 -w 200000
sleep 2
NUM_TESTS=`expr $NUM_TESTS - 1`
for i in `seq 1 $NUM_TESTS`;
do
	echo -ne "Running test" `expr $i + 1` "of" $1 \\r
	java -jar TDS-0.1.jar -ver 2 -csv -n 64 -c 12 -l 2 -w 200000
	sleep 2
done 
NUM_TESTS=`expr $NUM_TESTS + 1`
echo 'Done running' $NUM_TESTS 'tests!'
