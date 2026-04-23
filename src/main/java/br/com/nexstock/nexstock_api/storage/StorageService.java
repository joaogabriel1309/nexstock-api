package br.com.nexstock.nexstock_api.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    StorageUploadResult uploadProductImage(MultipartFile file, String empresaId, String produtoId);
}
