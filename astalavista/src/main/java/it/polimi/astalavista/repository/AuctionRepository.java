package it.polimi.astalavista.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import it.polimi.astalavista.model.Auction;
import it.polimi.astalavista.model.User;

public interface AuctionRepository extends JpaRepository<Auction, Integer> {

    List<Auction> findByIsClosedOrderByEndDateDesc(String isClosed);

    @Query("SELECT a FROM Auction a WHERE a.user = :user AND a.isClosed = 'N'")
    List<Auction> findOpenAuctionsByUser(@Param("user") User user);

    @Query("SELECT a FROM Auction a WHERE a.user = :user AND a.isClosed = 'Y'")
    List<Auction> findClosedAuctionsByUser(@Param("user") User user);

    @Query("""
        SELECT DISTINCT art.auction FROM Article art
        WHERE art.auction.isClosed = 'N'
        AND (
            art.name LIKE %:keyword%
            OR art.description LIKE %:keyword%
        )
        ORDER BY art.auction.endDate DESC
    """)
    List<Auction> findOpenAuctionsByKeyword(@Param("keyword") String keyword);
}
