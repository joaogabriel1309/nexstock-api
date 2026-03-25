package br.com.nexstock.nexstock_api.service;

import br.com.nexstock.nexstock_api.domain.entity.Contrato;
import br.com.nexstock.nexstock_api.domain.entity.Empresa;
import br.com.nexstock.nexstock_api.domain.entity.Plano;
import br.com.nexstock.nexstock_api.domain.enums.StatusContrato;
import br.com.nexstock.nexstock_api.dto.response.ContratoResponse;
import br.com.nexstock.nexstock_api.exception.ContratoInativoException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContratoService")
class ContratoServiceTest {

    @Mock ContratoRepository contratoRepository;
    @Mock PlanoService       planoService;

    @InjectMocks ContratoService contratoService;

    private UUID planoId;
    private Plano plano;

    @BeforeEach
    void setUp() {
        planoId = UUID.randomUUID();

        plano = Plano.builder()
                .id(planoId)
                .nome("Mensal Básico")
                .preco(new BigDecimal("49.90"))
                .duracaoDias(30)
                .maxDispositivos(5)
                .ativo(true)
                .build();
    }

    @Nested
    @DisplayName("gerarContratoInicial")
    class GerarInicial {

        @Test
        @DisplayName("deve gerar contrato inicial ATIVO baseado no plano escolhido")
        void deveGerarContratoInicialComSucesso() {
            when(planoService.buscarPorId(planoId)).thenReturn(plano);

            when(contratoRepository.save(any(Contrato.class))).thenAnswer(i -> i.getArgument(0));

            Contrato resultado = contratoService.gerarContratoInicial(planoId);

            assertThat(resultado.getStatus()).isEqualTo(StatusContrato.ATIVO);
            assertThat(resultado.getPlano()).isEqualTo(plano);
            assertThat(resultado.getDataInicio()).isEqualTo(LocalDate.now());
            assertThat(resultado.getDataFim()).isEqualTo(LocalDate.now().plusDays(30));

            verify(contratoRepository).save(any());
        }
    }

    @Nested
    @DisplayName("ciclo de vida e consultas")
    class CicloDeVida {

        @Test
        @DisplayName("deve buscar entidade e retornar contrato quando ID existe")
        void deveRetornarContratoQuandoIdExiste() {
            var contrato = Contrato.builder().id(UUID.randomUUID()).status(StatusContrato.ATIVO).build();

            when(contratoRepository.findById(contrato.getId())).thenReturn(Optional.of(contrato));

            Contrato resultado = contratoService.buscarEntidade(contrato.getId());

            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(contrato.getId());
        }

        @Test
        @DisplayName("deve lançar RecursoNaoEncontradoException quando contrato não existe")
        void deveLancarErroQuandoContratoInexistente() {
            UUID idInexistente = UUID.randomUUID();
            when(contratoRepository.findById(idInexistente)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contratoService.buscarEntidade(idInexistente))
                    .isInstanceOf(RecursoNaoEncontradoException.class);
        }

        @Test
        @DisplayName("deve alterar status para CANCELADO e salvar no banco")
        void deveCancelarContratoComSucesso() {
            var contrato = Contrato.builder()
                    .id(UUID.randomUUID())
                    .status(StatusContrato.ATIVO)
                    .build();

            when(contratoRepository.findById(contrato.getId())).thenReturn(Optional.of(contrato));
            when(contratoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            contratoService.cancelar(contrato.getId());

            assertThat(contrato.getStatus()).isEqualTo(StatusContrato.CANCELADO);
            verify(contratoRepository).save(argThat(c -> c.getStatus() == StatusContrato.CANCELADO));
        }
    }
}