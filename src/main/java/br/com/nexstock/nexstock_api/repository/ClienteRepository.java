package br.com.nexstock.nexstock_api.repository;

import br.com.nexstock.nexstock_api.domain.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, UUID> {
    Optional<Cliente> findByEmail(String email);

    boolean existsByEmail(String email);
}
