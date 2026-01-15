import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Mandelbrot {

    public static class Complex {   // a class for handling complex numbers
        private double real;
        private double imag;

        public Complex(double r, double i) {
            this.real = r;
            this.imag = i;
        }

        public double getReal() { return real; }
        public double getImaginary() { return imag; }
        public double abs() { return Math.sqrt(real*real + imag*imag); }
    }

    public static int checkConvergence(Complex c, int nIter) {   // a class for checking convergence for the Mandelbrot set calculation
        Complex z = new Complex(0,0);
        int iter = 0;

        while (iter < nIter && z.abs() < 2.0) {
            double newReal = z.getReal()*z.getReal() - z.getImaginary()*z.getImaginary() + c.getReal();
            double newImag = 2*z.getReal()*z.getImaginary() + c.getImaginary();
            z = new Complex(newReal, newImag);
            iter++;
        }

        return iter;
    }

    public static int[][] calculateMandelbrotSet(int size, double xMin, double xMax, double yMin, double yMax, int nIter) {   // calculation of the set
        int[][] mandelbrot = new int[size][size];

        double dx = (xMax - xMin) / size;
        double dy = (yMax - yMin) / size;

        for (int i = 0; i < size; i++) {
            double x = xMin + i*dx;
            for (int j = 0; j < size; j++) {
                double y = yMax - j*dy;
                Complex c = new Complex(x, y);
                mandelbrot[i][j] = checkConvergence(c, nIter);
            }
        }

        return mandelbrot;
    }

    public static void writeMandelbrotSet(int[][] data, String filename) {   // write the set to .csv
        int size = data.length;

        try (FileWriter csv = new FileWriter(filename)) {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    csv.write(data[i][j] + (j < size-1 ? "," : ""));
                }
                csv.write("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static double measureGenerationTime(int size, double xMin, double xMax, double yMin, double yMax, int nIter, int repeats) {   // measure and average out calculation time
        long totalTimeNs = 0;

        long startTime = System.nanoTime();

        for (int r = 0; r < repeats; r++)
            calculateMandelbrotSet(size, xMin, xMax, yMin, yMax, nIter);

        long endTime = System.nanoTime();

        totalTimeNs += (endTime - startTime);
        double avgTime = totalTimeNs / (repeats * 1_000_000_000.0);
        return avgTime;
    }

    public static void writeTime(int[] sizes, double[] times, String filename) {   // write time to .csv
        try (FileWriter csv = new FileWriter(filename)) {
            for (int i = 0; i < sizes.length; i++) {
                csv.write(sizes[i] + " " + times[i] + "\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        double avgTime;
        ArrayList<Double> timesList = new ArrayList<>();

        for(int i = 0; i < sizes.length; i++)
        {
            mandelbrot = calculateMandelbrotSet(sizes[i], xMin, xMax, yMin, yMax, nIter);
            writeMandelbrotSet(mandelbrot, "MandelbrotSet_" + sizes[i]);
            avgTime = measureGenerationTime(sizes[i], xMin, xMax, yMin, yMax, nIter, repeats);
            timesList.add(avgTime);
        }

        double[] times = timesList.stream().mapToDouble(Double::doubleValue).toArray();   // convert a dynamic array to a standard list

        writeTime(sizes, times, "avgTime");
    }
}