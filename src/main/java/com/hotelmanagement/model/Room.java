
package com.hotelmanagement.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "rooms")
@CompoundIndex(
    name = "hotel_room_number_unique",
    def = "{'hotelId': 1, 'roomNumber': 1}",
    unique = true
)
public class Room {

    @Id
    private String id;

    private String hotelId;
    private String roomNumber;
    private String roomType;
    private int capacity;
    private double pricePerNight;
    private List<String> amenities = new ArrayList<>();
    private boolean active = true;

    public Room() {
    }

    public Room(
            String hotelId,
            String roomNumber,
            String roomType,
            int capacity,
            double pricePerNight,
            List<String> amenities
    ) {
        this.hotelId = hotelId;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.capacity = capacity;
        this.pricePerNight = pricePerNight;
        this.amenities = amenities != null
                ? new ArrayList<>(amenities)
                : new ArrayList<>();
        this.active = true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHotelId() {
        return hotelId;
    }

    public void setHotelId(String hotelId) {
        this.hotelId = hotelId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public List<String> getAmenities() {
        return amenities;
    }

    public void setAmenities(List<String> amenities) {
        this.amenities = amenities != null
                ? new ArrayList<>(amenities)
                : new ArrayList<>();
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
