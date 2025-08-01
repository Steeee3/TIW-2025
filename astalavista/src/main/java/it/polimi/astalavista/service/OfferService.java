package it.polimi.astalavista.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.polimi.astalavista.model.Auction;
import it.polimi.astalavista.model.Offer;
import it.polimi.astalavista.model.User;
import it.polimi.astalavista.repository.OfferRepository;

@Service
public class OfferService {
    
    @Autowired
    private OfferRepository offerRepository;

    public Optional<Offer> getLastOfferByAuction(Auction auction) {
        return offerRepository.findFirstByAuctionOrderByTimestampDesc(auction);
    }

    public Optional<User> getWinnerByAuction(Auction auction) {
        Optional<Offer> offer = offerRepository.findFirstByAuctionOrderByTimestampDesc(auction);

        return offer.map(o -> o.getUser());
    }

    public List<Offer> getOffers(Auction auction) {
        return offerRepository.findByAuctionOrderByTimestampDesc(auction);
    }

    public void placeOffer(User user, Auction auction, float price) {
        Offer offer = new Offer(price, user, auction);

        offerRepository.save(offer);
    }
}
