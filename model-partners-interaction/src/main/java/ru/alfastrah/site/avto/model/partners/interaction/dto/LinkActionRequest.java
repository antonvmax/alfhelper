package ru.alfastrah.site.avto.model.partners.interaction.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LinkActionRequest {

    String upid;

    String calculationId;

    Long contractId;
}
