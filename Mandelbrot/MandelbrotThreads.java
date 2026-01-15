import java.io.IOException;
import java.util.ArrayList;

public class MandelbrotThreads {

    static class MandelbrotWorker extends Thread {   // a class with the task for threads
        private final int repeats;
        private final int startRow, endRow;
        private final int[][] output;
        private final int size;
        private final double xMin, xMax, yMin, yMax;
        private final int nIter;

        public MandelbrotWorker(int repeats, int startRow, int endRow, int[][] output, int size, double xMin, double xMax, double yMin, double yMax, int nIter) {
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

    public static int[][] calculateParallel(int size, double xMin, double xMax, double yMin, double yMax, int nIter, int repeats) {   // a class for parallel calculation of all threads
        int[][] mandelbrot = new int[size][size];

        int cores = Runtime.getRuntime().availableProcessors();   // get the number of cores
        Thread[] threads = new Thread[cores];

        int rowsPerThread = size / cores;   // determine the number of parts
        int remainder = size % cores;   // handle pixel rows if not divisible

        int currentRow = 0;

        for (int t = 0; t < cores; t++) {   // loop over threads
            int start = currentRow;
            int rows = rowsPerThread + (t < remainder ? 1 : 0);
            int end = start + rows;

            threads[t] = new MandelbrotWorker(repeats, start, end, mandelbrot, size, xMin, xMax, yMin, yMax, nIter);

            threads[t].start();

            currentRow = end;
        }

        for (Thread t : threads) {   // wait for completion of all threads
            try { t.join(); }
            catch (InterruptedException e) { throw new RuntimeException(e); }
        }

        return mandelbrot;
    }

    public static void main(String[] args) throws IOException {
        int[] sizes = {32, 64, 128, 256, 512, 1024, 2048, 4096};
        double xMin = -2.1;
        double xMax = 0.6;
        double yMin = -1.2;
        double yMax = 1.2;
        int nIter = 200;
        int[][] mandelbrot;
        int repeats = 10;
        ArrayList<Double> timesList = new ArrayList<>();

        for(int i = 0; i < sizes.length; i++)
        {
            long startTime = System.nanoTime();

            mandelbrot = MandelbrotThreads.calculateParallel(sizes[i], xMin, xMax, yMin, yMax, nIter, repeats);

            long endTime = System.nanoTime();
            double avgTime = (endTime - startTime) / (repeats * 1_000_000_000.0);
            timesList.add(avgTime);

            Mandelbrot.writeMandelbrotSet(mandelbrot, "MandelbrotSetThreads_" + sizes[i]);
        }

        double[] times = timesList.stream().mapToDouble(Double::doubleValue).toArray();   // convert a dynamic array to a standard list

        Mandelbrot.writeTime(sizes, times, "avgTimeThreads");
    }
}