package lexico;

public class AutomataCadena {

    public static Token reconocer(String texto, int inicio, int fila, int columna) {

        if (texto.charAt(inicio) != '"') return null;

        int i = inicio + 1;
        String lexema = "\"";

        while (i < texto.length() && texto.charAt(i) != '"') {
            lexema += texto.charAt(i);
            i++;
        }

        if (i >= texto.length()) return null;

        lexema += "\"";
        return new Token(TipoToken.CADENA, lexema, fila, columna);
    }
}