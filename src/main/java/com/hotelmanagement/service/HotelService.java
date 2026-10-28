
package com.hotelmanagement.service;

import com.hotelmanagement.model.Hotel;
import com.hotelmanagement.repository.HotelRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class HotelService {

    private final HotelRepository hotelRepository;

    public HotelService(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    public List<Hotel> getAllHotels(String city) {
        if (city != null && !city.isBlank()) {
            return hotelRepository
                    .findByCityContainingIgnoreCaseAndActiveTrue(city.trim());
        }

        return hotelRepository.findByActiveTrue();
    }

    public Hotel getHotelById(String id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Hotel not found"));

        if (!hotel.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Hotel not found");
        }

        return hotel;
    }
}
