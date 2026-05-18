package semantico;

/**
 * Tipos que el analizador semántico entiende.
 * Se mapean desde las palabras reservadas del lenguaje.
 */
public enum TipoSemantico {
    ENTERO,
    FLOTANTE,
    CADENA,
    BOOLEANO,
    VOID,
    DESCONOCIDO   // cuando no se puede inferir / hay error
}
