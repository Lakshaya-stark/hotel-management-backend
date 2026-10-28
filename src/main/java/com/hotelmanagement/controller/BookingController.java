
package com.hotelmanagement.controller;

import com.hotelmanagement.dto.BookingRequest;
import com.hotelmanagement.model.Booking;
import com.hotelmanagement.service.BookingService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "http://localhost:5173")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Booking createBooking(
            @Valid @RequestBody BookingRequest request) {
        return bookingService.createBooking(request);
    }

    @GetMapping
    public List<Booking> getBookings(
            @RequestParam String email) {
        return bookingService.getBookingsByEmail(email);
    }
}
