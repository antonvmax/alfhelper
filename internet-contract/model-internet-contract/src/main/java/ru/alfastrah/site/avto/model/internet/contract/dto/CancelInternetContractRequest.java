package ru.alfastrah.site.avto.model.internet.contract.dto;

import lombok.Builder;

@Builder
public record CancelInternetContractRequest(
        Long contractId
) {
}
