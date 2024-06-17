package com.diegorojas.literalura;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;

public class ClienteLibro {

    private static final HttpClient clienteHttp = HttpClient.newHttpClient();

    public static HttpResponse<String> enviarSolicitudGet(String url) throws IOException, InterruptedException {
        HttpRequest solicitud = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        return clienteHttp.send(solicitud, HttpResponse.BodyHandlers.ofString());
    }
}
