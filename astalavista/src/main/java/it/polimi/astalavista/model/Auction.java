package it.polimi.astalavista.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="auctions")
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "start_price", nullable = false)
    private float startPrice = 0;

    @Column(name = "bid_step", nullable = false)
    private int bidStep = 1;

    @Column(name = "is_closed", nullable = false, columnDefinition = "ENUM('Y','N')")
    private String isClosed = "N";

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Integer getId() {
        return id;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public float getStartPrice() {
        return startPrice;
    }

    public void setStartPrice(float startPrice) {
        this.startPrice = startPrice;
    }

    public int getBidStep() {
        return bidStep;
    }

    public void setBidStep(int bidStep) {
        this.bidStep = bidStep;
    }

    public boolean isClosed() {
        return "Y".equals(isClosed) || LocalDateTime.now().isAfter(endDate);
    }

    public void close() {
        isClosed = "Y";
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void start() {
        startDate = LocalDateTime.now();
    }
}
