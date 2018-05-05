package pi.ua.meetaveiro.models;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

import java.util.List;
import java.util.Map;

import pi.ua.meetaveiro.R;

public class Route implements Parcelable {
    private static int COUNTER = 0;
    private Polyline routePath;
    private List<LatLng> routePathPoints; //when we cant use polyline just for the parcelable
    private Map<Marker, Bitmap> routeMarkers;
    private String routeTitle = "Unnamed"; //default
    private String routeDescription = ""; //default

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

    public Route() {
    }

    //for parcelable
    public Route(Parcel in) {
        routeTitle = in.readString();
        routeDescription = in.readString();
        routePathPoints = in.readParcelable(getClass().getClassLoader());
        routeMarkers = in.readParcelable(getClass().getClassLoader());
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Write data in any order
        dest.writeString(routeTitle);
        dest.writeString(routeDescription);
        dest.writeList(routePath.getPoints());
        dest.writeMap(routeMarkers);
    }

    // de-serialize the object
    public static final Parcelable.Creator<Attraction> CREATOR = new Parcelable.Creator<Attraction>() {
        public Attraction createFromParcel(Parcel in) {
            return new Attraction(in);
        }

        @Override
        public Attraction[] newArray(int size) {
            return new Attraction[0];
        }
    };

    public void setRoutePath(Polyline routePath) {
        this.routePath = routePath;
    }

    public void setRouteMarkers(Map<Marker, Bitmap> routeMarkers) {
        this.routeMarkers = routeMarkers;
    }
}
