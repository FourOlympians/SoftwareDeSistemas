package semantico;

/**
 * Representa una variable declarada en el programa.
 * Guarda nombre, tipo, posición y si ya recibió un valor.
 */
public class EntradaSimbolo {

    public final String nombre;
    public final TipoSemantico tipo;
    public final int filaDeclaracion;
    public final int columnaDeclaracion;

    /**
     * true  → la variable tiene un valor asignado (declaración con valor,
     *         asignación simple, o asignación compuesta).
     * false → fue declarada pero nunca se le asignó nada todavía.
     */
    public boolean inicializada;

    public EntradaSimbolo(String nombre, TipoSemantico tipo, int fila, int columna, boolean inicializada) {
        this.nombre              = nombre;
        this.tipo                = tipo;
        this.filaDeclaracion     = fila;
        this.columnaDeclaracion  = columna;
        this.inicializada        = inicializada;
    }

    @Override
    public String toString() {
        return String.format("%-15s %-10s %-13s (declarada en %d:%d)",
                nombre, tipo,
                inicializada ? "[inicializada]" : "[sin valor]",
                filaDeclaracion, columnaDeclaracion);
    }
}