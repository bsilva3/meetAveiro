package pi.ua.meetaveiro.models;

import android.content.res.Resources;
import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

import java.util.List;
import java.util.Map;

import pi.ua.meetaveiro.R;

public class Route {
    private static int COUNTER = 0;
    private Polyline routePath;
    private Map<Marker, Bitmap> routeMarkers;
    private String routeTitle = "Unnamed"; //default
    private String routeDescription = "No description"; //default

    public Route(String routeTitle, Polyline routePath, String routeDescription, Map<Marker, Bitmap> routeMarkers) {
        COUNTER++;
        this.routeTitle = routeTitle;
        this.routePath = routePath;
        this.routeDescription = routeDescription;
        this.routeMarkers=routeMarkers;
    }

    public Route(String routeTitle, Polyline routePath, Map<Marker, Bitmap> routeMarkers) {
        this.routeTitle = routeTitle;
        this.routePath = routePath;
        this.routeMarkers = routeMarkers;
    }

    public Route(String routeTitle, String routeDescription) {
        this.routeTitle = routeTitle;
        this.routeDescription = routeDescription;
    }

    public Route(Polyline routePath, Map<Marker, Bitmap> routeMarkers) {
        COUNTER++;
        this.routeTitle = Resources.getSystem().getString(R.string.route) + " " + COUNTER;
        this.routePath = routePath;
        this.routeMarkers = routeMarkers;
    }

    public Route(String routeTitle) {
        COUNTER++;
        this.routeTitle = routeTitle;
    }

    public Route(Polyline routePath, String routeDescription, Map<Marker, Bitmap> routeMarkers) {
        COUNTER++;
        this.routeTitle = Resources.getSystem().getString(R.string.route) + " " + COUNTER;
        this.routePath = routePath;
        this.routeDescription = routeDescription;
        this.routeMarkers = routeMarkers;
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

    public Polyline getRoutePath() {
        return routePath;
    }

    public Map<Marker, Bitmap> getRouteMarkers() {
        return routeMarkers;
    }

    public List<LatLng> getRoutePoints(){
        return routePath.getPoints();
    }

    @Override
    public String toString() {
        return routeTitle + (routeDescription == null ? "" : routeDescription);
    }
}
