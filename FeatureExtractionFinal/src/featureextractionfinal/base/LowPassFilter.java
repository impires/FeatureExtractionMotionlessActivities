/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package featureextractionfinal.base;

/**
 *
 * @author Ivan
 */
public class LowPassFilter {

    public static final float ALPHA = 0.25f; // if ALPHA = 1 OR 0, no filter applies.

    public static float[] lowPass(float[] input, float[] output) {
        if (output == null) {
            return input;
        }
        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

}
