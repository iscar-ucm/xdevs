# Makefile to generate a xdevs static library (.a)

CC = g++
CFLAGS = -std=c++11 -Wall -O3 -static
PREFIX = .

XDEVS_OBJS = ${PREFIX}/core/util/Constants.o \
             ${PREFIX}/core/util/RulesEvaluator.o \
             ${PREFIX}/core/modeling/Event.o \
             ${PREFIX}/core/modeling/Port.o \
             ${PREFIX}/core/modeling/Component.o \
             ${PREFIX}/core/modeling/Atomic.o \
             ${PREFIX}/core/modeling/Coupling.o \
             ${PREFIX}/core/modeling/Coupled.o \
             ${PREFIX}/core/simulation/SimulationClock.o \
             ${PREFIX}/core/simulation/AbstractSimulator.o \
             ${PREFIX}/core/simulation/Simulator.o \
             ${PREFIX}/core/simulation/Coordinator.o

all: ${XDEVS_OBJS}
	ar rcs lib_xdevs.a $^

clean:
	rm -f *.o ${PREFIX}/core/util/*.o ${PREFIX}/core/modeling/*.o ${PREFIX}/core/simulation/*.o

%.o: %.cpp %.h
	${CC} -c ${CFLAGS} $< -o $@


