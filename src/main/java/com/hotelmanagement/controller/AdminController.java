
package com.hotelmanagement.controller;

import com.hotelmanagement.model.Booking;
import com.hotelmanagement.model.Hotel;
import com.hotelmanagement.repository.BookingRepository;
import com.hotelmanagement.repository.HotelRepository;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(
    origins = "http://localhost:5173",
    allowCredentials = "true"
)
public class AdminController {

    private final HotelRepository hotelRepository;
    private final BookingRepository bookingRepository;

    public AdminController(
            HotelRepository hotelRepository,
            BookingRepository bookingRepository) {
        this.hotelRepository = hotelRepository;
        this.bookingRepository = bookingRepository;
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        List<Booking> bookings = bookingRepository.findAll();
        List<Hotel> hotels = hotelRepository.findAll();

        long pending = bookings.stream()
                .filter(b -> "PENDING".equalsIgnoreCase(b.getStatus()))
                .count();

        long confirmed = bookings.stream()
                .filter(b -> "CONFIRMED".equalsIgnoreCase(b.getStatus()))
                .count();

        long cancelled = bookings.stream()
                .filter(b -> "CANCELLED".equalsIgnoreCase(b.getStatus()))
                .count();

        long rejected = bookings.stream()
                .filter(b -> "REJECTED".equalsIgnoreCase(b.getStatus()))
                .count();

        double confirmedRevenue = bookings.stream()
                .filter(b -> "CONFIRMED".equalsIgnoreCase(b.getStatus()))
                .mapToDouble(Booking::getTotalPrice)
                .sum();

        return Map.of(
                "totalHotels", hotels.size(),
                "activeHotels", hotels.stream().filter(Hotel::isActive).count(),
                "totalBookings", bookings.size(),
                "pendingBookings", pending,
                "confirmedBookings", confirmed,
                "cancelledBookings", cancelled,
                "rejectedBookings", rejected,
                "confirmedBookingValue", confirmedRevenue
        );
    }

    @GetMapping("/hotels")
    public List<Hotel> getHotels() {
        return hotelRepository.findAll();
    }

    @PostMapping("/hotels")
    @ResponseStatus(HttpStatus.CREATED)
    public Hotel createHotel(@RequestBody Hotel hotel) {
        validateHotel(hotel);

        hotel.setId(null);
        hotel.setActive(true);

        return hotelRepository.save(hotel);
    }

    @PutMapping("/hotels/{id}")
    public Hotel updateHotel(
            @PathVariable String id,
            @RequestBody Hotel request) {
        validateHotel(request);

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Hotel not found"
                ));

        hotel.setName(request.getName().trim());
        hotel.setDescription(request.getDescription());
        hotel.setCity(request.getCity().trim());
        hotel.setAddress(request.getAddress());
        hotel.setPricePerNight(request.getPricePerNight());
        hotel.setRating(request.getRating());
        hotel.setImageUrl(request.getImageUrl());
        hotel.setAmenities(request.getAmenities());

        return hotelRepository.save(hotel);
    }

    @PatchMapping("/hotels/{id}/active")
    public Hotel setHotelActive(
            @PathVariable String id,
            @RequestBody Map<String, Boolean> request) {

        Boolean active = request.get("active");

        if (active == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The active field is required"
            );
        }

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Hotel not found"
                ));

        hotel.setActive(active);
        return hotelRepository.save(hotel);
    }

    @GetMapping("/bookings")
    public List<Booking> getBookings() {
        return bookingRepository.findAll();
    }

    @PatchMapping("/bookings/{id}/status")
    public Booking updateBookingStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> request) {

        String status = request.get("status");

        if (status == null ||
                !Set.of("CONFIRMED", "REJECTED").contains(status)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Status must be CONFIRMED or REJECTED"
            );
        }

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Booking not found"
                ));

        if (!"PENDING".equalsIgnoreCase(booking.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only pending bookings can be reviewed"
            );
        }

        booking.setStatus(status);
        return bookingRepository.save(booking);
    }

    private void validateHotel(Hotel hotel) {
        if (hotel == null ||
                hotel.getName() == null ||
                hotel.getName().isBlank() ||
                hotel.getCity() == null ||
                hotel.getCity().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Hotel name and city are required"
            );
        }

        if (hotel.getPricePerNight() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Price per night must be greater than zero"
            );
        }

        if (hotel.getRating() < 0 || hotel.getRating() > 5) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Rating must be between 0 and 5"
            );
        }
    }
}
