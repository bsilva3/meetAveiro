package pi.ua.meetaveiro.others;

public class Constants {

    private Constants() {}

    // Package Names

    private static final String ACTIVITIES_PACKAGE_NAME = "pi.ua.meetaveiro.activities";
    private static final String SERVICES_PACKAGE_NAME = "pi.ua.meetaveiro.services";
    private static final String FRAGMENTS_PACKAGE_NAME = "pi.ua.meetaveiro.fragments";

    //Alterar consoante o IP do servidor (que pode ser consultado com ip addr show)
    public final static String API_URL = "http://192.168.160.192:8080";
    //url to log in a user
    public final static String URL_LOG_USER = API_URL + "/resources/users";
    //url to send an image to the server to scan
    public final static String IMAGE_SCAN_URL = API_URL + "/search";
    //url to send image feedback
    public final static String FEEDBACK_URL = API_URL + "/search/feedback";
    // url to fetch all community routes
    public static final String URL_COMMUNITY_ROUTES = API_URL + "/resources/routes/community";
    // url to fetch all created routes
    public static final String URL_CREATED_ROUTES = API_URL + "/resources/routes/byuser";
    //url to send image feedback
    public final static String URL_SEND_ROUTE = API_URL + "/resources/routes";
    // url to fetch all routes
    public static final String URL_ROUTES = API_URL + "/blahblah";
    // Rota pelo email do utilizador
    public static final String URL_ROUTE_HISTORY = API_URL + "/resources/routes/byuser";
    // url to fetch routes history json
    public static final String URL_ATTRACTIONS = API_URL + "/resources/atractions";
    // url to fetch routes history json
    public static final String URL_PHOTO_HISTORY = API_URL + "/resources/photos/byuser";
    // url to fetch routes that have a certain attraction
    public static final String URL_ROUTES_IN_ATTRACTION = API_URL + "/resources/routes/search";
    // url to fetch information of an attraction (aka concept)
    public static final String URL_ATTRACTION_INFO = API_URL + "/resources/atractions/";
    // url to fetch information of an attraction (aka concept)
    public static final String URL_ROUTES_ATTRACTION = API_URL + "/blahblah";
    // url to fetch routes history json
    public static final String URL_EVENTS = API_URL + "/resources/events";

    //Rota por id (NO MARKERS)
    public static final String ROUTE_BY_ID = API_URL + "/resources/routes/"; //+id
    //Instancia por user
    public static final String INSTANCE_BY_USER = API_URL + "/resources/routes/instances"; //+ user
    //Instancia por id
    public static final String INSTANCE_BY_ID= API_URL + "/resources/routes/instances/"; //+ id


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
    public static final float GEOFENCE_RADIUS_IN_METERS = 30; // 0.2 km

    // LocationUpdatesServiceConstants

    public static final String EXTRA_STARTED_FROM_NOTIFICATION = SERVICES_PACKAGE_NAME + ".started_from_notification";
    public static final String EXTRA_TAKE_PHOTO = SERVICES_PACKAGE_NAME + ".take_photo";
    public static final String ACTION_BROADCAST = SERVICES_PACKAGE_NAME + ".broadcast";
    public static final String EXTRA_LOCATION = SERVICES_PACKAGE_NAME + ".location";
    public static final String NEW_ROUTE_EXTRA = SERVICES_PACKAGE_NAME + ".new_route";
    //for image size when photo is taken
    public final static int DEFAULT_WIDTH = 1000;
    public final static int DEFAULT_HEIGHT = 1000;
    //image width and height of images in markers
    public final static  int THUMBSIZE = 200;

    public enum ROUTE_STATE{
        STARTED, PAUSED, STOPPED;

        public static ROUTE_STATE toMyEnum (String myEnumString) {
            try {
                return valueOf(myEnumString);
            } catch (Exception ex) {
                // For error cases
                return STARTED;
            }
        }
    }
}
