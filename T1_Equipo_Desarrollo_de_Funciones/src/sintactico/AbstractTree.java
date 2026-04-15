package sintactico;

import lexico.Token;

class Nodo {

    Token token;
    Nodo izquierdo;
    Nodo derecho;
}

public class AbstractTree {

    public Nodo raiz;

    public AbstractTree(Nodo raiz) {
        this.raiz = raiz;
    }
}
