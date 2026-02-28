package lexico;

public class AutomataCadena {

    // --- MODIFICADO: se agrega TablaErrores como parametro ---
    public static Token reconocer(String texto, int inicio, int fila, int columna, TablaErrores tablaErrores) {

        if (texto.charAt(inicio) != '"') return null;

        int i = inicio + 1;
        String lexema = "\"";

        while (i < texto.length() && texto.charAt(i) != '"') {
            lexema += texto.charAt(i);
            i++;
        }

        // --- MODIFICADO: antes solo devolvía null, ahora registra el error ---
        if (i >= texto.length()) {
            tablaErrores.agregar(
                TipoError.CADENA_SIN_CERRAR,
                lexema,
                fila,
                columna,
                "La cadena " + lexema + " nunca se cerro con comilla"
            );
            return null;
        }

        lexema += "\"";
        return new Token(TipoToken.CADENA, lexema, fila, columna);
    }
}