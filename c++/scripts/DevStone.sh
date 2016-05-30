#!/bin/bash

SIMULATOR="xDEVS++"
WORKDIR="/home/jlrisco/${SIMULATOR}"
PLATFORM=$1

for BENCH in "HO" ; do
	COMANDO="$WORKDIR/DevStoneBenchmark.sh ${BENCH} ${PLATFORM}"
	qsub -N ${SIMULATOR}${BENCH} -l q=${PLATFORM}.q -o ${WORKDIR}/${SIMULATOR}.${BENCH}.${PLATFORM}.out -e ${WORKDIR}/${SIMULATOR}.${BENCH}.${PLATFORM}.err $COMANDO
done

