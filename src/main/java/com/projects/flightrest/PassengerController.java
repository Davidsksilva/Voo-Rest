package com.projects.flightrest;

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
public class PassengerController {

    private final PassengerRepository passenger_repo;
    private final FlightRepository flight_repo;
    private final PassengerResourceAssembler passenger_assembler;

    PassengerController(PassengerRepository passenger_repo,FlightRepository flight_repo, PassengerResourceAssembler passenger_assembler){

        this.passenger_repo = passenger_repo;
        this.flight_repo = flight_repo;
        this.passenger_assembler = passenger_assembler;
    }

    @GetMapping(value = "/voos/{id_flight}/passageiros", produces = "application/json; charset=UTF-8")
    public Resources<Resource<Passenger>> allFlightPassengers(@PathVariable Long id_flight){

        List<Passenger> passengers = passenger_repo.findPassengersByFlight_Id(id_flight);
        List<Resource<Passenger>> passengers_resource;

        passengers_resource = passengers.stream()
                .map(passenger_assembler::toResource)
                .collect(Collectors.toList());

        return new Resources<>(passengers_resource,
                linkTo(methodOn(PassengerController.class).allFlightPassengers(id_flight)).withSelfRel());
    }

    @GetMapping(value = "/voos/{id_flight}/passageiros/{id_passenger}", produces = "application/json; charset=UTF-8")
    public Resource<Passenger> oneFlightPassenger(@PathVariable("id_flight") Long id_flight,
                                         @PathVariable("id_passenger") Long id_passenger){

        Passenger passenger = passenger_repo.findPassengerByIdAndFlight_Id(id_passenger,id_flight);

        return passenger_assembler.toResource(passenger);
    }

    @PostMapping( value = "/voos/{id_flight}", produces = "application/json; charset=UTF-8")
    ResponseEntity<?> newPassenger(@RequestBody Passenger newPassenger, @PathVariable Long id_flight) throws URISyntaxException {

        Flight flight = flight_repo.findById(id_flight)
                .orElseThrow(() -> new FlightNotFoundException(id_flight));
        newPassenger.setFlight(flight);
        Resource<Passenger> resource = passenger_assembler.toResource(passenger_repo.save(newPassenger));

        return ResponseEntity
                .created(new URI(resource.getId().expand().getHref()))
                .body(resource);
    }

    @DeleteMapping (value = "/voos/{id_flight}/passageiros/{id_passenger}", produces = "application/json; charset=UTF-8")
    ResponseEntity<?> deletePassenger(@PathVariable("id_flight") Long id_flight, @PathVariable("id_passenger") Long id_passenger){
        passenger_repo.deleteById(id_flight);

        return ResponseEntity.noContent().build();

    }



}

