CC = g++
CFLAGS = -std=c++11 -Wall -O3 -static
PREFIX = ../..

XDEVS_OBJS = ${PREFIX}/core/util/Constants.o \
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

# EDITABLE ------------------- OBJS
EXAMPLE_OBJS = Job.o Generator.o Processor.o Transducer.o
 
# EDITABLE ------------------- MAIN TARGET NAME
efp: main_efp.o Ef.o Efp.o ${XDEVS_OBJS} $(EXAMPLE_OBJS)
	${CC} ${CFLAGS} $^ -o $@
	
gpt: main_gpt.o Gpt.o ${XDEVS_OBJS} $(EXAMPLE_OBJS)
	${CC} ${CFLAGS} $^ -o $@

# EDITABLE ------------------- CLEAN
clean:
	rm -f *.o ${PREFIX}/core/util/*.o ${PREFIX}/core/modeling/*.o ${PREFIX}/core/simulation/*.o

%.o: %.cpp
	${CC} -c ${CFLAGS} $< -o $@

# EDITABLE ------------------- EXAMPLE DEPENDENCIES
Job.o: Job.cpp Job.h
Generator.o: Generator.cpp Generator.h
Processor.o: Processor.cpp Processor.h
Transducer.o: Transducer.cpp Transducer.h
Gpt.o: Gpt.cpp Gpt.h
Ef.o: Ef.cpp Ef.h
Efp.o: Efp.cpp Efp.h
main_gpt.o: main_gpt.cpp
main_efp.o: main_efp.cpp

# XDEVS DEPENDENCIES
${PREFIX}/core/util/Constants.o: ${PREFIX}/core/util/Constants.cpp ${PREFIX}/core/util/Constants.h
${PREFIX}/core/modeling/Event.o: ${PREFIX}/core/modeling/Event.cpp ${PREFIX}/core/modeling/Event.h
${PREFIX}/core/modeling/Port.o: ${PREFIX}/core/modeling/Port.cpp ${PREFIX}/core/modeling/Port.h
${PREFIX}/core/modeling/Component.o: ${PREFIX}/core/modeling/Component.cpp ${PREFIX}/core/modeling/Component.h
${PREFIX}/core/modeling/Atomic.o: ${PREFIX}/core/modeling/Atomic.cpp ${PREFIX}/core/modeling/Atomic.h
${PREFIX}/core/modeling/Coupling.o: ${PREFIX}/core/modeling/Coupling.cpp ${PREFIX}/core/modeling/Coupling.h
${PREFIX}/core/modeling/Coupled.o: ${PREFIX}/core/modeling/Coupled.cpp ${PREFIX}/core/modeling/Coupled.h
${PREFIX}/core/simulation/SimulationClock.o: ${PREFIX}/core/simulation/SimulationClock.cpp ${PREFIX}/core/simulation/SimulationClock.h
${PREFIX}/core/simulation/AbstractSimulator.o: ${PREFIX}/core/simulation/AbstractSimulator.cpp ${PREFIX}/core/simulation/AbstractSimulator.h
${PREFIX}/core/simulation/Simulator.o: ${PREFIX}/core/simulation/Simulator.cpp ${PREFIX}/core/simulation/Simulator.h
${PREFIX}/core/simulation/Coordinator.o: ${PREFIX}/core/simulation/Coordinator.cpp ${PREFIX}/core/simulation/Coordinator.h
