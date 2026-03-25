package br.com.nexstock.nexstock_api.service;

import br.com.nexstock.nexstock_api.domain.entity.Contrato;
import br.com.nexstock.nexstock_api.domain.entity.Plano;
import br.com.nexstock.nexstock_api.domain.enums.StatusContrato;
import br.com.nexstock.nexstock_api.dto.request.ContratoRequest;
import br.com.nexstock.nexstock_api.dto.response.ContratoResponse;
import br.com.nexstock.nexstock_api.exception.ContratoInativoException;
import br.com.nexstock.nexstock_api.exception.RegraDeNegocioException;
import br.com.nexstock.nexstock_api.exception.RecursoNaoEncontradoException;
import br.com.nexstock.nexstock_api.repository.ContratoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContratoService")
class ContratoServiceTest {

    @Mock ContratoRepository contratoRepository;
    @Mock ClienteService     clienteService;
    @Mock PlanoService       planoService;

    @InjectMocks ContratoService contratoService;

    private UUID clienteId;
    private UUID planoId;
    private Cliente cliente;
    private Plano plano;

    @BeforeEach
    void setUp() {
        clienteId = UUID.randomUUID();
        planoId   = UUID.randomUUID();

        cliente = Cliente.builder()
                .id(clienteId)
                .nome("Loja do João")
                .email("joao@loja.com")
                .ativo(true)
                .build();

        plano = Plano.builder()
                .id(planoId)
                .nome("Mensal Básico")
                .preco(new BigDecimal("49.90"))
                .duracaoDias(30)
                .maxDispositivos(1)
                .ativo(true)
                .build();
    }

    @Nested
    @DisplayName("contratar")
    class Contratar {

        @Test
        @DisplayName("deve criar contrato com status ATIVO e data_fim calculada")
        void deveCriarContratoComStatusAtivo() {
            var request = new ContratoRequest(clienteId, planoId);

            when(clienteService.buscarEntidade(clienteId)).thenReturn(cliente);
            when(planoService.buscarEntidade(planoId)).thenReturn(plano);
            when(contratoRepository.findByClienteIdAndStatus(clienteId, StatusContrato.ATIVO))
                    .thenReturn(Optional.empty());
            when(contratoRepository.save(any())).thenAnswer(inv -> {
                Contrato c = inv.getArgument(0);
                c = Contrato.builder()
                        .id(UUID.randomUUID())
                        .cliente(c.getCliente())
                        .plano(c.getPlano())
                        .dataInicio(c.getDataInicio())
                        .dataFim(c.getDataFim())
                        .status(StatusContrato.ATIVO)
                        .build();
                return c;
            });

            ContratoResponse response = contratoService.contratar(request);

            assertThat(response.getStatus()).isEqualTo(StatusContrato.ATIVO);
            assertThat(response.getDataFim())
                    .isEqualTo(LocalDate.now().plusDays(30));
        }

        @Test
        @DisplayName("deve lançar exceção quando cliente já tem contrato ativo")
        void deveLancarExcecaoComContratoJaAtivo() {
            var request   = new ContratoRequest(clienteId, planoId);
            var existente = Contrato.builder().id(UUID.randomUUID()).build();

            when(clienteService.buscarEntidade(clienteId)).thenReturn(cliente);
            when(planoService.buscarEntidade(planoId)).thenReturn(plano);
            when(contratoRepository.findByClienteIdAndStatus(clienteId, StatusContrato.ATIVO))
                    .thenReturn(Optional.of(existente));

            assertThatThrownBy(() -> contratoService.contratar(request))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("já possui um contrato ativo");
        }

        @Test
        @DisplayName("deve lançar exceção quando plano está inativo")
        void deveLancarExcecaoComPlanoInativo() {
            plano.setAtivo(false);
            var request = new ContratoRequest(clienteId, planoId);

            when(clienteService.buscarEntidade(clienteId)).thenReturn(cliente);
            when(planoService.buscarEntidade(planoId)).thenReturn(plano);

            assertThatThrownBy(() -> contratoService.contratar(request))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("não está disponível");
        }
    }

    @Nested
    @DisplayName("buscarEntidadeVigente")
    class BuscarVigente {

        @Test
        @DisplayName("deve lançar ContratoInativoException quando contrato expirado")
        void deveLancarExcecaoQuandoContratoExpirado() {
            var contratoExpirado = Contrato.builder()
                    .id(UUID.randomUUID())
                    .status(StatusContrato.EXPIRADO)
                    .dataFim(LocalDate.now().minusDays(1))
                    .build();

            when(contratoRepository.findById(contratoExpirado.getId()))
                    .thenReturn(Optional.of(contratoExpirado));

            assertThatThrownBy(() -> contratoService.buscarEntidadeVigente(contratoExpirado.getId()))
                    .isInstanceOf(ContratoInativoException.class);
        }

        @Test
        @DisplayName("deve lançar RecursoNaoEncontradoException quando contrato não existe")
        void deveLancarExcecaoQuandoContratoNaoExiste() {
            UUID idInexistente = UUID.randomUUID();
            when(contratoRepository.findById(idInexistente)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contratoService.buscarEntidadeVigente(idInexistente))
                    .isInstanceOf(RecursoNaoEncontradoException.class);
        }
    }

    @Nested
    @DisplayName("cancelar / suspender / reativar")
    class CicloDeVida {

        private Contrato contrato;

        @BeforeEach
        void setup() {
            contrato = Contrato.builder()
                    .id(UUID.randomUUID())
                    .cliente(cliente)
                    .plano(plano)
                    .dataInicio(LocalDate.now())
                    .dataFim(LocalDate.now().plusDays(30))
                    .status(StatusContrato.ATIVO)
                    .build();
        }

        @Test
        @DisplayName("deve cancelar contrato ativo")
        void deveCancelarContratoAtivo() {
            when(contratoRepository.findById(contrato.getId())).thenReturn(Optional.of(contrato));
            when(contratoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            contratoService.cancelar(contrato.getId());

            assertThat(contrato.getStatus()).isEqualTo(StatusContrato.CANCELADO);
        }

        @Test
        @DisplayName("deve suspender contrato ativo")
        void deveSuspenderContratoAtivo() {
            when(contratoRepository.findById(contrato.getId())).thenReturn(Optional.of(contrato));
            when(contratoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            contratoService.suspender(contrato.getId());

            assertThat(contrato.getStatus()).isEqualTo(StatusContrato.SUSPENSO);
        }
    }
}