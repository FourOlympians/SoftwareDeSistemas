package semantico;

/**
 * Representa una variable declarada en el programa.
 * Cada entrada guarda el nombre, tipo y posición donde se declaró.
 */
public class EntradaSimbolo {

    public final String nombre;
    public final TipoSemantico tipo;
    public final int filaDeclaracion;
    public final int columnaDeclaracion;

    public EntradaSimbolo(String nombre, TipoSemantico tipo, int fila, int columna) {
        this.nombre              = nombre;
        this.tipo                = tipo;
        this.filaDeclaracion     = fila;
        this.columnaDeclaracion  = columna;
    }

    @Override
    public String toString() {
        return String.format("%-15s %-10s (declarada en %d:%d)",
                nombre, tipo, filaDeclaracion, columnaDeclaracion);
    }
}
