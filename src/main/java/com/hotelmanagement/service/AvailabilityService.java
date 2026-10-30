
package com.hotelmanagement.service;

import com.hotelmanagement.model.Booking;
import com.hotelmanagement.model.Room;
import com.hotelmanagement.repository.BookingRepository;
import com.hotelmanagement.repository.RoomRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class AvailabilityService {

    private static final List<String> ACTIVE_STATUSES =
            List.of("PENDING", "CONFIRMED");

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    public AvailabilityService(
            RoomRepository roomRepository,
            BookingRepository bookingRepository
    ) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    public List<Room> getAvailableRooms(
            String hotelId,
            LocalDate checkIn,
            LocalDate checkOut,
            int guests
    ) {
        validateDates(checkIn, checkOut);

        if (guests < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least one guest is required"
            );
        }

        List<Room> rooms =
                roomRepository.findByHotelIdAndActiveTrue(hotelId);

        return rooms.stream()
                .filter(room -> room.getCapacity() >= guests)
                .filter(room -> isRoomAvailable(
                        room.getId(),
                        checkIn,
                        checkOut
                ))
                .toList();
    }

    public boolean isRoomAvailable(
            String roomId,
            LocalDate checkIn,
            LocalDate checkOut
    ) {
        validateDates(checkIn, checkOut);

        Room room = roomRepository.findByIdAndActiveTrue(roomId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Active room not found"
                ));

        List<Booking> overlappingBookings =
                bookingRepository
                        .findByRoomIdAndStatusInAndCheckInLessThanAndCheckOutGreaterThan(
                                room.getId(),
                                ACTIVE_STATUSES,
                                checkOut,
                                checkIn
                        );

        return overlappingBookings.isEmpty();
    }

    private void validateDates(
            LocalDate checkIn,
            LocalDate checkOut
    ) {
        if (checkIn == null || checkOut == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Check-in and check-out dates are required"
            );
        }

        if (!checkIn.isBefore(checkOut)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Check-out must be after check-in"
            );
        }

        if (checkIn.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Check-in date cannot be in the past"
            );
        }
    }
}
