
package com.hotelmanagement.service;

import com.hotelmanagement.dto.RoomRequest;
import com.hotelmanagement.model.Hotel;
import com.hotelmanagement.model.Room;
import com.hotelmanagement.repository.HotelRepository;
import com.hotelmanagement.repository.RoomRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@Service
public class RoomService {

    private static final Set<String> ROOM_TYPES = Set.of(
            "SINGLE",
            "DOUBLE",
            "DELUXE",
            "SUITE"
    );

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

    public RoomService(
            RoomRepository roomRepository,
            HotelRepository hotelRepository
    ) {
        this.roomRepository = roomRepository;
        this.hotelRepository = hotelRepository;
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public List<Room> getRoomsByHotel(String hotelId) {
        if (!hotelRepository.existsById(hotelId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Hotel not found"
            );
        }

        return roomRepository.findByHotelId(hotelId);
    }

    public Room getRoomById(String id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Room not found"
                ));
    }

    public Room createRoom(RoomRequest request) {
        validateRequest(request);

        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Hotel not found"
                ));

        if (roomRepository.existsByHotelIdAndRoomNumberIgnoreCase(
                request.getHotelId(),
                request.getRoomNumber().trim()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Room number already exists in this hotel"
            );
        }

        Room room = new Room(
                request.getHotelId(),
                request.getRoomNumber().trim(),
                normalizeRoomType(request.getRoomType()),
                request.getCapacity(),
                request.getPricePerNight(),
                request.getAmenities()
        );

        return roomRepository.save(room);
    }

    public Room updateRoom(String id, RoomRequest request) {
        validateRequest(request);

        Room room = getRoomById(id);

        hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Hotel not found"
                ));

        String roomNumber = request.getRoomNumber().trim();

        if (roomRepository.existsByHotelIdAndRoomNumberIgnoreCaseAndIdNot(
                request.getHotelId(),
                roomNumber,
                id
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Room number already exists in this hotel"
            );
        }

        room.setHotelId(request.getHotelId());
        room.setRoomNumber(roomNumber);
        room.setRoomType(normalizeRoomType(request.getRoomType()));
        room.setCapacity(request.getCapacity());
        room.setPricePerNight(request.getPricePerNight());
        room.setAmenities(request.getAmenities());

        return roomRepository.save(room);
    }

    public Room updateRoomStatus(String id, boolean active) {
        Room room = getRoomById(id);
        room.setActive(active);
        return roomRepository.save(room);
    }

    private void validateRequest(RoomRequest request) {
        String roomType = normalizeRoomType(request.getRoomType());

        if (!ROOM_TYPES.contains(roomType)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid room type. Allowed types: SINGLE, DOUBLE, DELUXE, SUITE"
            );
        }
    }

    private String normalizeRoomType(String roomType) {
        return roomType.trim().toUpperCase();
    }
}
