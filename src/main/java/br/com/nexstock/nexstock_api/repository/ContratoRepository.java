package br.com.nexstock.nexstock_api.repository;

import br.com.nexstock.nexstock_api.domain.entity.Contrato;
import br.com.nexstock.nexstock_api.domain.enums.StatusContrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContratoRepository extends JpaRepository<Contrato, UUID> {

    Optional<Contrato> findByClienteIdAndStatus(UUID clienteId, StatusContrato status);

    List<Contrato> findAllByClienteIdOrderByCriadoEmDesc(UUID clienteId);

    @Query("""
        SELECT c FROM Contrato c
        WHERE c.status = 'ATIVO'
        AND c.dataFim < :hoje
    """)
    List<Contrato> findContratosVencidos(@Param("hoje") LocalDate hoje);
}