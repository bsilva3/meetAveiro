package pi.ua.meetaveiro.others;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

public class Constants {

    private Constants() {}

    //Alterar consoante o IP do servidor (que pode ser consultado com ip addr show)
    public final static String API_URL = "http://192.168.54.155:8080/search";

    // url to fetch all routes
    public static final String URL_ROUTES = "https://localhost..blahblah";
    // url to fetch routes history json
    public static final String URL_ROUTE_HISTORY = "https://localhost..blahblah";
    // url to fetch routes history json
    public static final String URL_ATTRACTIONS = "https://localhost..blahblah";
    // url to fetch routes history json
    public static final String URL_ATTRACTION_HISTORY = "https://localhost..blahblah";

    // url to fetch routes that have a certain attraction
    public static final String URL_ROUTES_ATTRACTION = "https://localhost..blahblah";

    public final static String PREFERENCES_FILENAME = "pi.ua.meetaveirocom.PREFERENCE_FILE_KEY";

    private static final String PACKAGE_NAME = "com.google.android.gms.location.Geofence";

    static final String GEOFENCES_ADDED_KEY = PACKAGE_NAME + ".GEOFENCES_ADDED_KEY";

    /**
     * Used to set an expiration time for a geofence. After this amount of time Location Services
     * stops tracking the geofence.
     */
    private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;

    /**
     * For this sample, geofences expire after twelve hours.
     */
    static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;
    static final float GEOFENCE_RADIUS_IN_METERS = 500; // 500 meters

    public enum ROUTE_STATE{
        STARTED, PAUSED, STOPPED
    }

    /**
     * Map for storing geofence locations (current is just for testing)
     */
    static final HashMap<String, LatLng> BAY_AREA_LANDMARKS = new HashMap<>();

    static {
        // San Francisco International Airport.
        BAY_AREA_LANDMARKS.put("SFO", new LatLng(40.65148, -8.43735));

        // Googleplex.
        BAY_AREA_LANDMARKS.put("GOOGLE", new LatLng(37.422611,-122.0840577));
    }
}
