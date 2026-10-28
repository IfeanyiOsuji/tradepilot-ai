package tradepilot_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tradepilot_api.entity.Sale;

public interface SaleRepository extends JpaRepository<Sale, Long> {
}
