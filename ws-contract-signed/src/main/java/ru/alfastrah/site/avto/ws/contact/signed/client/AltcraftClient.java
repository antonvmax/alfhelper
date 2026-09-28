package ru.alfastrah.site.avto.ws.contact.signed.client;

import ru.alfastrah.interplat4.altcraft.model.Request;
import ru.alfastrah.interplat4.altcraft.model.Response;

public interface AltcraftClient {

    Response sendEmail(Request request);
}
