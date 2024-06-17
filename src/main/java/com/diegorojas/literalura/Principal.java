package com.diegorojas.literalura;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.sql.*;
import java.util.Arrays;

public class Principal {

    private static final Scanner lectorEntrada = new Scanner(System.in);

    public static void main(String[] args) throws SQLException {
        while (true) {
            mostrarMenu();
            int opcion = lectorEntrada.nextInt();
            lectorEntrada.nextLine();

            switch (opcion) {
                case 1:
                    buscarLibroPorTitulo();
                    break;
                case 2:
                    listarTodosLosLibros();
                    break;
                case 3:
                    listarAutores();
                    break;
                case 4:
                    listarAutoresVivosEnAnio();
                    break;
                case 5:
                    mostrarEstadisticasLibro();
                    break;
                case 6:
                    salir();
                    break;
                default:
                    System.out.println("Opción no válida. Inténtalo de nuevo.");
            }
        }
    }

    private static void mostrarMenu() {
        System.out.println("\n**Menú Principal**");
        System.out.println("1. Buscar libro por título");
        System.out.println("2. Listar todos los libros");
        System.out.println("3. Listar autores");
        System.out.println("4. Listar autores vivos en un año específico");
        System.out.println("5. Mostrar estadísticas de libros");
        System.out.println("6. Salir");
        System.out.print("Ingrese su opción: ");
    }

    private static void buscarLibroPorTitulo() {
        System.out.print("Ingrese el título del libro: ");
        String titulo = lectorEntrada.nextLine();

        try {
            String libroJson = ClienteLibro.enviarSolicitudGet("https://gutendex.com/books/?q=" + titulo).body();
            Libro libro = AnalizadorJson.parsearLibroDeJson(libroJson);

            System.out.println("\n**Detalles del libro:**");
            System.out.println("Título: " + libro.getTitulo());
            System.out.println("Autor: " + libro.getAutor());
            System.out.println("Idiomas: " + Arrays.toString(libro.getIdiomas()));
            System.out.println("Descargas: " + libro.getDescargas());
        } catch (IOException | InterruptedException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void listarTodosLosLibros() {
        try (PreparedStatement stmt = conexionBD.prepareStatement("SELECT * FROM libros")) {
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n**Lista de todos los libros:**");
            while (rs.next()) {
                int idLibro = rs.getInt("id_libro");
                String titulo = rs.getString("titulo");
                String autor = rs.getString("autor"); // This might need adjustment depending on your database schema
                String[] idiomas = rs.getString("idiomas").split(",");
                int descargas = rs.getInt("descargas");

                System.out.println("\nLibro " + idLibro + ":");
                System.out.println("Título: " + titulo);
                System.out.println("Autor: " + autor);
                System.out.println("Idiomas: " + Arrays.toString(idiomas));
                System.out.println("Descargas: " + descargas);
            }
        } catch (SQLException e) {
            System.out.println("Error al listar libros: " + e.getMessage());
        }
    }

    private static void listarAutores() {
        try (PreparedStatement stmt = conexionBD.prepareStatement("SELECT * FROM autores")) {
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n**Lista de autores:**");
            while (rs.next()) {
                int idAutor = rs.getInt("id_autor");
                String nombre = rs.getString("nombre");
                String apellido = rs.getString("apellido");
                String fechaNacimiento = rs.getString("fecha_nacimiento");
                String fechaMuerte = rs.getString("fecha_muerte");

                System.out.println("\nAutor " + idAutor + ":");
                System.out.println("Nombre: " + nombre + " " + apellido);
                System.out.println("Fecha de nacimiento: " + fechaNacimiento);
                System.out.println("Fecha de muerte: " + (fechaMuerte != null ? fechaMuerte : "Vivo/a"));
            }
        } catch (SQLException e) {
            System.out.println("Error al listar autores: " + e.getMessage());
        }
    }

    private static void listarAutoresVivosEnAnio() throws SQLException {
        System.out.print("Ingrese el año: ");
        int anio = lectorEntrada.nextInt();
        lectorEntrada.nextLine(); // Consumir carácter de nueva línea

        try (PreparedStatement stmt = conexionBD.prepareStatement("SELECT a.nombre, a.apellido\n" +
                "FROM autores a\n" +
                "WHERE a.fecha_muerte IS NULL OR a.fecha_muerte >= ?")) {
            stmt.setInt(1, anio);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n**Autores vivos en el año " + anio + ":");
        }

    }

    private static void mostrarEstadisticasLibro() {
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/literatura", "postgres", "password")) {
            // 1. Obtener el libro más descargado
            Libro libroMasDescargado = obtenerLibroMasDescargado(conn);

            // 2. Obtener el autor más prolífico
            Autor autorMasProlifico = obtenerAutorMasProlifico(conn);

            // 3. Obtener el idioma más popular
            String idiomaMasPopular = obtenerIdiomaMasPopular(conn);

            // 4. Mostrar los resultados
            System.out.println("\n**Estadísticas de libros:**");
            if (libroMasDescargado != null) {
                System.out.println("Libro más descargado: " + libroMasDescargado.getTitulo() + " (" + libroMasDescargado.getDescargas() + " descargas)");
            } else {
                System.out.println("No hay libros registrados en la base de datos.");
            }

            if (autorMasProlifico != null) {
                System.out.println("Autor más prolífico: " + autorMasProlifico.getNombre() + " " + autorMasProlifico.getApellido() + " (" + autorMasProlifico.getCantidadLibros() + " libros)");
            } else {
                System.out.println("No hay autores registrados en la base de datos.");
            }

            if (idiomaMasPopular != null) {
                System.out.println("Idioma más popular: " + idiomaMasPopular);
            } else {
                System.out.println("No hay libros registrados en la base de datos.");
            }
        } catch (SQLException e) {
            System.out.println("Error al mostrar estadísticas: " + e.getMessage());
        }
    }

    private static Libro obtenerLibroMasDescargado(Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT l.id_libro, l.titulo, l.descargas\n" +
                "FROM libros l\n" +
                "ORDER BY l.descargas DESC\n" +
                "LIMIT 1")) {
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int idLibro = rs.getInt("id_libro");
                String titulo = rs.getString("titulo");
                int descargas = rs.getInt("descargas");
                return new Libro(idLibro, titulo, descargas);
            } else {
                return null;
            }
        }
    }

    private static Autor obtenerAutorMasProlifico(Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT a.id_autor, a.nombre, a.apellido, COUNT(*) AS cantidad_libros\n" +
                "FROM autores a\n" +
                "JOIN libros l ON a.id_autor = l.id_autor\n" +
                "GROUP BY a.id_autor, a.nombre, a.apellido\n" +
                "ORDER BY cantidad_libros DESC\n" +
                "LIMIT 1")) {
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int idAutor = rs.getInt("id_autor");
                String nombre = rs.getString("nombre");
                String apellido = rs.getString("apellido");
                int cantidadLibros = rs.getInt("cantidad_libros");
                return new Autor(idAutor, nombre, apellido, cantidadLibros);
            } else {
                return null;
            }
        }
    }

    private static String obtenerIdiomaMasPopular(Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT i.idioma, COUNT(*) AS cantidad_libros\n" +
                "FROM libros l\n" +
                "INNER JOIN libros_idiomas li ON l.id_libro = li.id_libro\n" +
                "INNER JOIN idiomas i ON li.id_idioma = i.id_idioma\n" +
                "GROUP BY i.idioma\n" +
                "ORDER BY cantidad_libros DESC\n" +
                "LIMIT 1")) {
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String idioma = rs.getString("idioma");
                return idioma;
            } else {
                return null;
            }
        }
    }

    private static void salir() {
        System.out.println("Saliendo del sistema...");
        lectorEntrada.close();
        System.exit(0);
    }
}