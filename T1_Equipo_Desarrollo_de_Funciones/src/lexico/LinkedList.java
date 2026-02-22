package lexico;

public class LinkedList {
    private Nodo head;
    public int count;
    private Nodo tail;

    public LinkedList(Nodo head) {
        if (head == null) {
            this.count = 0;
            return;
        }
        this.head = head;
        this.tail = head;
    }


    public Nodo obtenerHead() {
        return this.head;
    }

    public Nodo  obtenerTail() {
        return this.tail;
    }

    public void debug() {
        if (head == null) {
            System.out.println("Lista Vacia!!");
            return;
        }
        Nodo tmp = this.head;
        while (tmp != null) {
            System.out.println(tmp + " Nodo " + tmp.data.value + " {");
            System.out.println("\tprev: " + tmp.prev);
            System.out.println("\tnext: " + tmp.next);
            System.out.println("}");

            tmp = tmp.next;
        }

    }

    public void printList() {
        if (head == null) {
            System.out.println("Lista Vacia!!");
            return;
        }

        Nodo tmp = this.head;
        System.out.print("Null <-- ");
        while (tmp.next != null) {
            System.out.print(tmp.data.value + " <===> ");
            tmp = tmp.next;
        }
        System.out.print(tmp.data.value);
        System.out.println(" --> Null");
    }

    public void agregarNodoInicio(Token t) {
        Nodo nuevoNodo = new Nodo(t, null, this.head);

        if (this.head != null) {
            this.head.prev = nuevoNodo;
        }
        this.head = nuevoNodo;
        this.count++;
        this.updateTail();
    }

    public void agregarNodoFinal(Token t) {
        Nodo nuevoNodo = new Nodo(t, null, null);

        if  (this.head == null) {
            this.head = nuevoNodo;
            this.tail = nuevoNodo;
        } else {
            Nodo tmp = this.head;
            while (tmp.next != null) {
                tmp = tmp.next;
            }
            nuevoNodo.prev = tmp;
            tmp.next = nuevoNodo;
            this.updateTail();
        }

        this.count++;
    }

    private void updateTail() {
        Nodo tmp = this.head;

        while (tmp.next != null) {
            tmp = tmp.next;
        }

        this.tail = tmp;
    }

    public void agregarNodoPos(Token t, int pos) {

        if (pos <= 0) {
            System.out.println("Posicion invalida " + pos);
            return;
        }

        Nodo nuevoNodo = new  Nodo(t, null, null);

        if (this.head == null) {
            System.out.println("Agregando al inicio");
            this.head = nuevoNodo;
            this.tail = nuevoNodo;
            this.count++;
        } else {
            int index = 1;
            Nodo tmp1 = this.head;
            Nodo tmp2 = null;
            while (tmp1 != null) {
                System.out.println(index + " " + tmp1.data.value + " " + tmp1.prev);
                if (pos - 1 == index) {
                    System.out.println("insertando en " + tmp1.data.value);
                    tmp2 = tmp1.next;
                    tmp1.next = nuevoNodo;
                    nuevoNodo.prev = tmp1;
                    nuevoNodo.next = tmp2;
                    tmp2.prev = nuevoNodo;
                    updateTail();
                    this.count++;
                    return;
                }
                tmp1 = tmp1.next;
                index++;
            }

            System.out.println("No es una posicion alcanzable");
        }
    }

    public void borrarNodoInicial() {
        if (this.head == null) {
            System.out.println("Lista Vacia!!");
            return;
        }

        Nodo tmp = this.head;

        if (tmp.prev == tmp.next) {
            this.head = null;
            this.count--;
            return;
        }

        this.head = tmp.next;
        this.head.prev = null;
        this.count--;
    }

    public void borrarNodoFinal() {
        if (this.head == null) {
            System.out.println("Lista Vacia!!");
            return;
        }

        Nodo tmp = this.head;

        if (tmp.prev == tmp.next) {
            this.head = null;
            this.tail = null;
            this.count--;
            return;
        }

        while (tmp.next.next != null) {
            tmp = tmp.next;
        }

        this.tail = tmp;
        tmp.next = null;
        this.count--;
    }
}

