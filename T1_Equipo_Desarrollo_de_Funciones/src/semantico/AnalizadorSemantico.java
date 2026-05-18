package semantico;

import sintactico.NodoAST;

import java.util.ArrayList;
import java.util.List;

/**
 * Analizador semántico que recorre el AST y verifica:
 *  - Variables declaradas antes de usarse
 *  - Variables no redeclaradas en el mismo alcance
 *  - Compatibilidad de tipos en asignaciones y operaciones (TypeEqual)
 *  - Inferencia del tipo de una expresión (GetType)
 */
public class AnalizadorSemantico {

    private TablaSimbolos alcanceActual;
    private final List<String> errores;
    private final List<String> advertencias;

    public AnalizadorSemantico() {
        this.alcanceActual = new TablaSimbolos(null); // alcance global
        this.errores       = new ArrayList<>();
        this.advertencias  = new ArrayList<>();
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
                visitarNodo(nodo.hijo);     // el BLOQUE
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
                // nodos de expresión los maneja GetType cuando se necesitan
                break;
        }
    }

    /** Recorre la lista de hermanos (nodos al mismo nivel). */
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

        // nodo.hijo es el ID
        NodoAST nodoId = nodo.hijo;
        if (nodoId == null || nodoId.valor == null) return;

        String nombre = nodoId.valor;
        int fila      = (nodoId.token != null) ? nodoId.token.fila    : -1;
        int columna   = (nodoId.token != null) ? nodoId.token.columna : -1;

        // Verificar redeclaración en este alcance
        boolean ok = alcanceActual.declarar(nombre, tipoDeclarado, fila, columna);
        if (!ok) {
            errores.add(String.format(
                "[Semántico] Redeclaración: '%s' ya fue declarada en este alcance (%d:%d)",
                nombre, fila, columna));
            return;
        }

        // Si hay valor inicial, verificar compatibilidad de tipos
        NodoAST valorInicial = nodoId.hijo;
        if (valorInicial != null) {
            TipoSemantico tipoValor = getType(valorInicial);
            if (!typeEqual(tipoDeclarado, tipoValor)) {
                errores.add(String.format(
                    "[Semántico] Tipos incompatibles: '%s' es %s pero se asigna valor de tipo %s (%d:%d)",
                    nombre, tipoDeclarado, tipoValor, fila, columna));
            }
        }
    }

    // ------------------------------------------------------------------
    //  Asignación  x = expr  /  x += expr  etc.
    // ------------------------------------------------------------------

    private void visitarAsignacion(NodoAST nodo) {
        // Estructura: ASIGNACION -> hijo=ID  (el ID tiene hijo=valor o el sig es OP_*)
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

        // El valor puede estar en nodoId.hijo (asignación simple)
        // o en nodo.sig si viene de una operación compuesta
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
        // Verificamos que cada argumento sea una expresión válida
        NodoAST args = nodo.hijo;   // nodo ARGUMENTOS
        if (args == null) return;

        NodoAST arg = args.hijo;
        while (arg != null) {
            // Sólo verificamos que los identificadores usados existan
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
            // La condición debería ser booleana o comparación numérica
            TipoSemantico tipoCond = getType(condicion);
            if (tipoCond == TipoSemantico.CADENA) {
                advertencias.add(String.format(
                    "[Semántico] Advertencia: condición de 'si' con tipo CADENA — puede ser error lógico"));
            }
        }

        if (bloque != null) visitarNodo(bloque);
    }

    // ------------------------------------------------------------------
    //  GetType — infiere el tipo de una expresión
    // ------------------------------------------------------------------

    /**
     * Infiere el tipo semántico de un nodo de expresión.
     * Implementa el algoritmo GetType del análisis semántico clásico.
     */
    public TipoSemantico getType(NodoAST nodo) {
        if (nodo == null) return TipoSemantico.DESCONOCIDO;

        switch (nodo.tipo) {
            case "NUMERO": {
                // Si contiene punto es flotante, si no es entero
                if (nodo.valor != null && nodo.valor.contains("."))
                    return TipoSemantico.FLOTANTE;
                return TipoSemantico.ENTERO;
            }

            case "CADENA":
                return TipoSemantico.CADENA;

            case "ID": {
                EntradaSimbolo simbolo = alcanceActual.buscar(nodo.valor);
                if (simbolo == null) {
                    // El error de variable no declarada se reporta en visitarAsignacion
                    // Aquí sólo devolvemos DESCONOCIDO para no duplicar mensajes
                    return TipoSemantico.DESCONOCIDO;
                }
                return simbolo.tipo;
            }

            case "REF": {
                // &x — misma búsqueda que ID
                EntradaSimbolo simbolo = alcanceActual.buscar(nodo.valor);
                if (simbolo == null) return TipoSemantico.DESCONOCIDO;
                return simbolo.tipo;
            }

            // Operaciones aritméticas: el tipo resulta del operando de mayor precedencia
            case "OP_+": case "OP_-": case "OP_*": case "OP_/": {
                TipoSemantico izq = getType(nodo.hijo);
                TipoSemantico der = getType(nodo.sig);
                return promoverTipo(izq, der);
            }

            // Comparaciones siempre producen un booleano
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
    //  TypeEqual — verifica compatibilidad de tipos
    // ------------------------------------------------------------------

    /**
     * Comprueba si dos tipos son compatibles para una asignación.
     * Implementa el algoritmo TypeEqual del análisis semántico clásico.
     *
     * Reglas de compatibilidad:
     *  - Mismo tipo siempre es válido.
     *  - ENTERO se puede asignar a FLOTANTE (promoción implícita).
     *  - Todo lo demás es incompatible.
     */
    public boolean typeEqual(TipoSemantico esperado, TipoSemantico actual) {
        if (esperado == TipoSemantico.DESCONOCIDO || actual == TipoSemantico.DESCONOCIDO)
            return true;   // ya hay otro error reportado, no duplicamos

        if (esperado == actual) return true;

        // Promoción implícita: int → float
        if (esperado == TipoSemantico.FLOTANTE && actual == TipoSemantico.ENTERO)
            return true;

        return false;
    }

    // ------------------------------------------------------------------
    //  Helpers
    // ------------------------------------------------------------------

    /** Convierte el string del tipo de nodo AST al enum TipoSemantico. */
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

    /**
     * Regla de promoción de tipo para operaciones binarias.
     * FLOTANTE tiene mayor precedencia que ENTERO.
     */
    private TipoSemantico promoverTipo(TipoSemantico a, TipoSemantico b) {
        if (a == TipoSemantico.FLOTANTE || b == TipoSemantico.FLOTANTE)
            return TipoSemantico.FLOTANTE;
        if (a == TipoSemantico.ENTERO   || b == TipoSemantico.ENTERO)
            return TipoSemantico.ENTERO;
        return TipoSemantico.DESCONOCIDO;
    }

    /** Recorre un subárbol buscando IDs no declarados (sin cambiar el tipo). */
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
        alcanceActual.imprimir();

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
