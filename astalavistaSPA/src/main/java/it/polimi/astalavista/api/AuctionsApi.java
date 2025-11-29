package it.polimi.astalavista.api;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import it.polimi.astalavista.dto.AuctionDTO;
import it.polimi.astalavista.mapper.AuctionMapper;
import it.polimi.astalavista.model.Auction;
import it.polimi.astalavista.model.Offer;
import it.polimi.astalavista.model.User;
import it.polimi.astalavista.repository.UserRepository;
import it.polimi.astalavista.service.AuctionService;
import it.polimi.astalavista.service.OfferService;

@RestController
@RequestMapping("/api/auctions")
public class AuctionsApi {

    private final UserRepository userRepository;
    private final AuctionService auctionService;
    private final OfferService offerService;
    private final AuctionMapper auctionMapper;

    public AuctionsApi(
        UserRepository userRepository,
        AuctionService auctionService,
        OfferService offerService,
        AuctionMapper auctionMapper
    ) {
        this.userRepository = userRepository;
        this.auctionService = auctionService;
        this.offerService = offerService;
        this.auctionMapper = auctionMapper;
    }

    private User me(@AuthenticationPrincipal UserDetails ud) {
        return userRepository.findByUsername(ud.getUsername()).orElseThrow();
    }

    @GetMapping("/open")
    public List<AuctionDTO> getOpenAuctions(@AuthenticationPrincipal UserDetails ud) {
        User user = me(ud);

        return auctionService.getOpenAuctions(user).stream()
            .map(auctionMapper::toDto)
            .toList();
    }

    @GetMapping("/closed")
    public List<AuctionDTO> getClosedAuctions(@AuthenticationPrincipal UserDetails ud) {
        User user = me(ud);

        return auctionService.getClosedAuctions(user).stream()
            .map(auctionMapper::toDto)
            .toList();
    }

    @GetMapping("/won")
    public List<AuctionDTO> getWonAuctions(@AuthenticationPrincipal UserDetails ud) {
        User user = me(ud);

        return auctionService.getAllAuctionsWonByUser(user).stream()
            .map(auctionMapper::toDto)
            .toList();
    }

    @GetMapping("/{id}")
    public AuctionDTO getAuctionById(@PathVariable Integer id, @AuthenticationPrincipal UserDetails ud) {
        Auction auction = auctionService.getAuctionById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Auction not found"));

        return auctionMapper.toDto(auction);
    }

    @PostMapping("/close")
    public void closeAuction(@RequestParam int auctionId) {
        Auction auction = auctionService.getAuctionById(auctionId).get();

        auctionService.closeAuction(auction);
    }

    @PostMapping("/placeOffer")
    public void closeAuction(@RequestParam int auctionId, @RequestParam float offer, @AuthenticationPrincipal UserDetails ud) {
        Auction auction = auctionService.getAuctionById(auctionId).get();
        Optional<Offer> higestOffer = offerService.getLastOfferByAuction(auction);
        double bidStep = auction.getBidStep();

        if (offer < auction.getStartPrice()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Offerta minima: € " + higestOffer.get().getPrice() + bidStep);
        }

        if (higestOffer.isPresent() && offer < higestOffer.get().getPrice() + bidStep) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Offerta minima: € " + higestOffer.get().getPrice() + bidStep);
        }

        User user = me(ud);
        offerService.placeOffer(user, auction, offer);
    }
}
