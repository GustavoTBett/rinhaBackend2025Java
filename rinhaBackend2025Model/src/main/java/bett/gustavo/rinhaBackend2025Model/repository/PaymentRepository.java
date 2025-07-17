package bett.gustavo.rinhaBackend2025Model.repository;

import bett.gustavo.rinhaBackend2025Model.model.Payment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PaymentRepository extends CrudRepository<Payment, UUID> {

}
