package lexico;

public class Token {

    public TipoToken tipo;
    public String lexema;
    public String value;
    public int fila;
    public int columna;

    // constructor para analizador léxico
    public Token(TipoToken tipo, String lexema, int fila, int columna) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.value = lexema;
        this.fila = fila;
        this.columna = columna;
    }

    // constructor que ya usabas en pruebas
    public Token(int v) {
        this.value = String.valueOf(v);
    }

    public String toString() {
        return tipo + " -> " + lexema + " (" + fila + "," + columna + ")";
    }
}