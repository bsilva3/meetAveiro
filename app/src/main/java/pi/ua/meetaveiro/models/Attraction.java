package pi.ua.meetaveiro.models;

import android.graphics.Bitmap;
import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;

/**
 * A simple shared tourist attraction class to easily pass data around.
 */
public class Attraction {
    private String name;
    private String description;
    private String longDescription;
    private Uri imageUrl;
    private Uri secondaryImageUrl;
    private LatLng location;
    private String city;

    private Bitmap image;
    private Bitmap secondaryImage;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public Uri getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(Uri imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Uri getSecondaryImageUrl() {
        return secondaryImageUrl;
    }

    public void setSecondaryImageUrl(Uri secondaryImageUrl) {
        this.secondaryImageUrl = secondaryImageUrl;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public Bitmap getSecondaryImage() {
        return secondaryImage;
    }

    public void setSecondaryImage(Bitmap secondaryImage) {
        this.secondaryImage = secondaryImage;
    }

    public Attraction(){}

    public Attraction(String name, String description, String longDescription, Uri imageUrl,
                      Uri secondaryImageUrl, LatLng location, String city) {
        this.name = name;
        this.description = description;
        this.longDescription = longDescription;
        this.imageUrl = imageUrl;
        this.secondaryImageUrl = secondaryImageUrl;
        this.location = location;
        this.city = city;
    }
}