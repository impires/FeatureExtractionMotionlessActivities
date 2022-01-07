package featureextractionfinal.base.geodesy;

public class CoordinatesPoint {

    // geodesic coordinates
    private String latitudeGeodesic = null;
    private String longitudeGeodesic = null;
    private String altitudeGeodesic = null;

    // constructors
    public CoordinatesPoint() {
        super();
    }

    public CoordinatesPoint(String first, String second, String third) {
        super();
        this.latitudeGeodesic = first;
        this.longitudeGeodesic = second;
        this.altitudeGeodesic = third;
    }

    // getters and setters
    public String getLatitudeGeodesic() {
        return latitudeGeodesic;
    }

    public void setLatitudeGeodesic(String latitudeGeodesic) {
        this.latitudeGeodesic = latitudeGeodesic;
    }

    public String getLongitudeGeodesic() {
        return longitudeGeodesic;
    }

    public void setLongitudeGeodesic(String longitudeGeodesic) {
        this.longitudeGeodesic = longitudeGeodesic;
    }

    public String getAltitudeGeodesic() {
        return altitudeGeodesic;
    }

    public void setAltitudeGeodesic(String altitudeGeodesic) {
        this.altitudeGeodesic = altitudeGeodesic;
    }

    // validate geodesic coordinates
    public boolean validateLatLong() {
        return Helper.validateGeodesicCoordinates(Double.parseDouble(latitudeGeodesic), Double.parseDouble(longitudeGeodesic));
    }

    public boolean isGeodesicSetted() {
        return this.latitudeGeodesic != null && this.longitudeGeodesic != null && this.altitudeGeodesic != null;
    }

    public double calculateGeodesicDistanceToAnotherPoint(CoordinatesPoint p2) {

        double p1_latitude = Double.parseDouble(latitudeGeodesic);
        double p1_longitude = Double.parseDouble(longitudeGeodesic);
        double p1_altitude = Double.parseDouble(altitudeGeodesic);
        double p2_latitude = Double.parseDouble(p2.getLatitudeGeodesic());
        double p2_longitude = Double.parseDouble(p2.getLongitudeGeodesic());
        double p2_altitude = Double.parseDouble(p2.getAltitudeGeodesic());
        double distance = Double.NaN;

        GeodeticCalculator geoCalc = new GeodeticCalculator();
        Ellipsoid reference = Ellipsoid.WGS84;
        GlobalPosition point2 = new GlobalPosition(p2_latitude, p2_longitude, p2_altitude);
        GlobalPosition point1 = new GlobalPosition(p1_latitude, p1_longitude, p1_altitude);
        GeodeticMeasurement geoMeasurement = geoCalc.calculateGeodeticMeasurement(reference, point1, point2);
        distance = geoMeasurement.getEllipsoidalDistance();

        return distance;

    }

}
