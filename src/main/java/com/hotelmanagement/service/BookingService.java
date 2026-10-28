
package com.hotelmanagement.service;

import com.hotelmanagement.dto.BookingRequest;
import com.hotelmanagement.model.Booking;
import com.hotelmanagement.model.Hotel;
import com.hotelmanagement.repository.BookingRepository;
import com.hotelmanagement.repository.HotelRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BookingService {

    private static final List<String> ACTIVE_STATUSES =
            List.of("PENDING", "CONFIRMED");

    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;

    public BookingService(
            BookingRepository bookingRepository,
            HotelRepository hotelRepository) {
        this.bookingRepository = bookingRepository;
        this.hotelRepository = hotelRepository;
    }

    public Booking createBooking(BookingRequest request) {
        validateRequest(request);

        String hotelId = request.getHotelId().trim();

        Hotel hotel = hotelRepository.findById(hotelId)
                .filter(Hotel::isActive)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Hotel not found or inactive"));

        List<Booking> overlappingBookings =
                bookingRepository
                        .findByHotelIdAndStatusInAndCheckInBeforeAndCheckOutAfter(
                                hotelId,
                                ACTIVE_STATUSES,
                                request.getCheckOut(),
                                request.getCheckIn()
                        );

        if (!overlappingBookings.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "These dates are unavailable for this hotel. Please choose different dates."
            );
        }

        long nights = ChronoUnit.DAYS.between(
                request.getCheckIn(),
                request.getCheckOut()
        );

        Booking booking = new Booking();
        booking.setHotelId(hotel.getId());
        booking.setHotelName(hotel.getName());
        booking.setGuestName(request.getGuestName().trim());
        booking.setGuestEmail(request.getGuestEmail().trim());
        booking.setCheckIn(request.getCheckIn());
        booking.setCheckOut(request.getCheckOut());
        booking.setGuests(request.getGuests());
        booking.setTotalPrice(hotel.getPricePerNight() * nights);
        booking.setStatus("PENDING");
        booking.setCreatedAt(LocalDateTime.now());

        return bookingRepository.save(booking);
    }

    public List<Booking> getBookingsByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email is required"
            );
        }

        return bookingRepository.findByGuestEmailIgnoreCase(email.trim());
    }

    private void validateRequest(BookingRequest request) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Booking details are required"
            );
        }

        if (request.getHotelId() == null ||
                request.getHotelId().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Hotel ID is required"
            );
        }

        if (request.getGuestName() == null ||
                request.getGuestName().trim().length() < 2 ||
                request.getGuestName().trim().length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Guest name must contain between 2 and 100 characters"
            );
        }

        String email = request.getGuestEmail();

        if (email == null ||
                !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") ||
                email.length() > 254) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A valid email address is required"
            );
        }

        LocalDate checkIn = request.getCheckIn();
        LocalDate checkOut = request.getCheckOut();

        if (checkIn == null || checkOut == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Check-in and check-out dates are required"
            );
        }

        if (checkIn.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Check-in cannot be in the past"
            );
        }

        if (!checkOut.isAfter(checkIn)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Check-out must be after check-in"
            );
        }

        if (request.getGuests() < 1 || request.getGuests() > 20) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Guest count must be between 1 and 20"
            );
        }
    }
}
