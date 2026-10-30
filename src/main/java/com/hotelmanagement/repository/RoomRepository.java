
package com.hotelmanagement.repository;

import com.hotelmanagement.model.Room;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends MongoRepository<Room, String> {

    List<Room> findByHotelId(String hotelId);

    List<Room> findByHotelIdAndActiveTrue(String hotelId);

    List<Room> findByHotelIdAndRoomTypeIgnoreCaseAndActiveTrue(
            String hotelId,
            String roomType
    );

    Optional<Room> findByIdAndActiveTrue(String id);

    boolean existsByHotelIdAndRoomNumberIgnoreCase(
            String hotelId,
            String roomNumber
    );

    boolean existsByHotelIdAndRoomNumberIgnoreCaseAndIdNot(
            String hotelId,
            String roomNumber,
            String id
    );
}
