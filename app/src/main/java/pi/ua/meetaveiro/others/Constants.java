package pi.ua.meetaveiro.others;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

public class Constants {

    private Constants() {}

    // Pakcage Names

    private static final String ACTIVITIES_PACKAGE_NAME = "pi.ua.meetaveiro.activities";
    private static final String SERVICES_PACKAGE_NAME = "pi.ua.meetaveiro.services";
    private static final String FRAGMENTS_PACKAGE_NAME = "pi.ua.meetaveiro.fragments";

    //Alterar consoante o IP do servidor (que pode ser consultado com ip addr show)
    public final static String API_URL = "http://192.168.160.192:8080";
    //url to send an image to the server to scan
    public final static String IMAGE_SCAN_URL = API_URL + "/search";
    //url to send image feedback
    public final static String FEEDBACK_URL = API_URL + "/search/feedback";
    // url to fetch all routes
    public static final String URL_ROUTES = API_URL + "/blahblah";
    // url to fetch routes history json
    public static final String URL_ROUTE_HISTORY = API_URL + "/blahblah";
    // url to fetch routes history json
    public static final String URL_ATTRACTIONS = API_URL + "/blahblah";
    // url to fetch routes history json
    public static final String URL_ATTRACTION_HISTORY = API_URL + "/blahblah";
    // url to fetch routes that have a certain attraction
    public static final String URL_ROUTES_ATTRACTION = API_URL + "/blahblah";

    // Geofence related constants

    public static final String GEOFENCES_ADDED_KEY = ACTIVITIES_PACKAGE_NAME + ".GEOFENCES_ADDED_KEY";
    /**
     * Used to set an expiration time for a geofence. After this amount of time Location Services
     * stops tracking the geofence.
     */
    private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
    // For this sample, geofences expire after twelve hours.
    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;
    public static final float GEOFENCE_RADIUS_IN_METERS = 209; // 0.2 km

    // LocationUpdatesServiceConstants

    public static final String EXTRA_STARTED_FROM_NOTIFICATION = SERVICES_PACKAGE_NAME + ".started_from_notification";
    public static final String ACTION_BROADCAST = SERVICES_PACKAGE_NAME + ".broadcast";
    public static final String EXTRA_LOCATION = SERVICES_PACKAGE_NAME + ".location";

    public enum ROUTE_STATE{
        STARTED, PAUSED, STOPPED, STARTED_NAVIGATION
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
