package br.com.nexstock.nexstock_api.storage.s3;

import br.com.nexstock.nexstock_api.config.StorageProperties;
import br.com.nexstock.nexstock_api.exception.ArquivoInvalidoException;
import br.com.nexstock.nexstock_api.exception.FalhaUploadException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("S3StorageService")
class S3StorageServiceTest {

    @Mock S3Client s3Client;

    private S3StorageService storageService;

    @BeforeEach
    void setUp() {
        var properties = new StorageProperties(
                DataSize.ofMegabytes(1),
                List.of("image/jpeg", "image/png", "image/webp"),
                new StorageProperties.S3(
                        "nexstock-test",
                        "us-east-2",
                        "https://cdn.test.local",
                        "produtos"
                )
        );
        storageService = new S3StorageService(s3Client, properties);
    }

    @Nested
    @DisplayName("uploadProductImage")
    class UploadProductImage {

        @Test
        @DisplayName("deve validar, enviar arquivo e retornar chave e url")
        void deveEnviarArquivoComSucesso() {
            // Arrange
            var file = new MockMultipartFile(
                    "arquivo", "Café Especial.png", "image/png", "imagem".getBytes());
            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(PutObjectResponse.builder().build());

            // Act
            var result = storageService.uploadProductImage(file, "empresa-1", "produto-1");

            // Assert
            assertThat(result.key()).startsWith("produtos/empresa-1/produto-1/cafe-especial-");
            assertThat(result.key()).endsWith(".png");
            assertThat(result.url()).isEqualTo("https://cdn.test.local/" + result.key());

            ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
            verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));

            PutObjectRequest request = requestCaptor.getValue();
            assertThat(request.bucket()).isEqualTo("nexstock-test");
            assertThat(request.key()).isEqualTo(result.key());
            assertThat(request.contentType()).isEqualTo("image/png");
            assertThat(request.contentLength()).isEqualTo(file.getSize());
        }

        @Test
        @DisplayName("deve rejeitar arquivo vazio")
        void deveRejeitarArquivoVazio() {
            // Arrange
            var file = new MockMultipartFile("arquivo", "produto.png", "image/png", new byte[0]);

            // Act & Assert
            assertThatThrownBy(() -> storageService.uploadProductImage(file, "empresa-1", "produto-1"))
                    .isInstanceOf(ArquivoInvalidoException.class);
            verifyNoInteractions(s3Client);
        }

        @Test
        @DisplayName("deve rejeitar content type nao permitido")
        void deveRejeitarContentTypeNaoPermitido() {
            // Arrange
            var file = new MockMultipartFile("arquivo", "produto.gif", "image/gif", "gif".getBytes());

            // Act & Assert
            assertThatThrownBy(() -> storageService.uploadProductImage(file, "empresa-1", "produto-1"))
                    .isInstanceOf(ArquivoInvalidoException.class)
                    .hasMessageContaining("Tipo de arquivo");
            verifyNoInteractions(s3Client);
        }

        @Test
        @DisplayName("deve rejeitar arquivo acima do limite")
        void deveRejeitarArquivoAcimaDoLimite() {
            // Arrange
            byte[] content = new byte[(int) DataSize.ofMegabytes(1).toBytes() + 1];
            var file = new MockMultipartFile("arquivo", "produto.png", "image/png", content);

            // Act & Assert
            assertThatThrownBy(() -> storageService.uploadProductImage(file, "empresa-1", "produto-1"))
                    .isInstanceOf(ArquivoInvalidoException.class)
                    .hasMessageContaining("tamanho");
            verifyNoInteractions(s3Client);
        }

        @Test
        @DisplayName("deve traduzir falha do S3 para excecao de upload")
        void deveTraduzirFalhaDoS3() {
            // Arrange
            var file = new MockMultipartFile("arquivo", "produto.webp", "image/webp", "webp".getBytes());
            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenThrow(S3Exception.builder().message("AccessDenied").build());

            // Act & Assert
            assertThatThrownBy(() -> storageService.uploadProductImage(file, "empresa-1", "produto-1"))
                    .isInstanceOf(FalhaUploadException.class)
                    .hasMessageContaining("storage");
        }
    }
}
