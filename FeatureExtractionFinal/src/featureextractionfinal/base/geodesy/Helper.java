package featureextractionfinal.base.geodesy;

public class Helper {

    public static CoordinatesPoint point1 = null;
    public static CoordinatesPoint point2 = null;

    /**
     * Validate Geodesic Latitude and Longitude
     *
     * @param latitude
     * @param longitude
     * @return
     */
    public static boolean validateGeodesicCoordinates(double latitude, double longitude) {

        if (latitude < -90.0 || latitude > 90.0 || longitude < -180.0 || longitude >= 180.0) {
            return false;
        }
        return true;
    }

    /**
     * Validate if String is a number
     *
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    /**
     * Validate if string is a letter
     *
     * @param str
     * @return
     */
    public static boolean isChar(String str) {
        if (str.length() == 1) {
            return Character.isLetter(str.charAt(0));
        }
        return false;
    }
}
