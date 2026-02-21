package lexico;

public class LinkedList {
    Nodo head;
    int count;
    Nodo tail;

    public LinkedList(Nodo head) {
        if (head == null) {
            this.count = 0;
            return;
        }
        this.head = head;
        this.tail = head;
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
        /*
        We can use the following steps to insert a new node at beginning of the double linked list...
            Step 1 - Create a newNode with given value and newNode → previous as NULL.
            Step 2 - Check whether list is Empty (head == NULL)
            Step 3 - If it is Empty then, assign NULL to newNode → next and newNode to head.
            Step 4 - If it is not Empty then, assign head to newNode → next and newNode to head.
        */
        Nodo nuevoNodo = new Nodo(t, null, this.head);

        if (this.head == null) {
            this.head = nuevoNodo;
//            nuevoNodo.next = this.head;
        } else {
            this.head.prev = nuevoNodo;
            this.head = nuevoNodo;
        }

    }
}

