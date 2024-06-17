package com.diegorojas.literalura;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class AnalizadorJson {

    private static final ObjectMapper mapeadorJson = new ObjectMapper();

    public static Libro parsearLibroDeJson(String json) throws IOException {
        return mapeadorJson.readValue(json, Libro.class);
    }

    public static Autor parsearAutorDeJson(String json) throws IOException {
        return mapeadorJson.readValue(json, Autor.class);
    }
}