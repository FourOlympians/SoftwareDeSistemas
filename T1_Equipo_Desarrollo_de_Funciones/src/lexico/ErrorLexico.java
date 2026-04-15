package lexico;

public class ErrorLexico {

    public TipoError tipo;
    public String lexema;
    public int fila;
    public int columna;
    public String descripcion;

    public ErrorLexico(TipoError tipo, String lexema, int fila, int columna, String descripcion) {
        this.tipo        = tipo;
        this.lexema      = lexema;
        this.fila        = fila;
        this.columna     = columna;
        this.descripcion = descripcion;
    }
}