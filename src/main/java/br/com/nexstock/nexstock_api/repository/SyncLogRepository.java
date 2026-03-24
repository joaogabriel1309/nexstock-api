package br.com.nexstock.nexstock_api.repository;

import br.com.nexstock.nexstock_api.domain.entity.SyncLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SyncLogRepository extends JpaRepository<SyncLog, UUID> {

    Page<SyncLog> findAllByEmpresaIdOrderByDataSyncDesc(UUID empresaId, Pageable pageable);

    Page<SyncLog> findAllByDispositivoIdOrderByDataSyncDesc(UUID dispositivoId, Pageable pageable);
}