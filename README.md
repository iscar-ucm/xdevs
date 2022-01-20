# xDEVS simulation engine (python branch)

  Wellcome to the xDEVS simulation engine. This is the python branch, which shares the xDEVS interfaces but through the python3 language.

## Running the DEVStone benchmark
   
  Just as a quick reference manual, here are the instructions to run the DEVStone benchmark in a few seconds:

  - clone the git repository:

    #+BEGIN_SRC shell
      git clone git@github.com:iscar-ucm/xdevs.git
    #+END_SRC

  - switch to the python branch

    #+BEGIN_SRC shell
      git checkout python
    #+END_SRC

  - change directory (to the DEVStone example):

    #+BEGIN_SRC shell
      cd src/xdevs/examples/DevStone 
    #+END_SRC

  - make

    #+BEGIN_SRC shell
      make
    #+END_SRC

  - and finally, run a DEVStone benchmark

    #+BEGIN_SRC shell
      ./devstone -w 100 -d 100 -b HO
      STATS
      Benchmark: HO (2)
      PreparationTime: 0
      Period: 1
      MaxEvents: 1
      Width: 100
      Depth: 100
      IntDelayTime: 0
      ExtDelatTime: 0
      Num delta_int: 490051, [490051]
      Num delta_ext: 490051, [490051]
      Num event_ext: 490051, [490051]
      Model creation time (s): 0.0136859
      Engine setup time (s): 0.00449369
      Simulation time (s): 0.721096
      TOTAL time (s): 0.739276
      MEMORY (KiB): 11752
    #+END_SRC
