package br.com.nexstock.nexstock_api.repository;

import br.com.nexstock.nexstock_api.domain.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmpresaRepository extends JpaRepository<Empresa, UUID> {

    List<Empresa> findAllByContratoId(UUID contratoId);

    Optional<Empresa> findByIdAndContratoId(UUID id, UUID contratoId);

    boolean existsByCpfCnpjAndContratoId(String cpfCnpj, UUID contratoId);
}