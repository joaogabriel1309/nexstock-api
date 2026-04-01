package br.com.nexstock.nexstock_api.repository;

import br.com.nexstock.nexstock_api.domain.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    List<Usuario> findAllByEmpresaIdAndDeletadoEmIsNull(UUID empresaId);

    Optional<Usuario> findByEmailAndDeletadoEmIsNull(String email);

    Optional<Usuario> findByIdAndEmpresaIdAndDeletadoEmIsNull(UUID id, UUID empresaId);

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);
}