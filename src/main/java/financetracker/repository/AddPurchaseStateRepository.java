package financetracker.repository;

import financetracker.entity.AddPurchaseState;
import financetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddPurchaseStateRepository extends JpaRepository<AddPurchaseState, Long> {

    Optional<AddPurchaseState> findByUser(User user);
}

