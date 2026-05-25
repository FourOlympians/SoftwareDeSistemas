package semantico;

import java.util.HashMap;
import java.util.Map;

/**
 * Tabla de símbolos con soporte de alcances (scopes).
 *
 * Cada vez que entramos a un bloque abrimos un nuevo alcance,
 * y al salir lo cerramos. La búsqueda sube por la cadena de
 * padres hasta encontrar la variable.
 */
public class TablaSimbolos {

    private final Map<String, EntradaSimbolo> simbolos;
    private final TablaSimbolos padre;
    private final int nivel;

    public TablaSimbolos(TablaSimbolos padre) {
        this.simbolos = new HashMap<>();
        this.padre    = padre;
        this.nivel    = (padre == null) ? 0 : padre.nivel + 1;
    }

    /** Abre un nuevo alcance hijo (al entrar a un bloque). */
    public TablaSimbolos abrirAlcance() {
        return new TablaSimbolos(this);
    }

    /** Cierra el alcance actual y regresa al padre. */
    public TablaSimbolos cerrarAlcance() {
        return padre;
    }

    /**
     * Declara una variable en el alcance actual.
     *
     * @param inicializada true si la declaración incluye valor (entero x = 5),
     *                     false si es solo declaración (entero x;)
     * @return false si ya existía en ESTE alcance (redeclaración).
     */
    public boolean declarar(String nombre, TipoSemantico tipo, int fila, int columna, boolean inicializada) {
        if (simbolos.containsKey(nombre)) return false;
        simbolos.put(nombre, new EntradaSimbolo(nombre, tipo, fila, columna, inicializada));
        return true;
    }

    /**
     * Busca una variable subiendo por los alcances.
     * @return null si no existe en ningún alcance visible.
     */
    public EntradaSimbolo buscar(String nombre) {
        EntradaSimbolo entrada = simbolos.get(nombre);
        if (entrada != null) return entrada;
        if (padre != null) return padre.buscar(nombre);
        return null;
    }

    /**
     * Marca una variable como inicializada (después de una asignación).
     * Sube por la cadena de alcances igual que buscar().
     */
    public void marcarInicializada(String nombre) {
        EntradaSimbolo entrada = simbolos.get(nombre);
        if (entrada != null) {
            entrada.inicializada = true;
            return;
        }
        if (padre != null) padre.marcarInicializada(nombre);
    }

    public void imprimir() {
        if (simbolos.isEmpty()) {
            System.out.println("  (vacío)");
            return;
        }
        for (EntradaSimbolo e : simbolos.values()) {
            System.out.println("  " + "  ".repeat(nivel) + e);
        }
    }

    public int getNivel() { return nivel; }
}