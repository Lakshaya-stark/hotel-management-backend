
package com.hotelmanagement.config;

import com.hotelmanagement.model.Hotel;
import com.hotelmanagement.repository.HotelRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedHotels(HotelRepository hotelRepository) {
        return args -> {
            if (hotelRepository.count() == 0) {
                List<Hotel> hotels = List.of(
                    new Hotel(
                        "The Grand Palace",
                        "A luxurious hotel with elegant rooms and excellent service.",
                        "Jaipur",
                        "MI Road, Jaipur",
                        4500,
                        4.8,
                        "https://images.unsplash.com/photo-1566073771259-6a8506099945",
                        List.of("WiFi", "Swimming Pool", "Restaurant", "Spa"),
                        true
                    ),
                    new Hotel(
                        "Mountain View Resort",
                        "A peaceful mountain resort surrounded by beautiful scenery.",
                        "Manali",
                        "Old Manali, Himachal Pradesh",
                        3200,
                        4.6,
                        "https://images.unsplash.com/photo-1511895426328-dc8714191300",
                        List.of("WiFi", "Mountain View", "Restaurant", "Parking"),
                        true
                    ),
                    new Hotel(
                        "City Comfort Inn",
                        "A comfortable and affordable stay for city travellers.",
                        "Delhi",
                        "Connaught Place, New Delhi",
                        1800,
                        4.2,
                        "https://images.unsplash.com/photo-1564501049412-61c2a3083791",
                        List.of("WiFi", "Air Conditioning", "Parking"),
                        true
                    )
                );

                hotelRepository.saveAll(hotels);
            }
        };
    }
}
