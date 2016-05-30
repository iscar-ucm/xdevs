#include <ctime>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <math.h>
#include <sys/resource.h>
#include <iostream>
#include <chrono>
#include "../../core/util/Constants.h"
#include "../../core/simulation/Coordinator.h"
#include "DevStoneGenerator.h"
#include "DevStoneCoupledHO.h"

#define BENCH_LI      0
#define BENCH_HI      1
#define BENCH_HO      2

const char* bench[]={"LI", "HI", "HO", "HOmod", "HOmem", "Trivial"};

void usage(char* name) {
  fprintf(stderr, "Usage: %s [-w width] [-d depth] [-m MaxEvents] [-b {HI | HO | HOmod | HOmem | Trivial}]\nwith w>=2, d>=2, m>=1\n",
	  name);
}

/* Number of external Events */

/* HOmod */
int W(int w, int i) {
  return ((w-i)>0?w-i:0);
}

int K(int w, int l) {
  if (l == 1) return 1;
  return K(w, l-1) + W(w, 1);
}

int P(int w, int l, int j) {
  if (j < 1 || j > K(w,l)) return 0;
  if (j == 1 && l == 1) return 1;
  int p=0;
  for (int i=1 ; i <= w; i++)
    p += P(w,  l-1, j);
  return (w-1)*p;
}
  
long int HOmod_lim(int w, int d) {
  //int i, j, l, c;
  
  long int n=1;
  for (int l=1; l <= d-1; l++)
    for (int c=1; c <= K(w,l)+w-1; c++)
      for (int i=1; i <= w; i++)
	n += (W(w,1) + W(w,i))*P(w, l, c-i+1);
  
  return n;
}

/* HOmem */

long int HOmem_lim(int w, int d) {
  int l;
  long int n=1;
  for (l=1; l <= d-1; l++)
    n+=w*pow(w-1,2*l-1);
  
  return n;
}


int main(int argc, char *argv[]) {
	// Common parameters
	double preparationTime = 0.0;
	double period = 1.0;
	//long maxEvents = 6000;
	long maxEvents = 1;
	double intDelayTime = 0;
	double extDelayTime = 0;
	struct rusage resources;
	char* strbenchmark = NULL;
	int benchmark = BENCH_HI;
	int width = 5, depth = 4;
	int opt;
	int n_events = 0;
	
	// TODO
	// Evaluate parameteres: benchmark, width and depth
	while ((opt = getopt(argc, argv, "w:d:b:m:")) != -1) {
	  switch (opt) {
	    case 'w':
	      width = atoi(optarg);
	      break;
	    case 'd':
	      depth = atoi(optarg);
	      break;
	    case 'b':
	      strbenchmark = optarg;
	      break;
	    case 'm':
	      maxEvents = atoi(optarg);
	      break;
	    default: /* '?' */
	      usage(argv[0]);
	      exit(EXIT_FAILURE);
	  }
	}
	
	if (strbenchmark == NULL) {
		width = 2;
		depth = 2;
	  benchmark = BENCH_HO;
	} else if (strncmp(strbenchmark, "LI", 2) == 0) {
	  benchmark = BENCH_LI;
	} else if (strncmp(strbenchmark, "HI", 2) == 0) {
	  benchmark = BENCH_HI;
	} else if (strncmp(strbenchmark, "HO", 2) == 0) {
	  benchmark = BENCH_HO;
	}
	if ((width <= 0) || (depth <= 0) || (maxEvents <= 0)) {
	  usage(argv[0]);
	  exit(EXIT_FAILURE);
	}
	
	Coupled framework("DevStoneHO");
	DevStoneGenerator generator("Generator", preparationTime, period, 1);
	framework.addComponent(&generator);
	Coupled* coupledStone = 0;


	switch (benchmark) {

	/*********************************************************************************************/
	// LI
	case BENCH_LI:
		break;
	case BENCH_HI:
		break;
	case BENCH_HO:
		coupledStone = new DevStoneCoupledHO("C", width, depth, preparationTime, intDelayTime, extDelayTime);
		framework.addComponent(coupledStone);
		framework.addCoupling(&generator, &generator.oOut, coupledStone, &((DevStoneCoupledHO*)coupledStone)->iIn);
		framework.addCoupling(&generator, &generator.oOut, coupledStone, &((DevStoneCoupledHO*)coupledStone)->iInAux);
		break;
	}

	//struct timespec ts_start1, ts_start2, ts_end;
	//	time_t start = clock();
	auto ts_start1 = std::chrono::steady_clock::now();
	Coordinator coordinator(&framework);
	coordinator.initialize();
	//	time_t start = clock();
	auto ts_start2 = std::chrono::steady_clock::now();
	coordinator.simulate(std::numeric_limits<double>::infinity());
	auto ts_end = std::chrono::steady_clock::now();
	coordinator.exit();
	delete coupledStone;
	//	time_t end = clock();
	//	double time = (double)(end-start)/CLOCKS_PER_SEC;
	double time1 = ((ts_end - ts_start1).count()) * std::chrono::steady_clock::period::num / static_cast<double>(std::chrono::steady_clock::period::den);
	double time2 = ((ts_end - ts_start2).count()) * std::chrono::steady_clock::period::num / static_cast<double>(std::chrono::steady_clock::period::den);

	if (getrusage(RUSAGE_SELF, &resources)) {
		perror ("rusage");
	}


	//cout << "MEM: " << resources.ru_utime.tv_sec << "." << resources.ru_utime.tv_usec << ", " << resources.ru_maxrss << endl;

	// 	std::cout << "Execution time (PreparationTime, Period, MaxEvents, Width, Depth, IntDelayTime, ExtDelatTime) = (" << preparationTime << ", " << period << ", " << maxEvents << ", " << width << ", " << depth << ", " << intDelayTime << ", " << extDelayTime << ") = " << time << std::endl;

	//     cout << "STATS:";
	// 	cout << "PreparationTime = " << preparationTime << std::endl;
	// 	cout << "Period = " << period << std::endl;
	// 	cout << "MaxEvents = " << maxEvents << std::endl;
	// 	cout << "Width = " << width << std::endl;
	// 	cout << "Depth = " << depth << std::endl;
	// 	cout << "IntDelayTime = " << intDelayTime << std::endl;
	// 	cout << "ExtDelatTime = " << extDelayTime << std::endl;
	// 	cout << "Num delta_int = " << DevStoneAtomic::NUM_DELT_INTS << " [" << maxEvents*((width-1)*(depth-1)+1) << "]" << std::endl;
	// 	cout << "Num delta_ext = " << DevStoneAtomic::NUM_DELT_EXTS << " [" << maxEvents*((width-1)*(depth-1)+1) << "]" << std::endl;
	// 	cout << "Num event_ext = " << DevStoneAtomic::NUM_EVENT_EXTS << " [" << maxEvents*((width-1)*(depth-1)+1) << "]" << std::endl;
	// 	cout << "SIMULATION TIME = " << time << std::endl;

	std::cout << "STATS";
	std::cout << ", Benchmark, " << strbenchmark;
	std::cout << ", PreparationTime, " << preparationTime;
	std::cout << ", Period, " << period;
	std::cout << ", MaxEvents, " << maxEvents;
	std::cout << ", Width, " << width;
	std::cout << ", Depth, " << depth;
	std::cout << ", IntDelayTime, " << intDelayTime;
	std::cout << ", ExtDelatTime, " << extDelayTime;
	std::cout << ", Num delta_int, " << DevStoneAtomic::NUM_DELT_INTS << ", [" << maxEvents*((width-1)*(depth-1)+1) << "]";
	std::cout << ", Num delta_ext, " << DevStoneAtomic::NUM_DELT_EXTS << ", [" << maxEvents*((width-1)*(depth-1)+1) << "]";
	std::cout << ", Num event_ext, " << DevStoneAtomic::NUM_EVENT_EXTS << ", [" << n_events << "]";
	std::cout << ", SIMULATION TIME, " << time1 << " , " << time2 << ", (" << 200*(time1-time2)/(time1+time2) << "%)";
	std::cout << ", MEMORY, " << resources.ru_maxrss;
	std::cout << std::endl;

	//	cout << "Tamaño: " << sizeof(adevs::Bag<PortValue>) << endl;
	return 0;
}

