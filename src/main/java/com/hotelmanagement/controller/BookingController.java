
package com.hotelmanagement.controller;

import com.hotelmanagement.dto.BookingRequest;
import com.hotelmanagement.model.Booking;
import com.hotelmanagement.model.UserAccount;
import com.hotelmanagement.repository.UserAccountRepository;
import com.hotelmanagement.service.BookingService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final UserAccountRepository userRepository;

    public BookingController(
            BookingService bookingService,
            UserAccountRepository userRepository) {
        this.bookingService = bookingService;
        this.userRepository = userRepository;
    }

    private UserAccount currentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please log in"
            );
        }

        return userRepository
                .findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Account not found"
                ));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Booking createBooking(
            @Valid @RequestBody BookingRequest request,
            Authentication authentication) {

        UserAccount user = currentUser(authentication);

        request.setGuestName(user.getName());
        request.setGuestEmail(user.getEmail());

        return bookingService.createBooking(
                request,
                user.getEmail()
        );
    }

    @GetMapping
    public List<Booking> myBookings(Authentication authentication) {
        return bookingService.getMyBookings(
                currentUser(authentication).getEmail()
        );
    }

    @GetMapping("/{id}")
    public Booking getBooking(
            @PathVariable String id,
            Authentication authentication) {

        return bookingService.getMyBooking(
                id,
                currentUser(authentication).getEmail()
        );
    }

    @PatchMapping("/{id}/cancel")
    public Booking cancelBooking(
            @PathVariable String id,
            Authentication authentication) {

        return bookingService.cancelBooking(
                id,
                currentUser(authentication).getEmail()
        );
    }
}
