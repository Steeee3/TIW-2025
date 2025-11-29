package it.polimi.astalavista.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import it.polimi.astalavista.model.Country;

public interface CountryRepository extends JpaRepository<Country, Integer> {
    Optional<Country> findByName(String name);
    List<Country> findAllByOrderByNameAsc();
}
