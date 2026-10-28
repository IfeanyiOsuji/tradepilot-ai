package tradepilot_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tradepilot_api.entity.Sale;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
}
