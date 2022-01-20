# xDEVS simulation engine (python branch)

  Wellcome to the xDEVS simulation engine. This is the python branch, which shares the xDEVS interfaces but through the python3 language.

## Running the DEVStone benchmark
   
  Just as a quick reference manual, here are the instructions to run the DEVStone benchmark in a few seconds:

  - clone the git repository:

    ```
      git clone git@github.com:iscar-ucm/xdevs.git
    ```

  - switch to the python branch:

    ```
      git checkout python
    ```

  - install the xdevs package or **update it**:

    ```
    pip3 install git+https://github.com/iscar-ucm/xdevs@python
    ```
    or
    ```
    pip3 install git+https://github.com/iscar-ucm/xdevs@python --upgrade
    ```

  - and finally, run a DEVStone benchmark

    ```
      python3 xdevs/examples/devstone/main.py -m HO -d 100 -w 100
      Model creation time: 3.2901763916015625e-05
      Engine setup time: 0.03270745277404785
      Simulation time: 5.047549247741699 
    ```
