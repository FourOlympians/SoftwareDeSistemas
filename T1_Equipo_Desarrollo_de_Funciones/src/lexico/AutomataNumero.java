package lexico;

public class AutomataNumero {

    // --- AGREGADO: cuantos caracteres se consumieron aunque haya error ---
    // AnalizadorLexico lo lee para saber cuanto avanzar cuando hay NUMERO_MAL_FORMADO
    public static int caracteresConsumidos = 0;

    public static Token reconocer(String texto, int inicio, int fila, int columna,
                                  TablaErrores tablaErrores) {

        // --- AGREGADO: reiniciar en cada llamado ---
        caracteresConsumidos = 0;

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
                tablaErrores.agregar(
                        TipoError.NUMERO_MAL_FORMADO,
                        lexema,
                        fila,
                        columna,
                        "Numero con punto decimal pero sin digitos despues: " + lexema
                );
                // --- AGREGADO: avisamos cuantos caracteres consumimos (ej: "3." = 2) ---
                caracteresConsumidos = lexema.length();
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