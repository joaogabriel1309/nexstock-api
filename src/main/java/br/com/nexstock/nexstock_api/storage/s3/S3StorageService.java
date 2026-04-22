package br.com.nexstock.nexstock_api.storage.s3;

import br.com.nexstock.nexstock_api.config.StorageProperties;
import br.com.nexstock.nexstock_api.exception.ArquivoInvalidoException;
import br.com.nexstock.nexstock_api.exception.FalhaUploadException;
import br.com.nexstock.nexstock_api.storage.StorageService;
import br.com.nexstock.nexstock_api.storage.StorageUploadResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3StorageService implements StorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private final S3Client s3Client;
    private final StorageProperties storageProperties;

    @Override
    public StorageUploadResult uploadProductImage(MultipartFile file, String empresaId, String produtoId) {
        validateFile(file);

        String extension = getExtension(file.getOriginalFilename());
        String key = buildKey(file, empresaId, produtoId, extension);

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(storageProperties.s3().bucket())
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return new StorageUploadResult(key, buildPublicUrl(key));
        } catch (IOException ex) {
            throw new FalhaUploadException("Falha ao ler o arquivo enviado.", ex);
        } catch (Exception ex) {
            throw new FalhaUploadException("Falha ao enviar imagem para o storage.", ex);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ArquivoInvalidoException("Arquivo de imagem é obrigatório.");
        }

        if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
            throw new ArquivoInvalidoException("Nome do arquivo é obrigatório.");
        }

        if (file.getSize() > storageProperties.maxFileSize().toBytes()) {
            throw new ArquivoInvalidoException("Arquivo excede o tamanho máximo permitido.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !storageProperties.allowedContentTypes().contains(contentType)) {
            throw new ArquivoInvalidoException("Tipo de arquivo não permitido. Use jpg, jpeg, png ou webp.");
        }

        String extension = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ArquivoInvalidoException("Extensão de arquivo não permitida.");
        }
    }

    private String buildKey(MultipartFile file, String empresaId, String produtoId, String extension) {
        String originalName = removeExtension(file.getOriginalFilename());
        String safeName = normalizeFileName(originalName);

        return "%s/%s/%s/%s-%s.%s".formatted(
                cleanPrefix(storageProperties.s3().productImagesPrefix()),
                empresaId,
                produtoId,
                safeName,
                UUID.randomUUID(),
                extension
        );
    }

    private String buildPublicUrl(String key) {
        String publicBaseUrl = storageProperties.s3().publicBaseUrl();
        if (publicBaseUrl != null && !publicBaseUrl.isBlank()) {
            return publicBaseUrl.replaceAll("/$", "") + "/" + key;
        }

        return "https://%s.s3.%s.amazonaws.com/%s".formatted(
                storageProperties.s3().bucket(),
                storageProperties.s3().region(),
                key
        );
    }

    private String getExtension(String filename) {
        int index = filename.lastIndexOf('.');
        if (index < 0 || index == filename.length() - 1) {
            throw new ArquivoInvalidoException("Arquivo sem extensão válida.");
        }

        return filename.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private String removeExtension(String filename) {
        int index = filename.lastIndexOf('.');
        return index > 0 ? filename.substring(0, index) : filename;
    }

    private String normalizeFileName(String name) {
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9-_]", "-")
                .replaceAll("-+", "-")
                .replaceAll("(^-|-$)", "");

        return normalized.isBlank() ? "produto" : normalized;
    }

    private String cleanPrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return "produtos";
        }

        return prefix.replaceAll("^/+", "").replaceAll("/+$", "");
    }
}
