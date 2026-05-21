package semantico;

import sintactico.NodoAST;

import java.util.ArrayList;
import java.util.List;

public class AnalizadorSemantico {

    private TablaSimbolos alcanceActual;
    private final List<String> errores;
    private final List<String> advertencias;
    private final List<EntradaTabla> tablaGlobal; // tabla de simbolos completa

    // Registro completo de cada simbolo para imprimir al final
    private static class EntradaTabla {
        String nombre;
        TipoSemantico tipo;
        int nivel;         // nivel de alcance (0 = global, 1 = main, 2 = bloque interno, ...)
        int fila;
        int columna;
        String valorInicial; // null si no tiene valor inicial

        EntradaTabla(String nombre, TipoSemantico tipo, int nivel, int fila, int columna, String valorInicial) {
            this.nombre       = nombre;
            this.tipo         = tipo;
            this.nivel        = nivel;
            this.fila         = fila;
            this.columna      = columna;
            this.valorInicial = valorInicial;
        }
    }

    public AnalizadorSemantico() {
        this.alcanceActual = new TablaSimbolos(null);
        this.errores       = new ArrayList<>();
        this.advertencias  = new ArrayList<>();
        this.tablaGlobal   = new ArrayList<>();
    }

    // ------------------------------------------------------------------
    //  Punto de entrada
    // ------------------------------------------------------------------

    public void analizar(NodoAST raiz) {
        if (raiz == null) return;
        visitarNodo(raiz);
    }

    // ------------------------------------------------------------------
    //  Despacho principal por tipo de nodo
    // ------------------------------------------------------------------

    private void visitarNodo(NodoAST nodo) {
        if (nodo == null) return;

        switch (nodo.tipo) {
            case "PROGRAMA":
                visitarHermanos(nodo.hijo);
                break;

            case "FUNCION_MAIN":
                alcanceActual = alcanceActual.abrirAlcance();
                visitarNodo(nodo.hijo);
                alcanceActual = alcanceActual.cerrarAlcance();
                break;

            case "BLOQUE":
                alcanceActual = alcanceActual.abrirAlcance();
                visitarHermanos(nodo.hijo);
                alcanceActual = alcanceActual.cerrarAlcance();
                break;

            case "DECLARACION_ENTERO":
            case "DECLARACION_FLOTANTE":
            case "DECLARACION_CADENA":
            case "DECLARACION_BOOLEANO":
            case "DECLARACION_VOID":
                visitarDeclaracion(nodo);
                break;

            case "ASIGNACION":
                visitarAsignacion(nodo);
                break;

            case "ESCRIBIR":
                visitarEscribir(nodo);
                break;

            case "SI":
                visitarSi(nodo);
                break;

            default:
                break;
        }
    }

    private void visitarHermanos(NodoAST primero) {
        NodoAST actual = primero;
        while (actual != null) {
            visitarNodo(actual);
            actual = actual.sig;
        }
    }

    // ------------------------------------------------------------------
    //  Declaración de variable
    // ------------------------------------------------------------------

    private void visitarDeclaracion(NodoAST nodo) {
        TipoSemantico tipoDeclarado = tipoDesdeNodo(nodo.tipo);

        NodoAST nodoId = nodo.hijo;
        if (nodoId == null || nodoId.valor == null) return;

        String nombre = nodoId.valor;
        int fila      = (nodoId.token != null) ? nodoId.token.fila    : -1;
        int columna   = (nodoId.token != null) ? nodoId.token.columna : -1;

        boolean ok = alcanceActual.declarar(nombre, tipoDeclarado, fila, columna);
        if (!ok) {
            errores.add(String.format(
                    "[Semántico] Redeclaración: '%s' ya fue declarada en este alcance (%d:%d)",
                    nombre, fila, columna));
            return;
        }

        // Extraer valor inicial si existe (es el hijo del nodo ID)
        NodoAST valorNodo = nodoId.hijo;
        String valorStr = null;
        if (valorNodo != null) {
            valorStr = valorNodo.valor != null ? valorNodo.valor : valorNodo.tipo;
            TipoSemantico tipoValor = getType(valorNodo);
            if (!typeEqual(tipoDeclarado, tipoValor)) {
                errores.add(String.format(
                        "[Semántico] Tipos incompatibles: '%s' es %s pero se asigna valor de tipo %s (%d:%d)",
                        nombre, tipoDeclarado, tipoValor, fila, columna));
            }
        }

        // Registrar en la tabla global con todos los datos
        tablaGlobal.add(new EntradaTabla(nombre, tipoDeclarado, alcanceActual.getNivel(), fila, columna, valorStr));
    }

    // ------------------------------------------------------------------
    //  Asignación
    // ------------------------------------------------------------------

    private void visitarAsignacion(NodoAST nodo) {
        NodoAST nodoId = nodo.hijo;
        if (nodoId == null || nodoId.valor == null) return;

        String nombre = nodoId.valor;
        int fila      = (nodoId.token != null) ? nodoId.token.fila    : -1;
        int columna   = (nodoId.token != null) ? nodoId.token.columna : -1;

        EntradaSimbolo simbolo = alcanceActual.buscar(nombre);
        if (simbolo == null) {
            errores.add(String.format(
                    "[Semántico] Variable no declarada: '%s' (%d:%d)",
                    nombre, fila, columna));
            return;
        }

        NodoAST expr = nodoId.hijo != null ? nodoId.hijo : nodo.sig;
        if (expr != null) {
            TipoSemantico tipoExpr = getType(expr);
            if (!typeEqual(simbolo.tipo, tipoExpr)) {
                errores.add(String.format(
                        "[Semántico] Tipos incompatibles en asignación: '%s' es %s pero expresión es %s (%d:%d)",
                        nombre, simbolo.tipo, tipoExpr, fila, columna));
            }
        }
    }

    // ------------------------------------------------------------------
    //  escribir(args...)
    // ------------------------------------------------------------------

    private void visitarEscribir(NodoAST nodo) {
        NodoAST args = nodo.hijo;
        if (args == null) return;

        NodoAST arg = args.hijo;
        while (arg != null) {
            verificarUsoDeVariables(arg);
            arg = arg.sig;
        }
    }

    // ------------------------------------------------------------------
    //  si (condicion) { bloque }
    // ------------------------------------------------------------------

    private void visitarSi(NodoAST nodo) {
        NodoAST condicion = nodo.hijo;
        NodoAST bloque    = (condicion != null) ? condicion.sig : null;

        if (condicion != null) {
            verificarUsoDeVariables(condicion);
            TipoSemantico tipoCond = getType(condicion);
            if (tipoCond == TipoSemantico.CADENA) {
                advertencias.add(
                        "[Semántico] Advertencia: condición de 'si' con tipo CADENA — puede ser error lógico");
            }
        }

        if (bloque != null) visitarNodo(bloque);
    }

    // ------------------------------------------------------------------
    //  GetType
    // ------------------------------------------------------------------

    public TipoSemantico getType(NodoAST nodo) {
        if (nodo == null) return TipoSemantico.DESCONOCIDO;

        switch (nodo.tipo) {
            case "NUMERO":
                if (nodo.valor != null && nodo.valor.contains("."))
                    return TipoSemantico.FLOTANTE;
                return TipoSemantico.ENTERO;

            case "CADENA":
                return TipoSemantico.CADENA;

            case "ID": {
                EntradaSimbolo simbolo = alcanceActual.buscar(nodo.valor);
                if (simbolo == null) return TipoSemantico.DESCONOCIDO;
                return simbolo.tipo;
            }

            case "REF": {
                EntradaSimbolo simbolo = alcanceActual.buscar(nodo.valor);
                if (simbolo == null) return TipoSemantico.DESCONOCIDO;
                return simbolo.tipo;
            }

            case "OP_+": case "OP_-": case "OP_*": case "OP_/": {
                TipoSemantico izq = getType(nodo.hijo);
                TipoSemantico der = getType(nodo.sig);
                return promoverTipo(izq, der);
            }

            case "OP_>": case "OP_<":
            case "OP_>=": case "OP_<=":
            case "OP_==": case "OP_!=":
            case "OP_&&": case "OP_||":
                return TipoSemantico.BOOLEANO;

            default:
                return TipoSemantico.DESCONOCIDO;
        }
    }

    // ------------------------------------------------------------------
    //  TypeEqual
    // ------------------------------------------------------------------

    public boolean typeEqual(TipoSemantico esperado, TipoSemantico actual) {
        if (esperado == TipoSemantico.DESCONOCIDO || actual == TipoSemantico.DESCONOCIDO)
            return true;
        if (esperado == actual) return true;
        if (esperado == TipoSemantico.FLOTANTE && actual == TipoSemantico.ENTERO)
            return true;
        return false;
    }

    // ------------------------------------------------------------------
    //  Helpers
    // ------------------------------------------------------------------

    private TipoSemantico tipoDesdeNodo(String tipoNodo) {
        switch (tipoNodo) {
            case "DECLARACION_ENTERO":   return TipoSemantico.ENTERO;
            case "DECLARACION_FLOTANTE": return TipoSemantico.FLOTANTE;
            case "DECLARACION_CADENA":   return TipoSemantico.CADENA;
            case "DECLARACION_BOOLEANO": return TipoSemantico.BOOLEANO;
            case "DECLARACION_VOID":     return TipoSemantico.VOID;
            default:                     return TipoSemantico.DESCONOCIDO;
        }
    }

    private TipoSemantico promoverTipo(TipoSemantico a, TipoSemantico b) {
        if (a == TipoSemantico.FLOTANTE || b == TipoSemantico.FLOTANTE)
            return TipoSemantico.FLOTANTE;
        if (a == TipoSemantico.ENTERO   || b == TipoSemantico.ENTERO)
            return TipoSemantico.ENTERO;
        return TipoSemantico.DESCONOCIDO;
    }

    private void verificarUsoDeVariables(NodoAST nodo) {
        if (nodo == null) return;

        if (nodo.tipo.equals("ID") || nodo.tipo.equals("REF")) {
            if (alcanceActual.buscar(nodo.valor) == null) {
                int fila    = (nodo.token != null) ? nodo.token.fila    : -1;
                int columna = (nodo.token != null) ? nodo.token.columna : -1;
                errores.add(String.format(
                        "[Semántico] Variable no declarada: '%s' (%d:%d)",
                        nodo.valor, fila, columna));
            }
        }

        verificarUsoDeVariables(nodo.hijo);
        verificarUsoDeVariables(nodo.sig);
    }

    // ------------------------------------------------------------------
    //  Salida de resultados
    // ------------------------------------------------------------------

    public boolean hayErrores() { return !errores.isEmpty(); }

    public List<String> getErrores()      { return errores; }
    public List<String> getAdvertencias() { return advertencias; }

    public void imprimirResultados() {
        System.out.println("\n===== TABLA DE SIMBOLOS (SEMANTICO) =====");

        if (tablaGlobal.isEmpty()) {
            System.out.println("  (vacío)");
        } else {
            // Encabezado
            System.out.printf("%-5s  %-15s  %-12s  %-7s  %-6s  %-7s  %-20s%n",
                    "No.", "NOMBRE", "TIPO", "ALCANCE", "FILA", "COLUMNA", "VALOR INICIAL");
            System.out.println("-".repeat(80));

            // Filas
            int num = 1;
            for (EntradaTabla e : tablaGlobal) {
                System.out.printf("%-5d  %-15s  %-12s  %-7d  %-6d  %-7d  %-20s%n",
                        num++,
                        e.nombre,
                        e.tipo,
                        e.nivel,
                        e.fila,
                        e.columna,
                        e.valorInicial != null ? e.valorInicial : "—");
            }
            System.out.println("-".repeat(80));
            System.out.println("Total de simbolos: " + tablaGlobal.size());
        }

        System.out.println("\n========== TABLA DE ERRORES SEMANTICOS ==========");
        if (errores.isEmpty() && advertencias.isEmpty()) {
            System.out.println("  No se encontraron errores semánticos.");
        } else {
            int num = 1;
            for (String adv : advertencias) {
                System.out.printf("  %-3d %s%n", num++, adv);
            }
            for (String err : errores) {
                System.out.printf("  %-3d %s%n", num++, err);
            }
        }
        System.out.println("=================================================");
    }
}