/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package featureextractionfinal.sleeping_bedroom;

import featureextractionfinal.base.LowPassFilter;
import featureextractionfinal.base.geodesy.CoordinatesPoint;
import featureextractionfinal.base.geodesy.Helper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ivan
 */
public class FeatureExtractionAccelerometerMagnetometerGyroscopeDistance {

    public static String getRow(String folder, String filename) {
        TreeMap<Long, Float> original = new TreeMap<>();
        TreeMap<Long, Float> values = new TreeMap<>();
        TreeMap<Long, Float> valuesMax = new TreeMap<>();
        List<Long> times = new ArrayList<>();

        double maximum = 0;
        double minimum = 0;
        double variance = 0;
        double std_dev = 0;
        double median = 0;
        double mean = 0;
        long[] dist_peaks = null;
        double variance_peaks = 0;
        double std_dev_peaks = 0;
        double mean_peaks = 0;
        double median_peaks = 0;
        double sum_peaks = 0;
        double sum = 0;
        double[] deviations = null;
        double[] squares = null;
        String line = null;

        float[] aux_low_pass_filter = null;

        System.out.println(folder + " " + filename);

        // clear the arrays
        original.clear();
        values.clear();
        valuesMax.clear();
        times.clear();

        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader = new FileReader("data/sleeping/" + folder + "/" + filename + ".txt");

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while ((line = bufferedReader.readLine()) != null) {
                String[] arr = line.split("\t");
                if (arr.length > 1) {
                    /**
                     * Apply low pass filter
                     */
                    float[] floats_arr = new float[3];
                    floats_arr[0] = Float.parseFloat(arr[1]);
                    floats_arr[1] = Float.parseFloat(arr[2]);
                    floats_arr[2] = Float.parseFloat(arr[3]);

                    aux_low_pass_filter = LowPassFilter.lowPass(floats_arr, aux_low_pass_filter);

                    float acc = (float) Math.sqrt((aux_low_pass_filter[0] * aux_low_pass_filter[0]) + (aux_low_pass_filter[1] * aux_low_pass_filter[1]) + (aux_low_pass_filter[2] * aux_low_pass_filter[2]));
                    original.put(Long.parseLong(arr[0]), acc);
                }
            }

            // Always close files.
            bufferedReader.close();

            /**
             * Calculate maximum peaks
             */
            values = (TreeMap<Long, Float>) original.clone();

            int nValues = 0;

            long[] aux_time = new long[3];
            float[] aux_acc = new float[3];

            do {

                int nCounts = 0;

                for (HashMap.Entry<Long, Float> entry : values.entrySet()) {
                    if (nCounts == 0) {
                        aux_time[0] = entry.getKey();
                        aux_acc[0] = entry.getValue();
                    } else if (nCounts == 1) {
                        aux_time[1] = entry.getKey();
                        aux_acc[1] = entry.getValue();
                    } else if (nCounts == 2) {
                        aux_time[2] = entry.getKey();
                        aux_acc[2] = entry.getValue();

                        if (aux_acc[0] < aux_acc[1] && aux_acc[2] < aux_acc[1]) {
                            valuesMax.put(aux_time[1], aux_acc[1]);
                            aux_time[0] = aux_time[2];
                            aux_acc[0] = aux_acc[2];
                            nCounts = 0;
                        } else {
                            aux_time[0] = aux_time[1];
                            aux_acc[0] = aux_acc[1];
                            aux_time[1] = aux_time[2];
                            aux_acc[1] = aux_acc[2];
                            nCounts = 1;
                        }
                    }

                    nCounts++;
                }

                nValues = valuesMax.size();

                if (nValues > 8) {
                    values.clear();
                    values.putAll(valuesMax);
                    valuesMax.clear();
                }

            } while (nValues > 8);

            /**
             * Calculate time between peaks
             */
            long first = 0;
            long second = 0;
            int count = 1;

            for (HashMap.Entry<Long, Float> entry : values.entrySet()) {
                if (first == 0) {
                    first = entry.getKey();
                } else {
                    second = entry.getKey();
                    long time = second - first;
                    times.add(time);
                    first = second;
                    count++;
                }
            }

            /**
             * Maximum
             */
            maximum = Collections.max(original.values());

            /**
             * Minimum
             */
            minimum = Collections.min(original.values());

            /**
             * Median
             */
            Collection<Float> data_collection = original.values();
            List<Float> data = new ArrayList<>(data_collection);

            Collections.sort(data);

            if (data.size() % 2 == 0) {
                median = (data.get((data.size() / 2) - 1) + data.get(data.size() / 2)) / 2.0;
            }
            median = data.get(data.size() / 2);

            /**
             * Mean
             */
            original = (TreeMap<Long, Float>) original.clone();

            sum = 0;
            mean = 0;

            for (HashMap.Entry<Long, Float> entry : original.entrySet()) {
                sum += entry.getValue();
            }

            mean = sum / original.size();

            /**
             * Standard Deviation
             */
            deviations = new double[original.size()];
            count = 0;

            for (HashMap.Entry<Long, Float> entry : original.entrySet()) {
                deviations[count] = entry.getValue() - mean;
                count++;
            }

            squares = new double[original.size()];

            // getting the squares of deviations
            for (int j = 0; j < squares.length; j++) {
                squares[j] = deviations[j] * deviations[j];
            }

            sum = 0;

            // adding all the squares
            for (int j = 0; j < squares.length; j++) {
                sum = sum + squares[j];
            }

            // dividing the numbers by one less than total numbers
            double result = sum / (original.size() - 1);

            std_dev = Math.sqrt(result);

            /**
             * Variance
             */
            variance = result;

            /**
             * 5 highest distances between peaks
             */
            Collections.sort(times);
            Collections.reverse(times);

            dist_peaks = new long[5];

            for (int j = 0; j < 5; j++) {
                if (j > times.size() - 1) {
                    dist_peaks[j] = times.get(0);
                } else {
                    dist_peaks[j] = times.get(j);
                }
            }

            /**
             * Mean of peaks
             */
            sum_peaks = 0;

            for (HashMap.Entry<Long, Float> entry : values.entrySet()) {
                sum_peaks += entry.getValue();
            }

            mean_peaks = sum_peaks / values.size();

            /**
             * Standard deviation of peaks
             */
            deviations = new double[values.size()];
            count = 0;

            for (HashMap.Entry<Long, Float> entry : values.entrySet()) {
                deviations[count] = entry.getValue() - mean_peaks;
                count++;
            }

            squares = new double[values.size()];

            // getting the squares of deviations
            for (int j = 0; j < squares.length; j++) {
                squares[j] = deviations[j] * deviations[j];
            }

            sum_peaks = 0;

            // adding all the squares
            for (int j = 0; j < squares.length; j++) {
                sum_peaks = sum_peaks + squares[j];
            }

            // dividing the numbers by one less than total numbers
            double result_peaks = sum_peaks / (values.size() - 1);

            std_dev_peaks = Math.sqrt(result_peaks);

            /**
             * Variance of peaks
             */
            variance_peaks = result_peaks;

            /**
             * Median of peaks
             */
            Collection<Float> data_collection_peaks = values.values();
            List<Float> data_peaks = new ArrayList<>(data_collection_peaks);

            Collections.sort(data_peaks);

            if (data_peaks.size() % 2 == 0) {
                median = (data_peaks.get((data_peaks.size() / 2) - 1) + data_peaks.get(data_peaks.size() / 2)) / 2.0;
            }
            median_peaks = data_peaks.get(data_peaks.size() / 2);

            /**
             * Save data to a file
             */
            String line_to_file = "";

            line_to_file += dist_peaks[0] + "\t";

            for (int j = 1; j < dist_peaks.length; j++) {
                line_to_file += dist_peaks[j] + "\t";
            }

            line_to_file += mean_peaks + "\t";
            line_to_file += std_dev_peaks + "\t";
            line_to_file += variance_peaks + "\t";
            line_to_file += median_peaks + "\t";
            line_to_file += std_dev + "\t";
            line_to_file += mean + "\t";
            line_to_file += maximum + "\t";
            line_to_file += minimum + "\t";
            line_to_file += variance + "\t";
            line_to_file += median;

            return line_to_file;

        } catch (FileNotFoundException ex) {
            return null;
        } catch (IOException ex) {
            return null;
        }
    }
    
    public static String getDistance(String folder, String filename) {
        Helper.point1 = null;
        Helper.point2 = null;
        double distance = 0;
        String line = null;

        System.out.println(folder + " " + filename);

        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader = new FileReader("data/sleeping/" + folder + "/" + filename + ".txt");

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while ((line = bufferedReader.readLine()) != null) {
                String[] arr = line.split("\t");
                if (arr.length > 1) {

                    if (Helper.point1 == null) {
                        Helper.point1 = new CoordinatesPoint(arr[1], arr[2], "0");
                    } else {
                        Helper.point2 = new CoordinatesPoint(arr[1], arr[2], "0");
                        
                        distance += Helper.point1.calculateGeodesicDistanceToAnotherPoint(Helper.point2);

                        Helper.point1 = Helper.point2;
                    }

                }
            }

            // Always close files.
            bufferedReader.close();

            return distance + "";

        } catch (FileNotFoundException ex) {
            return null;
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {

        for (int i = 1; i <= 2256; i++) {

            String line_acc = getRow(i + "", "accelerometer");
            String line_mag = getRow(i + "", "magnetometer");
            String line_gir = getRow(i + "", "gyroscope");
            String line_dist = getDistance(i + "", "location");

            String line_to_file = line_acc + "\t" + line_mag + "\t" + line_gir + "\t" + line_dist;

            try (FileWriter fw = new FileWriter("results/sleeping_bedroom/accelerometer_magnetometer_gyroscope_distance.txt", true);
                    BufferedWriter bw = new BufferedWriter(fw);
                    PrintWriter out = new PrintWriter(bw)) {
                out.println(line_to_file);
            } catch (IOException e) {
                Logger.getLogger(FeatureExtractionAccelerometerMagnetometerGyroscopeDistance.class.getName()).log(Level.SEVERE, null, e);
            }
        }

    }

}
