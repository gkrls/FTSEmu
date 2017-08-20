#!/bin/bash


### CONFIG ###
VERSION=23
NODES=16
C_LOW=0
C_HIGH=0
W=200000
CSV="csv"
DIST=1
STRATEGY=2
BATYPE=1
CI=$DIST
LOG=""
##############

JAR="../TDS-0.1.jar"
NUM_RUNS=$1
OUT_FOLDER=$2
NOW=$(date +"%d-%m-%Y")

if [ "$NUM_RUNS" = "" ]; then
	NUM_RUNS=1
fi

if [ "$OUT_FOLDER" = "" ]; then
	OUT_FOLDER="../results/$NOW"
fi

if [ ! -d "$OUT_FOLDER/$NODES" ]; then
	mkdir -p "$OUT_FOLDER/$NODES" ;
fi

for i in `seq 1 $NUM_RUNS`;
do
	echo -ne "Running test" `expr $i` "of" $NUM_RUNS \\r
	java -jar $JAR -ver $VERSION -n $NODES -c $C_LOW $C_HIGH -w $W -$CSV -dist $DIST \
		-strategy $STRATEGY -batype $BATYPE -ci $CI -folder "$OUT_FOLDER/$NODES"
	sleep 2
done

echo 'Done running' $NUM_TESTS 'tests!'
