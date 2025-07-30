package it.polimi.astalavista.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.polimi.astalavista.model.Country;
import it.polimi.astalavista.repository.CountryRepository;

@Service
public class CountryService {
    
    @Autowired
    private CountryRepository countryRepository;

    public List<Country> getAllCountries() {
        return countryRepository.findAllByOrderByNameAsc();
    }
}
