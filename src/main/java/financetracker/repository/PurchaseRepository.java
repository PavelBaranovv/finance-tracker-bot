package financetracker.repository;

import financetracker.entity.Purchase;
import financetracker.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    
    @Query("SELECT p FROM Purchase p JOIN FETCH p.currency WHERE p.user = :user ORDER BY p.id DESC LIMIT 10")
    List<Purchase> findTop10ByUserOrderByIdDescWithCurrency(@Param("user") User user);

    @EntityGraph(attributePaths = {"currency"})
    List<Purchase> findByUserAndExchangeRate_DateBetween(User user, LocalDate startDate, LocalDate endDate);
}


