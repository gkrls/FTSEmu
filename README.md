# TDS
Termination Detection Simulator

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

   Option   |                  Description                   | Params                                                  | Default |
------------|------------------------------------------------|---------------------------------------------------------|---------|
 -h 	    | Print help                                     |    -                                                    |off      |
 -ver {val} | Algorithm version                              |1,2,3 or any combination. 0 for all versions |All        |0        |
 -n {val}   | Number of nodes.                               |4 - 1024                                                 |4        |
 -l {val}   | Level of activity                              |2-4                                                      |2        |
 -w {val}   | Maximum wait time in ms.                       |>0                                                       |100000   |
 -v 	    | Verbose                                        |    -                                                    |off      |
 -csv	    | Write performance metrics to csv               |    -                                                    |off      | 
 -f	    | flush the csv file                             |    -                                                    |off      |
 -log	    | Log ouput in files    (N/A yet)                |    -                                                    |off      |


# Issues
[1] There is currently a bug introduced after the migration. When all version run at the same time, occasionally, one will hang with no activity taking place for that version.  
