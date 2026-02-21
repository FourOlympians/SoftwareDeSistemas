package lexico;

public class LinkedList {
    Nodo head;
    int count;
    Nodo tail;

    public LinkedList(Nodo head) {
        if (head == null) {
            this.count = 0;
        }
        this.head = head;
        this.tail = head;
    }
}

