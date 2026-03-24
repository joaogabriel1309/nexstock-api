package br.com.nexstock.nexstock_api.repository;

import br.com.nexstock.nexstock_api.domain.entity.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, UUID> {

    List<MovimentacaoEstoque> findAllByEmpresaIdAndProdutoIdOrderByCriadoEmDesc(
            UUID empresaId,
            UUID produtoId
    );

    @Query("""
        SELECT m.id FROM MovimentacaoEstoque m
        WHERE m.id IN :ids
         AND m.empresa.id = :empresaId
    """)
    Set<UUID> findIdsExistentes(
            @Param("ids") List<UUID> ids,
            @Param("empresaId") UUID empresaId
    );
}