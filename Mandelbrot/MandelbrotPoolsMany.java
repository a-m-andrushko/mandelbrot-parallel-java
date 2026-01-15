import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.*;

public class MandelbrotPoolsMany {

    static class MandelbrotTask implements Runnable {   // a class with the task for threads
        private final int repeats;
        private final int startRow, endRow;
        private final int[][] output;
        private final int size;
        private final double xMin, xMax, yMin, yMax;
        private final int nIter;

        public MandelbrotTask(int repeats, int startRow, int endRow, int[][] output, int size, double xMin, double xMax, double yMin, double yMax, int nIter) {
            this.repeats = repeats;
            this.startRow = startRow;
            this.endRow = endRow;
            this.output = output;
            this.size = size;
            this.xMin = xMin;
            this.xMax = xMax;
            this.yMin = yMin;
            this.yMax = yMax;
            this.nIter = nIter;
        }

        @Override
        public void run() {

            double dx = (xMax - xMin) / size;
            double dy = (yMax - yMin) / size;

            for (int r = 0; r < repeats; r++) {
                for (int i = startRow; i < endRow; i++) {
                    double x = xMin + i * dx;

                    for (int j = 0; j < size; j++) {
                        double y = yMax - j * dy;

                        Mandelbrot.Complex c = new Mandelbrot.Complex(x, y);
                        output[i][j] = Mandelbrot.checkConvergence(c, nIter);
                    }
                }
            }
        }
    }

    public static int[][] calculateParallelPoolCoreBlocks(ExecutorService pool, int size, double xMin, double xMax, double yMin, double yMax, int nIter, int repeats) {   // a class for parallel calculation of all threads with division into as many parts as there are cores
        int[][] mandelbrot = new int[size][size];

        int cores = Runtime.getRuntime().availableProcessors();   // get the number of cores
        ArrayList<Future<?>> futures = new ArrayList<>();         // a list of 'futures'

        int rowsPerThread = size / cores;       // determine the number of parts
        int remainder = size % cores;           // handle pixel rows if not divisible

        int currentRow = 0;

        for (int t = 0; t < cores; t++) {       // loop over threads
            int start = currentRow;
            int rows = rowsPerThread + (t < remainder ? 1 : 0);
            int end = start + rows;

            MandelbrotTask task = new MandelbrotTask(
                    repeats, start, end, mandelbrot,
                    size, xMin, xMax, yMin, yMax, nIter
            );

            futures.add(pool.submit(task));

            currentRow = end;
        }

        waitForAll(futures);   // ensure that all futures are appended

        return mandelbrot;
    }

    public static int[][] calculateParallelPoolSmallBlocks(ExecutorService pool, int size, double xMin, double xMax, double yMin, double yMax, int nIter, int repeats, int blockSizeRows) {   // a class for parallel calculation of all threads with division into blocks of different sizes
        int[][] mandelbrot = new int[size][size];

        ArrayList<Future<?>> futures = new ArrayList<>();   // a list of futures

        for (int start = 0; start < size; start += blockSizeRows) {
            int end = Math.min(size, start + blockSizeRows);

            MandelbrotTask task = new MandelbrotTask(
                    repeats, start, end, mandelbrot,
                    size, xMin, xMax, yMin, yMax, nIter
            );

            futures.add(pool.submit(task));
        }

        waitForAll(futures);   // ensure that all futures are appended

        return mandelbrot;
    }

    private static void waitForAll(ArrayList<Future<?>> futures) {   // wait until all futures are appended
        for (Future<?> f : futures) {
            try { f.get(); }
            catch (Exception e) { throw new RuntimeException(e); }
        }
    }

    public static void main(String[] args) throws IOException {
        int[] sizes = {32, 64, 128, 256, 512, 1024, 2048, 4096};
        double xMin = -2.1;
        double xMax = 0.6;
        double yMin = -1.2;
        double yMax = 1.2;
        int nIter = 200;
        int repeats = 10;

        int cores = Runtime.getRuntime().availableProcessors();

        ArrayList<Double> timesCoreBlocks = new ArrayList<>();
        ArrayList<Double> timesSmallBlocks = new ArrayList<>();

        int[] blockSizes = {4, 8, 16, 32, 64, 128};

        for (int i = 0; i < sizes.length; i++) {   // a test for as many parts as cores
            long startTime = System.nanoTime();

            ExecutorService pool = Executors.newFixedThreadPool(cores);   // pool created HERE and included in timing

            int[][] mandelbrot = calculateParallelPoolCoreBlocks(pool, sizes[i], xMin, xMax, yMin, yMax, nIter, repeats);

            pool.shutdown();   // shutdown also included in timing
            try { pool.awaitTermination(1, TimeUnit.HOURS); }
            catch (InterruptedException e) { throw new RuntimeException(e); }

            long endTime = System.nanoTime();
            double avgTime = (endTime - startTime) / (repeats * 1_000_000_000.0);
            timesCoreBlocks.add(avgTime);

            Mandelbrot.writeMandelbrotSet(mandelbrot, "MandelbrotPoolsManyCoreBlocks_" + sizes[i]);
        }

        double[] times = timesCoreBlocks.stream().mapToDouble(Double::doubleValue).toArray();

        Mandelbrot.writeTime(sizes, times, "avgTimePoolsManyCoreBlocks");


        for (int b = 0; b < blockSizes.length; b++) {   // a test for differently sized parts
            for (int i = 0; i < sizes.length; i++) {
                long startTime = System.nanoTime();

                ExecutorService pool = Executors.newFixedThreadPool(cores);   // pool created HERE and included in timing

                int[][] mandelbrot = calculateParallelPoolSmallBlocks(pool, sizes[i], xMin, xMax, yMin, yMax, nIter, repeats, blockSizes[b]);

                pool.shutdown();
                try { pool.awaitTermination(1, TimeUnit.HOURS); }
                catch (InterruptedException e) { throw new RuntimeException(e); }

                long endTime = System.nanoTime();
                double avgTime = (endTime - startTime) / (repeats * 1_000_000_000.0);
                timesSmallBlocks.add(avgTime);

                Mandelbrot.writeMandelbrotSet(mandelbrot, "MandelbrotPoolsManyBlockSize" + blockSizes[b] + "_" + sizes[i]);
            }

            double[] times_blocks = timesSmallBlocks.stream().mapToDouble(Double::doubleValue).toArray();

            Mandelbrot.writeTime(sizes, times_blocks, "avgTimePoolsManyBlockSize" + blockSizes[b]);
        }
    }
}