package utils;

import location.model.LocationData;
import model.SensorValue;

public class util {

    public static LocationData getLocationData() {
        return locationData;
    }

    public static void setLocationData(LocationData locationData) {
        util.locationData = locationData;
    }

    public static LocationData locationData;


}
