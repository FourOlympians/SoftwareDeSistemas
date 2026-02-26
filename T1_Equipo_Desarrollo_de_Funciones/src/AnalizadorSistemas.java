
import lexico.LinkedList;
import lexico.Nodo;
import lexico.Token;
import lexico.AnalizadorLexico;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;

public class AnalizadorSistemas {

    public static void main(String[] args) {

        String archivoEntrada = "entrada.txt";
        String archivoSalida = "salida_limpia.txt";

        // PREPROCESAR ARCHIVO (quita comentarios y espacios)
        preprocesarArchivo(archivoEntrada, archivoSalida);

        // EJECUTAR ANALIZADOR LEXICO
        LinkedList tabla = AnalizadorLexico.analizar(archivoSalida);

        // MOSTRAR TABLA DE SIMBOLOS
        System.out.println("\n===== TABLA DE SIMBOLOS =====");

        Nodo tmp = tabla.obtenerHead();

        if (tmp == null) {
            System.out.println("No se encontraron tokens");
            return;
        }

        while (tmp != null) {
            System.out.println(tmp.data);
            tmp = tmp.obtenerSiguiente();
        }

        System.out.println("Total de tokens: " + tabla.count);
    }


    public static void preprocesarArchivo(String rutaOrigen, String rutaDestino) {
        File archivo = new File(rutaOrigen);

        if (!archivo.exists()) {
            System.err.println("Error: El archivo de origen no existe.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(archivo));
             BufferedWriter bw = new BufferedWriter(new FileWriter(rutaDestino))) {

            String linea;
            boolean enComentarioBloque = false;

            while ((linea = br.readLine()) != null) {
                linea = linea.trim();

                if (linea.isEmpty()) continue;

                if (linea.contains("/*")) {
                    enComentarioBloque = true;
                }
                if (enComentarioBloque) {
                    if (linea.contains("*/")) {
                        enComentarioBloque = false;
                    }
                    continue;
                }

                if (linea.startsWith("//")) continue;

                String lineaProcesada = "";
                for (int i = 0; i < linea.length(); i++) {
                    char c = linea.charAt(i);
                    if (c != ' ' && c != '\t') {
                        lineaProcesada += c;
                    }
                }

                if (!lineaProcesada.isEmpty()) {
                    bw.write(lineaProcesada);
                    bw.newLine();
                }
            }

            System.out.println("Archivo preprocesado con éxito.");

        } catch (IOException e) {
            System.err.println("Error de lectura/escritura: " + e.getMessage());
        }
    }

    public static void recorrerArchivo(String rutaArchivo) {
        File archivo = new File(rutaArchivo);

        if (!archivo.exists()) {
            System.err.println("Error: El archivo no existe.");
            return;
        }

        try (FileReader fr = new FileReader(archivo)) {
            int caracter;
            System.out.println("--- Iniciando lectura carácter por carácter ---");
            while ((caracter = fr.read()) != -1) {
                System.out.print((char) caracter);
            }
            System.out.println("\n--- Fin de la lectura ---");
        } catch (IOException e) {
            System.err.println("Error al recorrer el archivo: " + e.getMessage());
        }
    }
}