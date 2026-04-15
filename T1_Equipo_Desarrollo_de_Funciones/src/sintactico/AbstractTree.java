package sintactico;

public class AbstractTree {
    public NodoAST raiz;

    public AbstractTree(NodoAST raiz) {
        this.raiz = raiz;
    }

    public void preOrder() {
        if (raiz != null) {
            raiz.preOrder();
            System.out.println();
        }
    }

    public void inOrder() {
        if (raiz != null) {
            raiz.inOrder();
            System.out.println();
        }
    }

    public void postOrder() {
        if (raiz != null) {
            raiz.postOrder();
            System.out.println();
        }
    }

    public void printTree() {
        if (raiz != null) {
            raiz.printTree();
        }
    }
}
