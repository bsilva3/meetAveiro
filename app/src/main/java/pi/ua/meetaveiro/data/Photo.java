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
    private String description;
    private String latitude;
    private String longitude;
    private LatLng latLngLocation;
    private Bitmap imgBitmap;
    //the photo id returned from the server (it is unique for every image)
    private int id;
    private String conceptId;

    public Photo() { }

    public Photo(String concept, String description, LatLng latLng, String date, Bitmap bitmap) {
        this.concept = concept;
        this.description = description;
        this.latLngLocation = latLng;
        this.date = date;
        this.imgBitmap = bitmap;
    }

    public Photo(String concept, String description, String date, LatLng latLng,
                 int id, String conceptId, Bitmap bitmap) {
        this.description = description;
        this.date = date;
        this.concept = concept;
        this.latLngLocation = latLng;
        this.id = id;
        this.conceptId = conceptId;
        this.imgBitmap = bitmap;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getConceptId() {
        return conceptId;
    }

    public void setConceptId(String conceptId) {
        this.conceptId = conceptId;
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

    @Override
    public String toString() {
        return "Photo{" +
                "img='" + img + '\'' +
                ", date='" + date + '\'' +
                ", concept='" + concept + '\'' +
                ", description='" + description + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", latLngLocation=" + latLngLocation +
                ", imgBitmap=" + imgBitmap +
                ", id=" + id +
                ", conceptId='" + conceptId + '\'' +
                '}';
    }
}
