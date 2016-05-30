#!/bin/bash

SIMULATOR="xDEVS++"
WORKDIR="/home/jlrisco/${SIMULATOR}"
#WORKDIR="."

benchmarkName=$1
PLATFORM=$2

preparationTime="0.0"
generatorPeriod="1"
intDelayTime="0.0"
extDelayTime="0.0"
flatten="false"
numTrials=10

loggerPath="${WORKDIR}/${SIMULATOR}.${benchmarkName}.${PLATFORM}.log"
errorPath="${WORKDIR}/${SIMULATOR}.${benchmarkName}.${PLATFORM}.txt"
resultsPath="${WORKDIR}/${SIMULATOR}.${benchmarkName}.${PLATFORM}.results"

if [ -f $errorPath ]; then
	rm $errorPath
fi
if [ -f $resultsPath ]; then
	rm $resultsPath
fi

step=100	
if [ "$benchmarkName" == "LI" ] ; then
	maxWidth=1502;
	maxDepth=1501;
elif [ "$benchmarkName" == "HI" ] || [ "$benchmarkName" == "HO" ] ; then
	maxWidth=1102;
	maxDepth=1101;
else
	maxWidth=10;
	maxDepth=10;
	step=1;
fi


for numTrial in $(seq 1 $numTrials)
do
    for width in $(seq 2 $step $maxWidth)
	do
		for depth in $(seq 1 $step $maxDepth)
		do
			if [ -f $loggerPath ]; then
				rm $loggerPath
			fi
			/usr/bin/time -v $WORKDIR/DevStone -b $benchmarkName -w $width -d $depth -m 1 >> $loggerPath 2>> $errorPath				
			dnt=$(tail -1 ${loggerPath} | cut -d ',' -f 19 | tr -d '[[:space:]]')
			dxt=$(tail -1 ${loggerPath} | cut -d ',' -f 22 | tr -d '[[:space:]]')
			nev=$(tail -1 ${loggerPath} | cut -d ',' -f 25 | tr -d '[[:space:]]')
			tim=$(tail -1 ${loggerPath} | cut -d ',' -f 29 | tr -d '[[:space:]]')
			mem=$(tail -1 ${loggerPath} | cut -d ',' -f 32 | tr -d '[[:space:]]') 
			#mem=$(cat ${errorPath} | grep "Maximum resident" | cut -d ' ' -f 6)
			echo "${numTrial};1;${width};${depth};${dnt};${dxt};${nev};${tim};${mem}"
			echo "${numTrial};1;${width};${depth};${dnt};${dxt};${nev};${tim};${mem}" >> $resultsPath					
		done
	done
done


