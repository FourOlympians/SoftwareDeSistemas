package semantico;

import sintactico.NodoAST;

import java.util.ArrayList;
import java.util.List;

public class AnalizadorSemantico {

    private TablaSimbolos alcanceActual;
    private final List<String> errores;
    private final List<String> advertencias;
    private final List<EntradaTabla> tablaGlobal;

    // Registro completo de cada símbolo para imprimir al final
    private static class EntradaTabla {
        String nombre;
        TipoSemantico tipo;
        int nivel;
        int fila;
        int columna;
        String valorInicial;
        boolean inicializada;

        EntradaTabla(String nombre, TipoSemantico tipo, int nivel,
                     int fila, int columna, String valorInicial, boolean inicializada) {
            this.nombre       = nombre;
            this.tipo         = tipo;
            this.nivel        = nivel;
            this.fila         = fila;
            this.columna      = columna;
            this.valorInicial = valorInicial;
            this.inicializada = inicializada;
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

        // ¿Tiene valor inicial?
        NodoAST valorNodo    = nodoId.hijo;
        boolean tieneValor   = (valorNodo != null);
        String  valorStr     = null;

        if (tieneValor) {
            valorStr = valorNodo.valor != null ? valorNodo.valor : valorNodo.tipo;

            // Verificar que las variables usadas en el valor inicial estén inicializadas
            verificarUsoDeVariables(valorNodo);

            // Verificar compatibilidad de tipos
            TipoSemantico tipoValor = getType(valorNodo);
            if (!typeEqual(tipoDeclarado, tipoValor)) {
                errores.add(String.format(
                        "[Semántico] Tipos incompatibles: '%s' es %s pero se asigna valor de tipo %s (%d:%d)",
                        nombre, tipoDeclarado, tipoValor, fila, columna));
            }
        }

        // Declarar en tabla de símbolos — inicializada solo si tiene valor
        boolean ok = alcanceActual.declarar(nombre, tipoDeclarado, fila, columna, tieneValor);
        if (!ok) {
            errores.add(String.format(
                    "[Semántico] Redeclaración: '%s' ya fue declarada en este alcance (%d:%d)",
                    nombre, fila, columna));
            return;
        }

        tablaGlobal.add(new EntradaTabla(
                nombre, tipoDeclarado, alcanceActual.getNivel(),
                fila, columna, valorStr, tieneValor));
    }

    // ------------------------------------------------------------------
    //  Asignación  (simple = y compuesta += -= *= /=)
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

        // Obtener la expresión del lado derecho
        NodoAST expr = nodoId.hijo;

        if (expr != null) {
            String opNodo = expr.tipo; // puede ser OP_+, OP_-, etc. o un valor directo

            boolean esCompuesta = opNodo != null && (
                    opNodo.equals("OP_+") || opNodo.equals("OP_-") ||
                            opNodo.equals("OP_*") || opNodo.equals("OP_/"));

            if (esCompuesta) {
                // x += 1  →  el nodo OP_+ tiene como hijo izq la propia x
                // Antes de usarla como operando izquierdo, verificar que esté inicializada
                if (!simbolo.inicializada) {
                    errores.add(String.format(
                            "[Semántico] Variable '%s' usada antes de ser asignada " +
                                    "— la asignación compuesta '%s' requiere un valor previo (%d:%d)",
                            nombre, opNodo.replace("OP_", "") + "=", fila, columna));
                }
                // Verificar también el operando derecho de la operación
                NodoAST operandoDer = expr.sig;
                if (operandoDer != null) verificarUsoDeVariables(operandoDer);
            } else {
                // Asignación simple: verificar que las variables del RHS estén inicializadas
                verificarUsoDeVariables(expr);
            }

            // Verificar compatibilidad de tipos
            TipoSemantico tipoExpr = getType(expr);
            if (!typeEqual(simbolo.tipo, tipoExpr)) {
                errores.add(String.format(
                        "[Semántico] Tipos incompatibles en asignación: '%s' es %s pero expresión es %s (%d:%d)",
                        nombre, simbolo.tipo, tipoExpr, fila, columna));
            }
        }

        // Después de una asignación, la variable queda inicializada
        alcanceActual.marcarInicializada(nombre);
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
    //  Verificar uso de variables (recorre expresiones buscando ID/REF)
    // ------------------------------------------------------------------

    /**
     * Recorre recursivamente un subárbol de expresión.
     * Para cada ID o REF encontrado verifica:
     *   1. ¿Está declarado?
     *   2. ¿Está inicializado?
     */
    private void verificarUsoDeVariables(NodoAST nodo) {
        if (nodo == null) return;

        if (nodo.tipo.equals("ID") || nodo.tipo.equals("REF")) {
            int fila    = (nodo.token != null) ? nodo.token.fila    : -1;
            int columna = (nodo.token != null) ? nodo.token.columna : -1;

            EntradaSimbolo simbolo = alcanceActual.buscar(nodo.valor);

            if (simbolo == null) {
                errores.add(String.format(
                        "[Semántico] Variable no declarada: '%s' (%d:%d)",
                        nodo.valor, fila, columna));
            } else if (!simbolo.inicializada) {
                // ← NUEVA VERIFICACIÓN
                errores.add(String.format(
                        "[Semántico] Variable '%s' usada antes de ser asignada (%d:%d)",
                        nodo.valor, fila, columna));
            }
        }

        // Recorrer hijo y hermano siguiente
        verificarUsoDeVariables(nodo.hijo);
        verificarUsoDeVariables(nodo.sig);
    }

    // ------------------------------------------------------------------
    //  Inferencia de tipos
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
    //  Compatibilidad de tipos
    // ------------------------------------------------------------------

    public boolean typeEqual(TipoSemantico esperado, TipoSemantico actual) {
        if (esperado == TipoSemantico.DESCONOCIDO || actual == TipoSemantico.DESCONOCIDO)
            return true;
        if (esperado == actual) return true;
        // Promoción implícita: entero cabe en flotante
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
            System.out.printf("%-5s  %-15s  %-12s  %-7s  %-6s  %-7s  %-22s  %-13s%n",
                    "No.", "NOMBRE", "TIPO", "ALCANCE", "FILA", "COLUMNA",
                    "VALOR INICIAL", "ESTADO");
            System.out.println("-".repeat(95));

            int num = 1;
            for (EntradaTabla e : tablaGlobal) {
                System.out.printf("%-5d  %-15s  %-12s  %-7d  %-6d  %-7d  %-22s  %-13s%n",
                        num++,
                        e.nombre,
                        e.tipo,
                        e.nivel,
                        e.fila,
                        e.columna,
                        e.valorInicial != null ? e.valorInicial : "—",
                        e.inicializada ? "inicializada" : "sin valor");
            }
            System.out.println("-".repeat(95));
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
            System.out.println("\nTotal: " + (errores.size() + advertencias.size()) +
                    " problema(s) encontrado(s).");
        }
        System.out.println("=================================================");
    }
}