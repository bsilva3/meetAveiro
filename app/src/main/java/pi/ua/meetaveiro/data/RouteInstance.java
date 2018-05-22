package pi.ua.meetaveiro.data;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.Marker;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fabio on 15-05-2018.
 */

public class RouteInstance {
    private Date startDate;
    private Date endDate;
    private Route route;
    private Map<Marker, Bitmap> routeMarkers;
    private List<Photo> bitmaps;

    private int idInstance;

    public RouteInstance(Date startDate, Date endDate, Route route, Map<Marker, Bitmap> routeMarkers) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.route = route;
        this.routeMarkers = routeMarkers;
    }

    public RouteInstance(Date startDate, Date endDate, Route route, List<Photo> bitmaps) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.route = route;
        this.bitmaps = bitmaps;
    }

    public RouteInstance(){
        routeMarkers = new HashMap<>();
    }

    public RouteInstance(Route r){
        routeMarkers = new HashMap<>();
        this.route = r;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public Map<Marker, Bitmap> getRouteMarkers() {
        return routeMarkers;
    }

    public void setRouteMarkers(Map<Marker, Bitmap> routeMarkers) {
        this.routeMarkers = routeMarkers;
    }

    public List<Photo> getBitmaps() {
        return bitmaps;
    }

    public void setBitmaps(List<Photo> bitmaps) {
        this.bitmaps = bitmaps;
    }

    public int getIdInstance() {
        return idInstance;
    }

    public void setIdInstance(int idInstance) {
        this.idInstance = idInstance;
    }
}
