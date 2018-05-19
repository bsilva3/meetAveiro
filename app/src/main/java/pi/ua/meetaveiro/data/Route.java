package pi.ua.meetaveiro.data;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Route implements Parcelable, Serializable {
    private int id;
    private Polyline routePath;
    private List<LatLng> routePathPoints; //when we cant use polyline just for the parcelable
    private String routeTitle = "Unnamed"; //default
    private String routeDescription = ""; //default
    private String Type  ="";  //To be used to split into ROUTE or ROUTE INSTANCE (To see the ROUTES WITHOUT MARKERS)

    public Route(String routeTitle, Polyline routePath, String routeDescription, Map<Marker, Bitmap> routeMarkers) {
        this.routeTitle = routeTitle;
        this.routePath = routePath;
        this.routeDescription = routeDescription;
    }

    public Route(String routeTitle, Polyline routePath, Map<Marker, Bitmap> routeMarkers) {
        this.routeTitle = routeTitle;
        this.routePath = routePath;
    }

    public Route(String routeTitle, String routeDescription) {
        this.routeTitle = routeTitle;
        this.routeDescription = routeDescription;
    }

    public Route(String routeTitle) {
        this.routeTitle = routeTitle;
    }

    public Route() {
    }

    //for parcelable
    public Route(Parcel in) {
        routeTitle = in.readString();
        routeDescription = in.readString();
        routePathPoints = in.readParcelable(getClass().getClassLoader());
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }
}
