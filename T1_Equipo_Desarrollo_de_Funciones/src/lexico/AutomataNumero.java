package lexico;

public class AutomataNumero {

    public static Token reconocer(String texto, int inicio, int fila, int columna) {

        int i = inicio;
        String lexema = "";

        if (texto.charAt(i) == '-') {
            lexema += "-";
            i++;
        }

        boolean tieneDigitos = false;

        while (i < texto.length() && Character.isDigit(texto.charAt(i))) {
            lexema += texto.charAt(i);
            i++;
            tieneDigitos = true;
        }

        if (!tieneDigitos) return null;

        if (i < texto.length() && texto.charAt(i) == '.') {
            lexema += ".";
            i++;

            if (i >= texto.length() || !Character.isDigit(texto.charAt(i))) {
                return null;
            }

            while (i < texto.length() && Character.isDigit(texto.charAt(i))) {
                lexema += texto.charAt(i);
                i++;
            }

            return new Token(TipoToken.NUMERO_DECIMAL, lexema, fila, columna);
        }

        return new Token(TipoToken.NUMERO_ENTERO, lexema, fila, columna);
    }
}