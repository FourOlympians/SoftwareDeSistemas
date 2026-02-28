package lexico;

public class TablaErrores {

    private ErrorLexico[] errores;
    private int cantidad;

    public TablaErrores() {
        this.errores  = new ErrorLexico[100];
        this.cantidad = 0;
    }

    // Este metodo es el que llaman los automatas y el AnalizadorLexico
    // cuando encuentran un error. Guarda el error y sigue.
    public void agregar(TipoError tipo, String lexema, int fila, int columna, String descripcion) {

        // Si la tabla se llenó, duplica su capacidad
        if (cantidad >= errores.length) {
            ErrorLexico[] nueva = new ErrorLexico[errores.length * 2];
            for (int i = 0; i < errores.length; i++) {
                nueva[i] = errores[i];
            }
            errores = nueva;
        }

        errores[cantidad] = new ErrorLexico(tipo, lexema, fila, columna, descripcion);
        cantidad++;
    }

    public boolean hayErrores() {
        return cantidad > 0;
    }

    public int getCantidad() {
        return cantidad;
    }

    // Imprime la tabla completa al final del analisis
    public void imprimir() {

        System.out.println("\n========== TABLA DE ERRORES LEXICOS ==========");

        if (cantidad == 0) {
            System.out.println("  No se encontraron errores lexicos.");
            System.out.println("==============================================");
            return;
        }

        System.out.printf("%-5s  %-28s  %-20s  %-6s  %-7s%n",
                "No.", "TIPO", "LEXEMA", "FILA", "COLUMNA");
        System.out.println("----------------------------------------------" +
                           "----------------------------------------------");

        for (int i = 0; i < cantidad; i++) {
            ErrorLexico e = errores[i];
            System.out.printf("%-5d  %-28s  %-20s  %-6d  %-7d%n",
                    (i + 1),
                    e.tipo,
                    "'" + e.lexema + "'",
                    e.fila,
                    e.columna);
            System.out.printf("       Descripcion: %s%n", e.descripcion);
            System.out.println();
        }

        System.out.println("Total de errores: " + cantidad);
        System.out.println("==============================================");
    }
}