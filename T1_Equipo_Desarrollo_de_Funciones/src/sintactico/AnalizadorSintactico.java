package sintactico;

import java.util.ArrayList;
import java.util.List;
import lexico.LinkedList;
import lexico.Nodo;
import lexico.TablaErrores;
import lexico.TipoError;
import lexico.TipoToken;
import lexico.Token;

public class AnalizadorSintactico {

    private TablaErrores tablaErrores;
    private LinkedList tablaTokens;
    private Nodo actual;
    private List<String> errores;

    public AnalizadorSintactico(TablaErrores tablaErrores) {
        this.tablaErrores = tablaErrores;
        this.errores = new ArrayList<>();
    }

    public AbstractTree analizar(LinkedList tabla) {
        this.tablaTokens = tabla;
        this.actual = tabla.obtenerHead();
        this.errores.clear();

        NodoAST raiz = programa();

        return new AbstractTree(raiz);
    }

    public List<String> getErrores() {
        return errores;
    }

    private Token peek() {
        return actual != null ? actual.data : null;
    }

    private Token next() {
        Token t = peek();
        if (actual != null) actual = actual.obtenerSiguiente();
        return t;
    }

    private void agregarErrorSintactico(String esperado, String encontrado, int fila, int columna) {
        String lexema = encontrado != null ? encontrado : "EOF";
        String descripcion = "Se esperaba " + esperado + ", encontro '" + lexema + "'";
        tablaErrores.agregar(TipoError.ERROR_SINTACTICO, lexema, fila, columna, descripcion);
        errores.add("Error Sintactico: " + descripcion + " (linea " + fila + ")");
    }

    private void agregarErrorSintactico(String esperado, String encontrado) {
        Token t = peek();
        int fila = (t != null) ? t.fila : -1;
        int columna = (t != null) ? t.columna : -1;
        agregarErrorSintactico(esperado, encontrado, fila, columna);
    }

    private boolean esTipo(Token t) {
        if (t == null) return false;
        if (t.tipo == TipoToken.PALABRA_RESERVADA) {
            String lex = t.lexema.toLowerCase();
            return (
                lex.equals("entero") ||
                lex.equals("flotante") ||
                lex.equals("cadena") ||
                lex.equals("booleano") ||
                lex.equals("void")
            );
        }
        return false;
    }

    private NodoAST programa() {
        NodoAST raiz = new NodoAST("PROGRAMA");
        NodoAST ultimoHermano = null;

        while (peek() != null) {
            NodoAST decl = declaracion();
            if (decl != null) {
                if (raiz.hijo == null) {
                    raiz.hijo = decl;
                } else {
                    ultimoHermano.sig = decl;
                }
                ultimoHermano = decl;
            } else {
                Token t = peek();
                if (t != null) {
                    agregarErrorSintactico("inicio de una declaracion valida", t.lexema);
                }
                next();
            }
        }
        return raiz;
    }

    private NodoAST declaracion() {
        Token t = peek();
        if (t == null) return null;

        if (t.tipo == TipoToken.PALABRA_RESERVADA) {
            if (t.lexema.equals("escribir")) {
                return escribir();
            }
            if (t.lexema.equals("si")) {
                return si();
            }
            if (esTipo(t)) {
                return declaracionVariable();
            }
            agregarErrorSintactico("identificador, tipo de dato, 'escribir' o 'si'", t.lexema);
            return null;
        }

        if (t.tipo == TipoToken.IDENTIFICADOR) {
            return asignacion();
        }

        return null;
    }

    private NodoAST declaracionVariable() {
        Token tipoTok = peek();
        String tipoDecl = null;

        if (tipoTok.lexema.equals("entero")) tipoDecl = "DECLARACION_ENTERO";
        else if (tipoTok.lexema.equals("flotante")) tipoDecl = "DECLARACION_FLOTANTE";
        else if (tipoTok.lexema.equals("cadena")) tipoDecl = "DECLARACION_CADENA";
        else if (tipoTok.lexema.equals("booleano")) tipoDecl = "DECLARACION_BOOLEANO";
        else if (tipoTok.lexema.equals("void")) tipoDecl = "DECLARACION_VOID";

        next();

        Token id = peek();
        if (id == null) {
            agregarErrorSintactico("identificador", null);
            return new NodoAST(tipoDecl);
        }

        if (id.lexema.equals("main")) {
            next();
            if (peek() == null || !peek().lexema.equals("(")) {
                agregarErrorSintactico("'('", peek() != null ? peek().lexema : null);
                return new NodoAST("FUNCION_MAIN");
            }
            next();
            while (peek() != null && !peek().lexema.equals(")")) next();
            if (peek() == null) {
                agregarErrorSintactico("')'", null);
                return new NodoAST("FUNCION_MAIN");
            }
            next();
            NodoAST bloque = bloque();
            if (bloque == null || (bloque.hijo == null && bloque != null)) {
                agregarErrorSintactico("bloque '{...}' con contenido", peek() != null ? peek().lexema : null);
            }
            return new NodoAST("FUNCION_MAIN", bloque, null);
        } 

        if (id.tipo != TipoToken.IDENTIFICADOR) {
            agregarErrorSintactico("identificador", id.lexema);
            return null;
        }
        next();

        NodoAST nodoId = new NodoAST("ID", id.lexema);
        nodoId.token = id;
        String nombre = id.lexema; 

        NodoAST valor = null;
        if (
            peek() != null &&
            peek().tipo == TipoToken.SIMBOLO_ASSIGN &&
            peek().lexema.equals("=")
        ) {
            next();
            valor = expresion();
            if (valor == null) {
                agregarErrorSintactico("expresion", peek() != null ? peek().lexema : null);
            }
            nodoId.hijo = valor;
        } else {
            next();
            // if (peek() == null || !peek().lexema.equals("(")) {
            //     agregarErrorSintactico("'('", peek() != null ? peek().lexema : null);
            //     return new NodoAST("Funcion" + nombre);
            // }
            // next();
            // while (peek() != null && !peek().lexema.equals(")")) next();
            // if (peek() == null) {
            //     agregarErrorSintactico("')'", null);
            //     return new NodoAST("Funcion" + nombre);
            // }
            next();
            NodoAST bloque = bloque();
            if (bloque == null || (bloque.hijo == null && bloque != null)) {
                agregarErrorSintactico("bloque '{...}' con contenido", peek() != null ? peek().lexema : null);
            }

            return new NodoAST("Funcion" + nombre, bloque, null);           
            // if (peek() == null || peek().lexema != "(" ) {
            //     agregarErrorSintactico("'('", peek() != null ? peek().lexema : null);
            //     return new NodoAST("Funcion" + peek().lexema);
            // }
            // next();
            // while (peek() != null && !peek().lexema.equals(")")) next();
            // if (peek() == null) {
            //     agregarErrorSintactico("')'", null);
            //     return new NodoAST("Funcion" + peek().lexema);
            // }
            // next();
            // NodoAST bloque = bloque();
            // if (bloque == null || (bloque.hijo == null && bloque != null)) {
            //     agregarErrorSintactico("bloque '{...}' con contenido", peek() != null ? peek().lexema : null);
            // }
            // return new NodoAST("Funcion" + peek().lexema, bloque, null);

        }

        if (peek() != null && peek().lexema.equals(";")) {
            next();
        } else if (peek() != null) {
            agregarErrorSintactico("';'", peek().lexema);
        } else {
            agregarErrorSintactico("';'", null);
        }

        return new NodoAST(tipoDecl, nodoId, null);
    }

    private NodoAST asignacion() {
        Token id = peek();
        if (id == null || id.tipo != TipoToken.IDENTIFICADOR) return null;
        next();

        NodoAST nodoId = new NodoAST("ID", id.lexema);
        nodoId.token = id;

        Token op = peek();
        if (op == null) {
            agregarErrorSintactico("operador de asignacion o ';'", null);
            return nodoId;
        }

        if (op.tipo == TipoToken.SIMBOLO_ASSIGN) {
            if (op.lexema.equals("=")) {
                next();
                NodoAST valor = expresion();
                if (valor == null) {
                    agregarErrorSintactico("expresion", peek() != null ? peek().lexema : null);
                }
                if (peek() != null && peek().lexema.equals(";")) next();
                else if (peek() != null) {
                    agregarErrorSintactico("';'", peek().lexema);
                } else {
                    agregarErrorSintactico("';'", null);
                }
                nodoId.hijo = valor;
                return new NodoAST("ASIGNACION", nodoId, null);
            }
            if (
                op.lexema.equals("+=") ||
                op.lexema.equals("-=") ||
                op.lexema.equals("*=") ||
                op.lexema.equals("/=")
            ) {
                String opLex = op.lexema;
                next();
                NodoAST valor = expresion();
                if (valor == null) {
                    agregarErrorSintactico("expresion", peek() != null ? peek().lexema : null);
                }
                if (peek() != null && peek().lexema.equals(";")) next();
                else if (peek() != null) {
                    agregarErrorSintactico("';'", peek().lexema);
                } else {
                    agregarErrorSintactico("';'", null);
                }
                String opName = "OP_" + opLex.substring(0, 1);
                NodoAST operacion = new NodoAST(opName, nodoId, valor);
                return new NodoAST("ASIGNACION", nodoId, operacion);
            }
        }

        if (peek() != null && peek().lexema.equals(";")) {
            next();
        } else if (peek() != null) {
            agregarErrorSintactico("operador de asignacion o ';'", peek().lexema);
        }
        return nodoId;
    }

    private NodoAST escribir() {
        Token escribirTok = peek();
        if (escribirTok == null || !escribirTok.lexema.equals("escribir")) return null;
        next();

        if (peek() == null || !peek().lexema.equals("(")) {
            agregarErrorSintactico("'('", peek() != null ? peek().lexema : null);
            return new NodoAST("ESCRIBIR");
        }
        next();

        NodoAST args = new NodoAST("ARGUMENTOS");

        Token afterParen = peek();
        if (afterParen != null && afterParen.lexema.equals(")")) {
            agregarErrorSintactico("al menos un argumento", ")");
        } else {
            NodoAST primerArg = expresion();
            if (primerArg == null) {
                agregarErrorSintactico("expresion como argumento", peek() != null ? peek().lexema : null);
            }
            args.hijo = primerArg;

            NodoAST ultimoArg = args.hijo;
            while (
                peek() != null &&
                peek().tipo == TipoToken.SIMBOLO &&
                peek().lexema.equals(",")
            ) {
                next();
                NodoAST nuevoArg = expresion();
                if (nuevoArg == null) {
                    agregarErrorSintactico("expresion despues de ','", peek() != null ? peek().lexema : null);
                }
                if (ultimoArg != null) {
                    ultimoArg.sig = nuevoArg;
                }
                ultimoArg = nuevoArg;
            }
        }

        if (peek() == null || !peek().lexema.equals(")")) {
            agregarErrorSintactico("')'", peek() != null ? peek().lexema : null);
        } else {
            next();
        }

        if (peek() != null && peek().lexema.equals(";")) {
            next();
        } else if (peek() != null) {
            agregarErrorSintactico("';'", peek().lexema);
        } else {
            agregarErrorSintactico("';'", null);
        }

        return new NodoAST("ESCRIBIR", args, null);
    }

    private NodoAST si() {
        Token siTok = peek();
        if (siTok == null || !siTok.lexema.equals("si")) return null;
        next();

        if (peek() == null || !peek().lexema.equals("(")) {
            agregarErrorSintactico("'('", peek() != null ? peek().lexema : null);
            return new NodoAST("SI");
        }
        next();

        Token afterParen = peek();
        if (afterParen != null && afterParen.lexema.equals(")")) {
            agregarErrorSintactico("expresion de condicion", ")");
            next();
            NodoAST bloqueHijo = bloque();
            if (bloqueHijo == null) {
                agregarErrorSintactico("'{'", peek() != null ? peek().lexema : null);
            }
            if (peek() != null && peek().lexema.equals(";")) next();
            return new NodoAST("SI", new NodoAST("CONDICION_VACIA"), bloqueHijo);
        }

        NodoAST condicion = expresion();
        if (condicion == null) {
            agregarErrorSintactico("expresion de condicion", peek() != null ? peek().lexema : null);
        }

        if (peek() == null || !peek().lexema.equals(")")) {
            agregarErrorSintactico("')'", peek() != null ? peek().lexema : null);
        } else {
            next();
        }

        NodoAST bloqueHijo = bloque();
        if (bloqueHijo == null || bloqueHijo.hijo == null) {
            agregarErrorSintactico("bloque '{...}' con contenido", peek() != null ? peek().lexema : null);
        }

        if (peek() != null && peek().lexema.equals(";")) next();

        return new NodoAST("SI", condicion, bloqueHijo);
    }

    private NodoAST bloque() {
        if (peek() == null || !peek().lexema.equals("{")) {
            agregarErrorSintactico("'{'", peek() != null ? peek().lexema : null);
            return null;
        }
        next();

        NodoAST sentencias = new NodoAST("BLOQUE");
        NodoAST ultimo = null;

        while (peek() != null && !peek().lexema.equals("}")) {
            NodoAST sent = declaracion();
            if (sent != null) {
                if (sentencias.hijo == null) {
                    sentencias.hijo = sent;
                } else {
                    ultimo.sig = sent;
                }
                ultimo = sent;
            } else {
                Token t = peek();
                if (t != null && !t.lexema.equals("}")) {
                    agregarErrorSintactico("sentencia valida", t.lexema);
                    next();
                }
            }
        }

        if (peek() == null) {
            agregarErrorSintactico("'}'", null);
            return sentencias;
        }
        if (peek().lexema.equals("}")) {
            next();
        }

        return sentencias;
    }

    private NodoAST expresion() {
        NodoAST izquierda = termino();

        while (
            peek() != null &&
            (peek().tipo == TipoToken.OP_ARITMETICO || peek().tipo == TipoToken.SIMBOLO_COMPARISON)
        ) {
            Token op = next();
            NodoAST derecha = termino();
            if (derecha == null) {
                agregarErrorSintactico("expresion", peek() != null ? peek().lexema : null);
                derecha = new NodoAST("ERROR_EXPR");
            }
            NodoAST nuevo = new NodoAST("OP_" + op.lexema, izquierda, derecha);
            izquierda = nuevo;
        }

        return izquierda;
    }

    private NodoAST termino() {
        return factor();
    }

    private NodoAST factor() {
        Token t = peek();
        if (t == null) return null;

        switch (t.tipo) {
            case NUMERO_ENTERO:
            case NUMERO_DECIMAL:
                next();
                return new NodoAST("NUMERO", t.lexema);
            case IDENTIFICADOR:
                next();
                return new NodoAST("ID", t.lexema);
            case CADENA:
                next();
                return new NodoAST("CADENA", t.lexema);
            case SIMBOLO:
                if (t.lexema.equals("(")) {
                    next();
                    NodoAST exp = expresion();
                    if (peek() != null && peek().lexema.equals(")")) {
                        next();
                    } else {
                        agregarErrorSintactico("')'", peek() != null ? peek().lexema : null);
                    }
                    return exp;
                }
                if (t.lexema.equals("&")) {
                    next();
                    Token sig = peek();
                    if (sig != null && sig.tipo == TipoToken.IDENTIFICADOR) {
                        next();
                        return new NodoAST("REF", sig.lexema);
                    }
                    agregarErrorSintactico("identificador despues de '&'", sig != null ? sig.lexema : null);
                    return new NodoAST("REF");
                }
                if (t.lexema.equals(")")) {
                    return null;
                }
                agregarErrorSintactico("numero, identificador, cadena, '(' o '&'", t.lexema);
                next();
                return null;
            default:
                return null;
        }
    }
}
