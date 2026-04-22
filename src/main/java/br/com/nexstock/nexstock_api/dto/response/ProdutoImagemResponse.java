package br.com.nexstock.nexstock_api.dto.response;

import java.util.UUID;

public record ProdutoImagemResponse(
        UUID produtoId,
        String imagemUrl,
        String imagemKey
) {
}
