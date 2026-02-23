package lexico;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class AnalizadorLexico {

    public static LinkedList analizar(String rutaArchivo) {

        LinkedList tabla = new LinkedList(null);

        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {

            String linea;
            int fila = 1;

            while ((linea = br.readLine()) != null) {

                int i = 0;
                int columna = 1;

                while (i < linea.length()) {

                    Token token;

                    token = AutomataCadena.reconocer(linea, i, fila, columna);
                    if (token != null) {
                        tabla.agregarNodoFinal(token);
                        i += token.lexema.length();
                        columna += token.lexema.length();
                        continue;
                    }

                    token = AutomataNumero.reconocer(linea, i, fila, columna);
                    if (token != null) {
                        tabla.agregarNodoFinal(token);
                        i += token.lexema.length();
                        columna += token.lexema.length();
                        continue;
                    }

                    token = AutomataIdentificador.reconocer(linea, i, fila, columna);
                    if (token != null) {
                        tabla.agregarNodoFinal(token);
                        i += token.lexema.length();
                        columna += token.lexema.length();
                        continue;
                    }

                    token = AutomataSimbolo.reconocer(linea, i, fila, columna);
                    if (token != null) {
                        tabla.agregarNodoFinal(token);
                        i += token.lexema.length();
                        columna += token.lexema.length();
                        continue;
                    }

                    i++;
                    columna++;
                }

                fila++;
            }

        } catch (IOException e) {
            System.out.println("Error leyendo archivo");
        }

        return tabla;
    }
}