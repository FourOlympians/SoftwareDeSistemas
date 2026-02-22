import lexico.LinkedList;
import lexico.Nodo;
import lexico.Token;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class AnalizadorSistemas {

    public static void main(String[] args) {
       //AQUI PUEDEN CAMBIAR LOS NOMBRE POR RUTAS PARA SUS CASOS
        //O PUEDEN PONERLE NOMBRE PERO METAN EL ARCHIVO AL PROYECTO
        // FIERRO
        String archivoEntrada = "entrada.txt";
        String archivoSalida = "salida_limpia.txt";

        List<Token> tokens = new ArrayList<Token>();
        for  (int i = 0; i < 10; i++) {
            tokens.add(new Token(i));
        }

        LinkedList list = new LinkedList(null);
        list.agregarNodoInicio(tokens.get(4));
        list.agregarNodoFinal(tokens.get(1));
        list.agregarNodoFinal(tokens.get(3));
        list.agregarNodoInicio(tokens.get(9));
        list.printList();
        System.out.println("No de Tokens " + list.count);
        Nodo head = list.obtenerHead();
        Nodo tail = list.obtenerTail();
        System.out.println("Head -> " + head.data.value);
        System.out.println("Tail -> " + tail.data.value);
//        list.borrarNodoInicial();
        list.borrarNodoFinal();
        System.out.println("Eliminando " + tail.data.value);
//        list.agregarNodoPos(tokens.get(2), 2);
        list.printList();
        System.out.println("No de Tokens " + list.count);
        tail = list.obtenerTail();
        System.out.println("new Tail -> " + tail.data.value);
//        list.debug();

//        preprocesarArchivo(archivoEntrada, archivoSalida);
//        recorrerArchivo(archivoSalida);
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
                // Eliminar tabuladores y espacios en los extremos
                linea = linea.trim();

                if (linea.isEmpty()) continue;

                // Salto de comentarios de bloque
                if (linea.contains("/*")) {
                    enComentarioBloque = true;
                }
                if (enComentarioBloque) {
                    if (linea.contains("*/")) {
                        enComentarioBloque = false;
                    }
                    continue;
                }

                // Salto de comentarios de línea
                if (linea.startsWith("//")) continue;

                // Eliminación manual de espacios y tabuladores
                String lineaProcesada = "";
                for (int i = 0; i < linea.length(); i++) {
                    char c = linea.charAt(i);
                    // Solo agregamos el carácter si no es un espacio o tabulador
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