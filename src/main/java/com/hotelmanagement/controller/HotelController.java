
package com.hotelmanagement.controller;

import com.hotelmanagement.model.Hotel;
import com.hotelmanagement.service.HotelService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@CrossOrigin(origins = "http://localhost:5173")
public class HotelController {

    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping
    public List<Hotel> getAllHotels(
            @RequestParam(required = false) String city) {
        return hotelService.getAllHotels(city);
    }

    @GetMapping("/{id}")
    public Hotel getHotelById(@PathVariable String id) {
        return hotelService.getHotelById(id);
    }
}
