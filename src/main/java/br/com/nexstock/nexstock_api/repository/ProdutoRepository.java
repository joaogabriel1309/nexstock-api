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

    List<Produto> findAllByEmpresaIdAndDeletadoEmIsNull(UUID empresaId);

    Optional<Produto> findByIdAndEmpresaId(UUID id, UUID empresaId);

    Optional<Produto> findByIdAndEmpresaIdAndDeletadoEmIsNull(UUID id, UUID empresaId);

    boolean existsByCodigoBarrasAndEmpresaIdAndDeletadoEmIsNull(String codigoBarras, UUID empresaId);

    boolean existsBySkuAndEmpresaIdAndDeletadoEmIsNull(String sku, UUID empresaId);

    @Query("""
        SELECT p FROM Produto p
        WHERE p.empresa.id = :empresaId
        AND p.atualizadoEm > :desde
        ORDER BY p.atualizadoEm ASC
    """)
    List<Produto> findAllParaSync(
            @Param("empresaId") UUID empresaId,
            @Param("desde") LocalDateTime desde
    );

    @Query("""
        SELECT COUNT(p) > 0 FROM Produto p
        WHERE p.empresa.id = :empresaId
        AND p.codigoBarras = :codigoBarras
        AND p.id <> :idExcluir
        AND p.deletadoEm IS NULL
    """)
    boolean existsCodigoBarrasEmOutroProduto(
            @Param("empresaId") UUID empresaId,
            @Param("codigoBarras") String codigoBarras,
            @Param("idExcluir") UUID idExcluir
    );

    @Query("""
        SELECT COUNT(p) > 0 FROM Produto p
        WHERE p.empresa.id = :empresaId
        AND p.sku = :sku
        AND p.id <> :idExcluir
        AND p.deletadoEm IS NULL
    """)
    boolean existsSkuEmOutroProduto(
            @Param("empresaId") UUID empresaId,
            @Param("sku") String sku,
            @Param("idExcluir") UUID idExcluir
    );
}
