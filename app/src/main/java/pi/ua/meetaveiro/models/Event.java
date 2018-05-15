package pi.ua.meetaveiro.models;

import java.io.Serializable;

/**
 * Created by fabio on 15-05-2018.
 */

public class Event implements Serializable{
    private double price;
    private String title;

    private Double longitude;
    private Double latitude;

    private String location;

    public Event(double price, String title, Double latitude, Double longitude, String location) {
        this.price = price;
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
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

    @Override
    public String toString() {
        return "Event{" +
                "price=" + price +
                ", title='" + title + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", location='" + location + '\'' +
                '}';
    }
}
