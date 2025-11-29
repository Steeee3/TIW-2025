package it.polimi.astalavista.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import it.polimi.astalavista.model.Address;
import it.polimi.astalavista.model.Country;

public interface AddressRepository extends JpaRepository<Address, Integer> {

    @Query("SELECT a FROM Address a WHERE a.city = :city AND a.postalCode = :postalCode AND a.street = :street AND a.country = :country")
    Optional<Address> findExactMatch(
        @Param("city") String city,
        @Param("postalCode") String postalCode,
        @Param("street") String street,
        @Param("country") Country country
    );
}
