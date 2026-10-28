
package com.hotelmanagement.repository;

import com.hotelmanagement.model.Hotel;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface HotelRepository extends MongoRepository<Hotel, String> {

    List<Hotel> findByActiveTrue();

    List<Hotel> findByCityContainingIgnoreCaseAndActiveTrue(String city);
}
