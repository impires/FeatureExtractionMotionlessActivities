/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package featureextractionfinal.street;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jtransforms.fft.FloatFFT_1D;
import featureextractionfinal.base.comirva.audio.util.MFCC;

/**
 *
 * @author Ivan
 */
public class FeatureExtractionSound {

    public static String getRow(String root, String folder, String filename) throws IOException {
        List<Integer> values = new ArrayList<>();
        List<Float> normalizedValues = new ArrayList<>();
        List<Float> FFTValues = new ArrayList<>();

        double maximum = 0;
        double minimum = 0;
        double variance = 0;
        double std_dev = 0;
        double median = 0;
        double mean = 0;
        double sum = 0;
        double[] deviations = null;
        double[] squares = null;
        String line = null;
        int count;

        System.out.println(folder + " - " + root);

        // clear the arrays
        values.clear();
        normalizedValues.clear();
        FFTValues.clear();

        try {

            // FileReader reads text files in the default encoding.
            FileReader fileReader = new FileReader("data/" + root + "/" + folder + "/sound.txt");

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while ((line = bufferedReader.readLine()) != null) {
                values.add(Integer.parseInt(line));
            }

            // Always close files.
            bufferedReader.close();

            /**
             * Convert List to Array
             */
            float[] input = new float[values.size()];

            for (int j = 0; j < values.size(); j++) {
                input[j] = values.get(j);
            }

            /**
             * Apply FFT
             */
            try {
                FloatFFT_1D fftlib = new FloatFFT_1D(values.size());
                fftlib.realForward(input);
            } catch (Exception e) {
                return null;
            }

            for (int j = 0; j < input.length; j++) {
                FFTValues.add((float) input[j]);
            }

            /**
             * Maximum
             */
            maximum = Collections.max(FFTValues);

            /**
             * Minimum
             */
            minimum = Collections.min(FFTValues);

            /**
             * Normalize Data (MIN/MAX)
             */
            for (int j = 0; j < FFTValues.size(); j++) {
                normalizedValues.add((float) ((FFTValues.get(j) - minimum) / (maximum - minimum)));
            }

            /**
             * Mean
             */
            sum = 0;
            mean = 0;

            for (float entry : normalizedValues) {
                sum += entry;
            }

            mean = sum / normalizedValues.size();

            /**
             * Standard Deviation
             */
            deviations = new double[normalizedValues.size()];
            count = 0;

            for (float entry : normalizedValues) {
                deviations[count] = entry - mean;
                count++;
            }

            squares = new double[normalizedValues.size()];

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
            double result = sum / (normalizedValues.size() - 1);

            std_dev = Math.sqrt(result);

            /**
             * Variance
             */
            variance = result;

            /**
             * Median
             */
            Collection<Float> data_collection = normalizedValues;
            List<Float> data = new ArrayList<>(data_collection);

            Collections.sort(data);

            if (data.size() % 2 == 0) {
                median = (data.get((data.size() / 2) - 1) + data.get(data.size() / 2)) / 2.0;
            }
            median = data.get(data.size() / 2);

            /**
             * MFCC coefficients
             */
            try {

                MFCC mfcc = new MFCC(44100, 32768, 26, true);
                double[] input_mfcc = new double[32768];

                for (int j = 0; j < 32768; j++) {
                    input_mfcc[j] = values.get(j);
                }

                double[][] mfcc_coefs = mfcc.process(input_mfcc);

                /**
                 * Save data to a file
                 */
                String line_to_file = "";

                for (int j = 0; j < mfcc_coefs.length; j++) {

                    for (int k = 0; k < mfcc_coefs[j].length; k++) {
                        line_to_file += mfcc_coefs[j][k] + "\t";
                    }

                }

                line_to_file += std_dev + "\t";
                line_to_file += mean + "\t";
                line_to_file += maximum + "\t";
                line_to_file += minimum + "\t";
                line_to_file += variance + "\t";
                line_to_file += median;

                return line_to_file;

            } catch (Exception e) {
                return null;
            }

        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {

        // running
        
        for (int i = 1; i <= 2206; i++) {
            
            String line_to_file = getRow("running", i + "", "sound");

            if(line_to_file != null) {

                try (FileWriter fw = new FileWriter("results/street/sound.txt", true);
                        BufferedWriter bw = new BufferedWriter(fw);
                        PrintWriter out = new PrintWriter(bw)) {
                    out.println(line_to_file);
                } catch (IOException e) {
                    Logger.getLogger(FeatureExtractionSound.class.getName()).log(Level.SEVERE, null, e);
                }

            }
        }
        
        // walking

        for (int i = 1; i <= 2027; i++) {

            String line_to_file = getRow("walking", i + "", "sound");

            if(line_to_file != null) {

                try (FileWriter fw = new FileWriter("results/street/sound.txt", true);
                        BufferedWriter bw = new BufferedWriter(fw);
                        PrintWriter out = new PrintWriter(bw)) {
                    out.println(line_to_file);
                } catch (IOException e) {
                    Logger.getLogger(FeatureExtractionSound.class.getName()).log(Level.SEVERE, null, e);
                }

            }
        }
        
    }

}
