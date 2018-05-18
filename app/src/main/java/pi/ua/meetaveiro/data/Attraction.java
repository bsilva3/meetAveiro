package pi.ua.meetaveiro.data;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

/**
 * A simple shared tourist attraction class to easily pass data around.
 */
public class Attraction implements Parcelable {
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

    public Attraction() {
    }

    //for parcelable
    public Attraction(Parcel in) {
        try {
            name = in.readString();
            description = in.readString();
            longDescription = in.readString();
            imageUrl = in.readParcelable(getClass().getClassLoader());
            secondaryImage = in.readParcelable(getClass().getClassLoader());
            location = in.readParcelable(getClass().getClassLoader());
            city = in.readString();
        } catch (Exception e) {
            Log.e("parcelable error", "There were some problemas creating parcelable object (some elements may be null)");
        }
    }

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

    public Attraction(String name, LatLng location) {
        this.name = name;
        this.location = location;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Write data in any order
        dest.writeString(name);
        dest.writeString(description);
        dest.writeParcelable(imageUrl, flags);
        dest.writeParcelable(secondaryImageUrl, flags);
        dest.writeParcelable(location, flags);
        dest.writeString(city);
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
}
