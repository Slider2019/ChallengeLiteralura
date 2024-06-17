package com.diegorojas.literalura;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Libro {

    @JsonProperty("titulo")
    private String titulo;

    @JsonProperty("autor")
    private String autor;

    @JsonProperty("idiomas")
    private String[] idiomas;

    @JsonProperty("descargas")
    private int descargas;

    public Libro(int idLibro, String titulo, int descargas) {
    }


    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String[] getIdiomas() {
        return idiomas;
    }

    public void setIdiomas(String[] idiomas) {
        this.idiomas = idiomas;
    }

    public int getDescargas() {
        return descargas;
    }

    public void setDescargas(int descargas) {
        this.descargas = descargas;
    }
}