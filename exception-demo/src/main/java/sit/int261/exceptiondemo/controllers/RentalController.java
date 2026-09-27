package sit.int261.exceptiondemo.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sit.int261.exceptiondemo.dtos.RentalDto;
import sit.int261.exceptiondemo.entities.Rental;
import sit.int261.exceptiondemo.services.RentalService;

@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class RentalController {
    private final RentalService rentalService;

    @PostMapping
    public ResponseEntity<Rental> createRental(@RequestBody RentalDto rentalDto) {
        Rental rental = rentalService.createRental(rentalDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(rental);
    }
}
