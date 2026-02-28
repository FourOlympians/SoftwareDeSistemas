package lexico;

public class AutomataSimbolo {

    public static Token reconocer(String texto, int i, int fila, int columna) {

        if (i + 1 < texto.length()) {
            String dos = texto.substring(i, i + 2);

            if (dos.equals("->") || dos.equals("<=") || dos.equals(">=") ||
                    dos.equals("==") || dos.equals("!=") || dos.equals("&&")) {
                return new Token(TipoToken.SIMBOLO, dos, fila, columna);
            }
        }

        char c = texto.charAt(i);
        String simbolos = "()+-*/<>{};,=";
        if (simbolos.indexOf(c) != -1) {
            return new Token(TipoToken.SIMBOLO, String.valueOf(c), fila, columna);
        }

        return null;
    }
}