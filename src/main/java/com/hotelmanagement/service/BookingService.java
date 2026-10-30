
package com.hotelmanagement.service;

import com.hotelmanagement.dto.BookingRequest;
import com.hotelmanagement.model.Booking;
import com.hotelmanagement.model.Hotel;
import com.hotelmanagement.model.Room;
import com.hotelmanagement.repository.BookingRepository;
import com.hotelmanagement.repository.HotelRepository;
import com.hotelmanagement.repository.RoomRepository;

import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    private static final List<String> ACTIVE_STATUSES =
            List.of("PENDING", "CONFIRMED");

    private static final long LOCK_DURATION_SECONDS = 120;

    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final MongoTemplate mongoTemplate;

    public BookingService(
            BookingRepository bookingRepository,
            HotelRepository hotelRepository,
            RoomRepository roomRepository,
            MongoTemplate mongoTemplate
    ) {
        this.bookingRepository = bookingRepository;
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public Booking createBooking(
            BookingRequest request,
            String userEmail
    ) {
        validateRequest(request);

        String roomId = request.getRoomId().trim();

        Room room = roomRepository.findByIdAndActiveTrue(roomId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Room not found or inactive"
                ));

        if (request.getHotelId() != null
                && !request.getHotelId().isBlank()
                && !request.getHotelId().trim().equals(room.getHotelId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Selected room does not belong to the supplied hotel"
            );
        }

        if (request.getGuests() > room.getCapacity()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This room can accommodate only "
                            + room.getCapacity() + " guests"
            );
        }

        String hotelId = room.getHotelId();

        Hotel hotel = hotelRepository.findById(hotelId)
                .filter(Hotel::isActive)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Hotel not found or inactive"
                ));

        String lockToken = acquireRoomLock(roomId);

        try {
            List<Booking> overlaps =
                    bookingRepository
                            .findByRoomIdAndStatusInAndCheckInLessThanAndCheckOutGreaterThan(
                                    roomId,
                                    ACTIVE_STATUSES,
                                    request.getCheckOut(),
                                    request.getCheckIn()
                            );

            if (!overlaps.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "This room is already booked for the selected dates"
                );
            }

            long nights = ChronoUnit.DAYS.between(
                    request.getCheckIn(),
                    request.getCheckOut()
            );

            double totalPrice = room.getPricePerNight() * nights;

            if (!Double.isFinite(totalPrice) || totalPrice <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid room price"
                );
            }

            Booking booking = new Booking();

            booking.setRoomId(room.getId());
            booking.setHotelId(hotel.getId());
            booking.setHotelName(hotel.getName());

            booking.setGuestName(request.getGuestName().trim());
            booking.setGuestEmail(
                    userEmail.trim().toLowerCase()
            );

            booking.setCheckIn(request.getCheckIn());
            booking.setCheckOut(request.getCheckOut());
            booking.setGuests(request.getGuests());

            booking.setTotalPrice(totalPrice);
            booking.setStatus("PENDING");
            booking.setCreatedAt(LocalDateTime.now());

            return bookingRepository.save(booking);

        } finally {
            releaseRoomLock(roomId, lockToken);
        }
    }

    private String acquireRoomLock(String roomId) {

        String token = UUID.randomUUID().toString();

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime lockExpiry =
                now.plusSeconds(LOCK_DURATION_SECONDS);

        Criteria availableLock = new Criteria().orOperator(
                Criteria.where("reservationLockUntil").lte(now),
                Criteria.where("reservationLockUntil").exists(false)
        );

        Query query = new Query(
                new Criteria().andOperator(
                        Criteria.where("_id").is(roomId),
                        Criteria.where("active").is(true),
                        availableLock
                )
        );

        Update update = new Update()
                .set("reservationLockToken", token)
                .set("reservationLockUntil", lockExpiry);

        Room lockedRoom = mongoTemplate.findAndModify(
                query,
                update,
                FindAndModifyOptions.options().returnNew(true),
                Room.class
        );

        if (lockedRoom == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This room is currently being reserved. Please try again."
            );
        }

        return token;
    }

    private void releaseRoomLock(
            String roomId,
            String token
    ) {
        Query query = new Query(
                new Criteria().andOperator(
                        Criteria.where("_id").is(roomId),
                        Criteria.where("reservationLockToken").is(token)
                )
        );

        Update update = new Update()
                .unset("reservationLockToken")
                .unset("reservationLockUntil");

        mongoTemplate.updateFirst(query, update, Room.class);
    }

    public List<Booking> getMyBookings(String userEmail) {
        return bookingRepository
                .findByGuestEmailIgnoreCaseOrderByCreatedAtDesc(
                        userEmail
                );
    }

    public Booking getMyBooking(
            String bookingId,
            String userEmail
    ) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Booking not found"
                ));

        if (!booking.getGuestEmail().equalsIgnoreCase(userEmail)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Booking not found"
            );
        }

        return booking;
    }

    public Booking cancelBooking(
            String bookingId,
            String userEmail
    ) {
        Booking booking = getMyBooking(bookingId, userEmail);

        if ("CANCELLED".equalsIgnoreCase(booking.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This booking is already cancelled"
            );
        }

        if (booking.getCheckIn().isBefore(LocalDate.now())
                || booking.getCheckIn().isEqual(LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Bookings cannot be cancelled on or after check-in"
            );
        }

        if (!ACTIVE_STATUSES.contains(booking.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This booking cannot be cancelled"
            );
        }

        booking.setStatus("CANCELLED");

        return bookingRepository.save(booking);
    }

    private void validateRequest(BookingRequest request) {

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Booking details are required"
            );
        }

        if (request.getRoomId() == null
                || request.getRoomId().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Room ID is required"
            );
        }

        if (request.getGuestName() == null
                || request.getGuestName().trim().length() < 2
                || request.getGuestName().trim().length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Guest name must contain between 2 and 100 characters"
            );
        }

        String email = request.getGuestEmail();

        if (email == null
                || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                || email.length() > 254) {
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
