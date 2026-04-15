package lexico;

import java.util.HashSet;
import java.util.Set;

public class AutomataIdentificador {

    private static final Set<String> reservadas = new HashSet<>();

    static {
        reservadas.add("entero");
        reservadas.add("flotante");
        reservadas.add("cadena");
        reservadas.add("caracter");
        reservadas.add("logico");
        reservadas.add("si");
        reservadas.add("sino");
        reservadas.add("hacer");
        reservadas.add("mientras");
        reservadas.add("leer");
        reservadas.add("escribir");
    }

    public static Token reconocer(
        String texto,
        int inicio,
        int fila,
        int columna
    ) {
        int i = inicio;
        String lexema = "";

        if (!Character.isLetter(texto.charAt(i))) return null;

        while (i < texto.length() && Character.isLetter(texto.charAt(i))) {
            lexema += texto.charAt(i);
            i++;
        }

        if (reservadas.contains(lexema)) {
            return new Token(
                TipoToken.PALABRA_RESERVADA,
                lexema,
                fila,
                columna
            );
        }

        return new Token(TipoToken.IDENTIFICADOR, lexema, fila, columna);
    }
}
