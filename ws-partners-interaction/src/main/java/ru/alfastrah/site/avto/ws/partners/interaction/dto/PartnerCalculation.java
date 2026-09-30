package ru.alfastrah.site.avto.ws.partners.interaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerCalculation {
    private Long calculationId;
    private LocalDateTime dateInsert;
    private UUID upid;
    private Long contractId;
    private String calcId;
}