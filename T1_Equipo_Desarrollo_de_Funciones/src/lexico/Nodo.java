package lexico;

public class Nodo {
    Nodo prev;
    public Token data;
    Nodo next;

    public Nodo(Token t, Nodo prev, Nodo next) {
        this.data = t;
        this.prev = prev;
        this.next = next;
    }
}



