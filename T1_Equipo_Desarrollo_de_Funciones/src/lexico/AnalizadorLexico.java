package lexico;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class AnalizadorLexico {

    // --- AGREGADO: tabla de errores publica para accederla desde AnalizadorSistemas ---
    public static TablaErrores tablaErrores = new TablaErrores();

    public static LinkedList analizar(String rutaArchivo) {

        LinkedList tabla = new LinkedList(null);

        // --- AGREGADO: reiniciar tabla en cada analisis ---
        tablaErrores = new TablaErrores();

        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {

            String linea;
            int fila = 1;

            while ((linea = br.readLine()) != null) {

                int i = 0;
                int columna = 1;

                while (i < linea.length()) {

                    // --- AGREGADO: saltar espacios sin registrarlos como error ---
                    if (Character.isWhitespace(linea.charAt(i))) {
                        i++;
                        columna++;
                        continue;
                    }

                    Token token;

                    // --- MODIFICADO: se agrega tablaErrores al llamado ---
                    token = AutomataCadena.reconocer(linea, i, fila, columna, tablaErrores);
                    if (token != null) {
                        tabla.agregarNodoFinal(token);
                        i += token.lexema.length();
                        columna += token.lexema.length();
                        continue;
                    }

                    // Si habia una " pero devolvio null, fue cadena sin cerrar.
                    // Saltamos el resto de la linea para no generar errores falsos.
                    if (linea.charAt(i) == '"') {
                        i = linea.length();
                        continue;
                    }

                    // --- MODIFICADO: se agrega tablaErrores al llamado ---
                    token = AutomataNumero.reconocer(linea, i, fila, columna, tablaErrores);
                    if (token != null) {
                        tabla.agregarNodoFinal(token);
                        i += token.lexema.length();
                        columna += token.lexema.length();
                        continue;
                    }

                    // --- AGREGADO: si AutomataNumero consumio caracteres con error, saltarlos ---
                    // Esto evita que "3." se registre como NUMERO_MAL_FORMADO y luego
                    // "3" y "." se registren tambien como CARACTER_NO_RECONOCIDO
                    if (AutomataNumero.caracteresConsumidos > 0) {
                        i += AutomataNumero.caracteresConsumidos;
                        columna += AutomataNumero.caracteresConsumidos;
                        AutomataNumero.caracteresConsumidos = 0;
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

                    // --- AGREGADO: registrar caracter no reconocido ---
                    char charNoReconocido = linea.charAt(i);
                    tablaErrores.agregar(
                        TipoError.CARACTER_NO_RECONOCIDO,
                        String.valueOf(charNoReconocido),
                        fila,
                        columna,
                        "El caracter '" + charNoReconocido + "' no pertenece al lenguaje"
                    );

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