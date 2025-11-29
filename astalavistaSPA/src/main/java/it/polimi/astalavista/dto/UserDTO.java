package it.polimi.astalavista.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserDTO (
    String name,
    String surname,
    String username,
    String street,
    String city,
    String postalCode
) {}
