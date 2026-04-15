package sintactico;

import lexico.Token;

public class NodoAST {
    public String tipo;
    public String valor;
    public Token token;
    public NodoAST hijo;
    public NodoAST sig;

    public NodoAST(String tipo) {
        this.tipo = tipo;
    }

    public NodoAST(String tipo, String valor) {
        this.tipo = tipo;
        this.valor = valor;
    }

    public NodoAST(String tipo, NodoAST hijo, NodoAST sig) {
        this.tipo = tipo;
        this.hijo = hijo;
        this.sig = sig;
    }

    public void preOrder() {
        System.out.print("[" + tipo);
        if (valor != null) {
            System.out.print(":" + valor);
        }
        System.out.print("] ");
        if (hijo != null) hijo.preOrder();
        if (sig != null) sig.preOrder();
    }

    public void inOrder() {
        if (hijo != null) hijo.inOrder();
        System.out.print("[" + tipo);
        if (valor != null) {
            System.out.print(":" + valor);
        }
        System.out.print("] ");
        if (sig != null) sig.inOrder();
    }

    public void postOrder() {
        if (hijo != null) hijo.postOrder();
        if (sig != null) sig.postOrder();
        System.out.print("[" + tipo);
        if (valor != null) {
            System.out.print(":" + valor);
        }
        System.out.print("] ");
    }

    public void printTree(String prefix, boolean isLast) {
        System.out.println(prefix + (isLast ? "└── " : "├── ") + formatNodo());

        String childPrefix = prefix + (isLast ? "    " : "│   ");

        if (hijo != null) {
            boolean hasMoreSiblings = sig != null;
            hijo.printTree(childPrefix, !hasMoreSiblings);
        }

        if (sig != null) {
            sig.printTree(prefix, true);
        }
    }
    
    public void printTree() {
        printTree("", true);
    }

    public String formatNodo() {
        return tipo + (valor != null ? ":" + valor : "");
    }
}
