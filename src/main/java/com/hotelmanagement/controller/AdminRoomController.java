
package com.hotelmanagement.controller;

import com.hotelmanagement.dto.RoomRequest;
import com.hotelmanagement.model.Room;
import com.hotelmanagement.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/rooms")
public class AdminRoomController {

    private final RoomService roomService;

    public AdminRoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public List<Room> getAllRooms() {
        return roomService.getAllRooms();
    }

    @GetMapping("/hotel/{hotelId}")
    public List<Room> getRoomsByHotel(
            @PathVariable String hotelId
    ) {
        return roomService.getRoomsByHotel(hotelId);
    }

    @GetMapping("/{id}")
    public Room getRoomById(@PathVariable String id) {
        return roomService.getRoomById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Room createRoom(
            @Valid @RequestBody RoomRequest request
    ) {
        return roomService.createRoom(request);
    }

    @PutMapping("/{id}")
    public Room updateRoom(
            @PathVariable String id,
            @Valid @RequestBody RoomRequest request
    ) {
        return roomService.updateRoom(id, request);
    }

    @PatchMapping("/{id}/active")
    public Room updateRoomStatus(
            @PathVariable String id,
            @RequestBody Map<String, Boolean> request
    ) {
        Boolean active = request.get("active");

        if (active == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Active status is required"
            );
        }

        return roomService.updateRoomStatus(id, active);
    }
}
