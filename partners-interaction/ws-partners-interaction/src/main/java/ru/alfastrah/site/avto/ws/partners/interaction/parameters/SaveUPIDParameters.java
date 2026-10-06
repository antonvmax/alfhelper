package ru.alfastrah.site.avto.ws.partners.interaction.parameters;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class SaveUPIDParameters {
    private String UPID;
    private String callerCode;
}
