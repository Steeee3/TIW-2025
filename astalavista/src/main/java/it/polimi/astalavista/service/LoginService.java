package it.polimi.astalavista.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import it.polimi.astalavista.exceptions.CountryNotAvailableException;
import it.polimi.astalavista.model.Address;
import it.polimi.astalavista.model.Country;
import it.polimi.astalavista.model.User;
import it.polimi.astalavista.repository.AddressRepository;
import it.polimi.astalavista.repository.CountryRepository;
import it.polimi.astalavista.repository.UserRepository;

@Service
public class LoginService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean authenticate(String username, String rawPassword) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            return passwordEncoder.matches(rawPassword, user.getPassword());
        }

        return false;
    }

    public boolean register (
        String username,
        String rawPassword,
        String name,
        String surname,
        String country,
        String city,
        String street,
        int postalCode
        ) throws CountryNotAvailableException {

        if (userRepository.findByUsername(username).isPresent()) {
            return false;
        }

        User user = new User();

        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setName(name);
        user.setSurname(surname);

        Optional<Country> countryOptional = countryRepository.findByName(country);
        if (!countryOptional.isPresent()) {
            throw new CountryNotAvailableException();
        }

        Optional<Address> addressOptional = addressRepository.findExactMatch(city, postalCode, street, countryOptional.get());
        Address address;

        if (addressOptional.isPresent()) {
            address = addressOptional.get();
        } else {
            address = new Address(city, postalCode, street, countryOptional.get());
            addressRepository.save(address);
        }

        user.setAddress(address);
        userRepository.save(user);
        return true;
    }
}
