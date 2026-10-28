
package com.hotelmanagement.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "hotels")
public class Hotel {

    @Id
    private String id;

    private String name;
    private String description;
    private String city;
    private String address;
    private double pricePerNight;
    private double rating;
    private String imageUrl;
    private List<String> amenities;
    private boolean active;

    public Hotel() {
    }

    public Hotel(String name, String description, String city,
                 String address, double pricePerNight, double rating,
                 String imageUrl, List<String> amenities, boolean active) {
        this.name = name;
        this.description = description;
        this.city = city;
        this.address = address;
        this.pricePerNight = pricePerNight;
        this.rating = rating;
        this.imageUrl = imageUrl;
        this.amenities = amenities;
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<String> getAmenities() {
        return amenities;
    }

    public void setAmenities(List<String> amenities) {
        this.amenities = amenities;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
