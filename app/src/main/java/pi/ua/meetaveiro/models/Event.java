package pi.ua.meetaveiro.models;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by fabio on 15-05-2018.
 */

public class Event {
    private double price;
    private String title;
    private LatLng coordinates;
    private String location;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Event(double price, String title, LatLng coordinates, String location) {
        this.price = price;
        this.title = title;
        this.coordinates = coordinates;
        this.location = location;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(LatLng coordinates) {
        this.coordinates = coordinates;
    }
}
