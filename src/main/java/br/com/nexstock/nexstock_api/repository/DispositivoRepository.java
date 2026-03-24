package br.com.nexstock.nexstock_api.repository;

import br.com.nexstock.nexstock_api.domain.entity.Dispositivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DispositivoRepository extends JpaRepository<Dispositivo, UUID> {

    List<Dispositivo> findAllByEmpresaId(UUID empresaId);

    Optional<Dispositivo> findByIdAndEmpresaId(UUID id, UUID empresaId);

    long countByEmpresaId(UUID empresaId);
}