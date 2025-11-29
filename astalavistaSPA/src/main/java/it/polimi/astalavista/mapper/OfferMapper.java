package it.polimi.astalavista.mapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import it.polimi.astalavista.dto.OfferDTO;
import it.polimi.astalavista.model.Offer;

@Component
public class OfferMapper {

    private final UserMapper userMapper;

    public OfferMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }
    
    public OfferDTO toDto(Offer offer) {
        return new OfferDTO(
            userMapper.toDto(offer.getUser()),
            offer.getPrice(),
            offer.getTimestamp()
        );
    }

    public List<OfferDTO> toDto(List<Offer> offers) {
        List<OfferDTO> offerDTO = new ArrayList<>();

        for (Offer offer : offers) {
            offerDTO.add(toDto(offer));
        }

        return offerDTO;
    }
}
