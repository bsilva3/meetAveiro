package pi.ua.meetaveiro.data;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by fabio on 18-05-2018.
 */

public class Photo implements ClusterItem {
    private String img;
    private String date;
    private String concept;
    private String latitude;
    private String longitude;
    private LatLng latLngLocation;
    private Bitmap imgBitmap;

    public Photo() { }

    public Photo(String img, String date, String concept, String latitude, String longitude) {
        this.img = img;
        this.date = date;
        this.concept = concept;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getConcept() {
        return concept;
    }

    public void setConcept(String concept) {
        this.concept = concept;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public LatLng getLatLngLocation() {
        return latLngLocation;
    }

    public void setLatLngLocation(LatLng latLngLocation) {
        this.latLngLocation = latLngLocation;
    }

    public Bitmap getImgBitmap() {
        return imgBitmap;
    }

    public void setImgBitmap(Bitmap imgBitmap) {
        this.imgBitmap = imgBitmap;
    }

    @Override
    public LatLng getPosition() {
        return latLngLocation;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getSnippet() {
        return null;
    }
}
