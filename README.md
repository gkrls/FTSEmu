# TDS
A Distributed System Simulator for a single node. It has been developed as a platform for analyzing and benchmarking a novel approach on Fault-Tolerant Termination Detection based on an optimized  version of Safra's Termination Detection algorithm. 

More: TBA

Ref:<br/>
[1] https://www.cs.utexas.edu/users/EWD/ewd09xx/EWD998.PDF<br/>
[2] http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.22.4105&rep=rep1&type=pdf


# Usage 
 - Build
```sh
$ ant jar
```	
 - Run

```sh
$ java -jar TDS-0.1.jar [OPTION]...  
```

# Options

| Option    | Description                                                                                                     | Possible Parameters                                                                                                                                                                                                                               | Default |
|-----------|-----------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------|
| -h        | Print help                                                                                                      | -                                                                                                                                                                                                                                                 | off     |
| -v        | Verbose output                                                                                                  | -                                                                                                                                                                                                                                                 | off     |
| -ver      | Algorithm version                                                                                               | 1 (OFSS), 2 (IFSS), 3 (FTS), 0 (All)                                                                                                                                                                                                              | 0       |
| -n        | Number of nodes                                                                                                 | [2, 1024]                                                                                                                                                                                                                                         | 4       |
| -c        | Number of crashing nodes                                                                                        | -c : random <br/>-c [0, N] : crash 0 up to N nodes                                                                                                                                                                                                     | 0       |
| -l        | Activity level                                                                                                  | 1, 2                                                                                                                                                                                                                                              | 1       |
| -w        | Max wait time for a run (ms)                                                                                    | >0                                                                                                                                                                                                                                                | 100000  |
| -csv      | Write performance metrics to a csv file                                                                         | -                                                                                                                                                                                                                                                 | off     |
| -f        | Clean (flush) the csv (if one exists with a  name corresponding to this run's params)                           | -                                                                                                                                                                                                                                                 | off     |
| -dist     | Choose a probability distribution by which random activity is determined                                        | -(1) : Uniform <br/>-(2) : Gaussian                                                                                                                                                                                                                    |         |
| -strategy | Choose a strategy by which activity is happening (random under chose distribution),  when a node becomes active | -(1) : Compute-Send - Perform some computation and then send some messages (uniform 0-3, gaussian mean=1 sd=1) <br/>-(2) : N-Activities - Choose an N under given distribution. For each of the n activities randomly choose if computation of message | 1       |
| -batype   | Basic Algorithm type                                                                                            | -(1) :  Centralized (Node 0 is initially active) <br/>-(2) : Decentralized-Even (Nodes with even id's are initially active) <br/>-(3) : Decentralized-Random (A random number of nodes, uniformly chosen, is initially active)                              | 2       |
| -ci       | Interval between 2 consecutive node crashes                                                                     | -(1) : Uniform in [200, 3000] ms <br/>-(2) : Gaussian with mean 1000ms and sd 200ms                                                                                                                                                                    | 1       |
| -anl      | Average network latency                                                                                         | Value in [20, 100] (Message delays are random with Gaussian distribution with mean -avl and standard deviation of 10 ms                                                                                                                           | 60      |
# Verbose Output
This is a typical output when running with option **-v** and default **-ver**


<p align="center">
  <img src="http://i.imgur.com/OaIwYsN.png" alt="simulator screenshot"/>
</p>

# Issues
[1] There is currently a bug introduced after the migration. When all versions run at the same time, occasionally, one will hang with no activity taking place for that version.  
