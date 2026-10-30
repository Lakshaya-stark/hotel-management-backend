
package com.hotelmanagement.controller;

import com.hotelmanagement.model.Room;
import com.hotelmanagement.service.AvailabilityService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomAvailabilityController {

    private final AvailabilityService availabilityService;

    public RoomAvailabilityController(
            AvailabilityService availabilityService
    ) {
        this.availabilityService = availabilityService;
    }

    @GetMapping("/availability")
    public List<Room> getAvailableRooms(
            @RequestParam String hotelId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate checkIn,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate checkOut,
            @RequestParam(defaultValue = "1") int guests
    ) {
        return availabilityService.getAvailableRooms(
                hotelId,
                checkIn,
                checkOut,
                guests
        );
    }

    @GetMapping("/{roomId}/availability")
    public boolean checkRoomAvailability(
            @PathVariable String roomId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate checkIn,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate checkOut
    ) {
        return availabilityService.isRoomAvailable(
                roomId,
                checkIn,
                checkOut
        );
    }
}
