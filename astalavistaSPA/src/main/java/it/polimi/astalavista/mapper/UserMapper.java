package it.polimi.astalavista.mapper;

import org.springframework.stereotype.Component;

import it.polimi.astalavista.dto.UserDTO;
import it.polimi.astalavista.model.Address;
import it.polimi.astalavista.model.User;

@Component
public class UserMapper {
    
    public UserDTO toDto(User user) {
        Address address = user.getAddress();
        return new UserDTO(
            user.getName(), 
            user.getSurname(), 
            user.getUsername(), 
            address.getStreet(), 
            address.getCity(), 
            address.getPostalCode()
        );
    }
}
