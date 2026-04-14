package lexico;

import lexico.TipoError;
import lexico.TipoToken;

public class AutomataSimbolo {

    public static Token reconocer(String texto, int i, int fila, int columna) {
        if (i + 1 < texto.length()) {
            String dos = texto.substring(i, i + 2);

            if (
                dos.equals("++") ||
                dos.equals("--") ||
                dos.equals("||") ||
                dos.equals("&&") ||
                dos.equals("->")
            ) {
                return new Token(
                    TipoToken.SIMBOLO_COMPUESTO,
                    dos,
                    fila,
                    columna
                );
            }

            if (
                dos.equals(">=") ||
                dos.equals("<=") ||
                dos.equals("==") ||
                dos.equals("!=")
            ) {
                return new Token(
                    TipoToken.SIMBOLO_COMPARISON,
                    dos,
                    fila,
                    columna
                );
            }

            if (
                dos.equals("+=") ||
                dos.equals("-=") ||
                dos.equals("*=") ||
                dos.equals("/=")
            ) {
                return new Token(TipoToken.SIMBOLO_ASSIGN, dos, fila, columna);
            }
        }

        char c = texto.charAt(i);

        if (c == '>' || c == '<') {
            return new Token(
                TipoToken.SIMBOLO_COMPARISON,
                String.valueOf(c),
                fila,
                columna
            );
        }

        if (c == '+' || c == '-' || c == '*' || c == '/') {
            return new Token(
                TipoToken.OP_ARITMETICO,
                String.valueOf(c),
                fila,
                columna
            );
        }

        if (c == '=') {
            return new Token(
                TipoToken.SIMBOLO_ASSIGN,
                String.valueOf(c),
                fila,
                columna
            );
        }

        if (
            c == '(' ||
            c == ')' ||
            c == '{' ||
            c == '}' ||
            c == ';' ||
            c == ',' ||
            c == '&'
        ) {
            return new Token(
                TipoToken.SIMBOLO,
                String.valueOf(c),
                fila,
                columna
            );
        }

        return null;
    }
}
