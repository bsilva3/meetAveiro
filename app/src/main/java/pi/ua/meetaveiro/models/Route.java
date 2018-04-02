package pi.ua.meetaveiro.models;

import android.content.res.Resources;
import com.google.android.gms.maps.model.LatLng;
import java.util.List;
import pi.ua.meetaveiro.R;

public class Route {
    private static int COUNTER = 0;
    private final List<LatLng> routePath;
    private String routeTitle;
    private String routeDescription;

    public Route(String routeTitle, List<LatLng> routePath, String routeDescription) {
        COUNTER++;
        this.routeTitle = routeTitle;
        this.routePath = routePath;
        this.routeDescription = routeDescription;
    }

    public Route(String routeTitle, List<LatLng> routePath) {
        this.routeTitle = routeTitle;
        this.routePath = routePath;
    }

    public Route(List<LatLng> routePath) {
        COUNTER++;
        this.routeTitle = Resources.getSystem().getString(R.string.route) + " " + COUNTER;
        this.routePath = routePath;
    }

    public Route(List<LatLng> routePath, String routeDescription) {
        COUNTER++;
        this.routeTitle = Resources.getSystem().getString(R.string.route) + " " + COUNTER;
        this.routePath = routePath;
        this.routeDescription = routeDescription;
    }

    public String getRouteTitle() {
        return routeTitle;
    }

    public void setRouteTitle(String routeTitle) {
        this.routeTitle = routeTitle;
    }

    public String getRouteDescription() {
        return routeDescription;
    }

    public void setRouteDescription(String routeDescription) {
        this.routeDescription = routeDescription;
    }

    @Override
    public String toString() {
        return routeTitle + (routeDescription == null ? "" : routeDescription);
    }
}
