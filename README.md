# Parallel Mandelbrot Set Generation in Java


## Mandelbrot Set

The Mandelbrot set is a well-known fractal defined in the complex plane and is commonly used as a benchmark problem for numerical computation and parallel programming.

More details about its mathematical definition and properties can be found here -- _https://en.wikipedia.org/wiki/Mandelbrot_set_.


## Overview

This project implements and benchmarks multiple parallelisation strategies for generating the Mandelbrot set in Java.  
The focus is on **CPU-level parallelism**, **task decomposition**, and **performance analysis** on multi-core systems.

Several approaches to concurrency are implemented and compared, including manual thread management and thread pools with different workload partitioning strategies.


## Key Features

- Sequential generation of the Mandelbrot set;
- Pure multithreading (manual `Thread` creation);
- Thread pool–based parallelisation (`ExecutorService`);
- Multiple workload partitioning strategies:
  - One block per CPU core;
  - Many small blocks of configurable size;
- Performance benchmarking using high-resolution timing;
- Output of computation results and timing data to text files.


## Technical Concepts Demonstrated

- Java concurrency (`Thread`, `Runnable`);
- Thread pools and task scheduling (`ExecutorService`, `Future`);
- Synchronisation and task completion handling;
- Load balancing and work granularity;
- Numerical computation (complex dynamics);
- Performance benchmarking (`System.nanoTime`);
- File I/O for data export.


## Project Structure

├── `Mandelbrot.java`

│ ├── Complex number implementation

│ ├── Convergence test

│ ├── Sequential generator of the Mandelbrot set

│ └── Output utilities

│

├── `MandelbrotThreads.java`

│ └── Pure multithreading (one thread per CPU core)

│

├── `MandelbrotPoolsOne.java`

│ └── Single thread pool reused across computations

│

├── `MandelbrotPoolsMany.java`

│ └── New thread pool created for each computation

│

├── `Mandelbrot.ipynb`

│ └── A Python notebook with exemplary results visualisation

│

└── README.md


## How It Works

For a given image resolution `N × N`:

1. A region of the complex plane is discretised into pixels;
2. Each pixel is tested for the Mandelbrot set convergence;
3. The computation is parallelised using:
   - Manual threads;
   - Thread pools;
4. The image is divided into:
   - As many blocks as CPU cores;
   - Many smaller blocks (4–128 rows per task);
5. Execution time is measured and averaged over multiple runs;
6. Results are written to text files for external analysis.


## Performance Measurements

The program benchmarks the following image sizes: 32, 64, 128, 256, 512, 1024, 2048, 4096.

Measured metrics include:

- Total execution time;
- Thread creation overhead;
- Thread pool reuse vs recreation;
- Impact of task granularity on performance.

The generated timing files can be plotted using external tools such as Python, gnuplot, or spreadsheet software.


## Requirements for Build and Run

- Java **JDK 11** or newer;
- Multi-core CPU (necessary for meaningful results).

Each class contains its own `main()` method and can be executed independently.


## Output

1. The Mandelbrot set data files (iteration counts per pixel).
2. Timing files containing average execution times per image size.

Output is written in plain text format to allow easy external analysis and plotting.
