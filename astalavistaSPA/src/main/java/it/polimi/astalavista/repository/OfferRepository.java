package it.polimi.astalavista.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import it.polimi.astalavista.model.Auction;
import it.polimi.astalavista.model.Offer;

public interface OfferRepository extends JpaRepository<Offer, Integer> {

    Optional<Offer> findFirstByAuctionOrderByTimestampDesc(Auction auction);
    List<Offer> findByAuctionOrderByTimestampDesc(Auction auction);
}
