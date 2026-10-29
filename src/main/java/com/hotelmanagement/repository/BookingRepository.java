
package com.hotelmanagement.repository;

import com.hotelmanagement.model.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface BookingRepository extends MongoRepository<Booking, String> {

    List<Booking> findByGuestEmailIgnoreCaseOrderByCreatedAtDesc(
            String guestEmail
    );

    List<Booking> findByHotelId(String hotelId);

    List<Booking> findByHotelIdAndStatusInAndCheckInBeforeAndCheckOutAfter(
            String hotelId,
            Collection<String> statuses,
            LocalDate checkOut,
            LocalDate checkIn
    );
}
