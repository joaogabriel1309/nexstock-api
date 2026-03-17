package br.com.nexstock.nexstock_api.repository;

import br.com.nexstock.nexstock_api.domain.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, UUID> {

    List<Produto> findAllByContratoIdAndDeletadoFalse(UUID contratoId);

    Optional<Produto> findByIdAndContratoId(UUID id, UUID contratoId);

    @Query("""
        SELECT p FROM Produto p
        WHERE p.contrato.id = :contratoId
        AND p.atualizadoEm > :desde
        ORDER BY p.atualizadoEm ASC
    """)
    List<Produto> findAllParaSync(
            @Param("contratoId") UUID contratoId,
            @Param("desde") LocalDateTime desde
    );

    @Query("""
        SELECT COUNT(p) > 0 FROM Produto p
        WHERE p.contrato.id = :contratoId
        AND p.codigoBarras = :codigoBarras
        AND p.id <> :idExcluir
        AND p.deletado = false
    """)
    boolean existsCodigoBarrasEmOutroProduto(
            @Param("contratoId") UUID contratoId,
            @Param("codigoBarras") String codigoBarras,
            @Param("idExcluir") UUID idExcluir
    );
}