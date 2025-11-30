import java.util.Random;

public class TreapDemo {

    public static void main(String[] args) {
        // ----- 1. Build two example sets A and B -----
        Treap A = new Treap();
        Treap B = new Treap();

        // A = {1, 3, 5, 7}
        A.insert(1);
        A.insert(3);
        A.insert(5);
        A.insert(7);

        // B = {3, 4, 5, 8}
        B.insert(3);
        B.insert(4);
        B.insert(5);
        B.insert(8);

        System.out.println("Treap A (inorder):");
        A.printInOrder(); // should be 1 3 5 7
        System.out.println("Treap B (inorder):");
        B.printInOrder(); // should be 3 4 5 8

        // ----- 2. Compute UNION, INTERSECTION, DIFFERENCE -----
        Treap union = Treap.unionTreaps(A, B);        // A ∪ B
        Treap inter = Treap.intersectTreaps(A, B);    // A ∩ B
        Treap diff  = Treap.differenceTreaps(A, B);   // A \ B

        // ----- 3. Show results -----
        System.out.println("\n--- Set Operations ---");
        System.out.print("Union (A ∪ B):         ");
        union.printInOrder();        // expected: 1 3 4 5 7 8

        System.out.print("Intersection (A ∩ B): ");
        inter.printInOrder();        // expected: 3 5

        System.out.print("Difference (A \\ B):   ");
        diff.printInOrder();         // expected: 1 7
    }
}

/* ======================== Treap Implementation ======================== */

class Treap {

    static class Node {
        int key;
        int priority;   // random
        Node left, right;

        Node(int key, int priority) {
            this.key = key;
            this.priority = priority;
        }
    }

    static class SplitResult {
        Node less;
        Node equal;
        Node greater;
    }

    private static final Random RAND = new Random();
    Node root;

    /* ------------ Basic treap operations (insert, print) ------------ */

    public void insert(int key) {
        root = insert(root, key);
    }

    private Node insert(Node r, int key) {
        if (r == null) {
            return new Node(key, RAND.nextInt());
        }
        if (key == r.key) {
            // no duplicates, treat like a set
            return r;
        }
        if (key < r.key) {
            r.left = insert(r.left, key);
            if (r.left.priority < r.priority) {
                r = rotateRight(r);
            }
        } else {
            r.right = insert(r.right, key);
            if (r.right.priority < r.priority) {
                r = rotateLeft(r);
            }
        }
        return r;
    }

    private Node rotateRight(Node y) {
        Node x = y.left;
        Node t2 = x.right;
        x.right = y;
        y.left = t2;
        return x;
    }

    private Node rotateLeft(Node x) {
        Node y = x.right;
        Node t2 = y.left;
        y.left = x;
        x.right = t2;
        return y;
    }

    public void printInOrder() {
        printInOrder(root);
        System.out.println();
    }

    private void printInOrder(Node r) {
        if (r == null) return;
        printInOrder(r.left);
        System.out.print(r.key + " ");
        printInOrder(r.right);
    }

    /* ------------------ Core building blocks: split, join ------------------ */

    // split(root, key):
    //   less    = all nodes with key < given key
    //   equal   = node with that key (or null if not present)
    //   greater = all nodes with key > given key
    static SplitResult split(Node root, int key) {
        SplitResult res = new SplitResult();
        if (root == null) {
            return res;
        }

        if (root.key < key) {
            // root belongs to "less"
            SplitResult rightRes = split(root.right, key);
            root.right = rightRes.less; // reconnect
            res.less = root;
            res.equal = rightRes.equal;
            res.greater = rightRes.greater;
        } else if (root.key > key) {
            // root belongs to "greater"
            SplitResult leftRes = split(root.left, key);
            root.left = leftRes.greater; // reconnect
            res.less = leftRes.less;
            res.equal = leftRes.equal;
            res.greater = root;
        } else {
            // root.key == key
            res.less = root.left;
            res.equal = root;
            res.greater = root.right;
            root.left = root.right = null; // detach children
        }

        return res;
    }

    // join(a, b): all keys in a < all keys in b
    static Node join(Node a, Node b) {
        if (a == null) return b;
        if (b == null) return a;

        if (a.priority < b.priority) {
            a.right = join(a.right, b);
            return a;
        } else {
            b.left = join(a, b.left);
            return b;
        }
    }

    /* ---------------------- Set UNION / INTERSECTION / DIFFERENCE ---------------------- */

    // union of two treaps (by nodes)
    static Node union(Node r1, Node r2) {
        if (r1 == null) return r2;
        if (r2 == null) return r1;

        // Ensure r1 has higher priority (min-heap)
        if (r1.priority > r2.priority) {
            Node tmp = r1;
            r1 = r2;
            r2 = tmp;
        }

        SplitResult s = split(r2, r1.key);
        r1.left = union(r1.left, s.less);
        r1.right = union(r1.right, s.greater);
        // s.equal is ignored because r1 already has this key
        return r1;
    }

    // intersection of two treaps (by nodes)
    static Node intersect(Node r1, Node r2) {
        if (r1 == null || r2 == null) return null;

        if (r1.priority > r2.priority) {
            Node tmp = r1;
            r1 = r2;
            r2 = tmp;
        }

        SplitResult s = split(r2, r1.key);
        Node left = intersect(r1.left, s.less);
        Node right = intersect(r1.right, s.greater);

        if (s.equal == null) {
            // key only in one tree → drop this node
            return join(left, right);
        } else {
            // key in both → keep it
            r1.left = left;
            r1.right = right;
            return r1;
        }
    }

    // difference: r1 \ r2
    static Node difference(Node r1, Node r2) {
        return diff(r1, r2, true);
    }

    // diff(r1, r2, r2IsSubtr): from the paper
    private static Node diff(Node r1, Node r2, boolean r2IsSubtr) {
        if (r1 == null || r2 == null) {
            return r2IsSubtr ? r1 : r2;
        }

        if (r1.priority > r2.priority) {
            Node tmp = r1;
            r1 = r2;
            r2 = tmp;
            r2IsSubtr = !r2IsSubtr;
        }

        SplitResult s = split(r2, r1.key);
        Node left = diff(r1.left, s.less, r2IsSubtr);
        Node right = diff(r1.right, s.greater, r2IsSubtr);

        if (s.equal == null && r2IsSubtr) {
            // we're subtracting r2 from r1 AND r2 did not contain this key
            // → keep r1
            r1.left = left;
            r1.right = right;
            return r1;
        } else {
            // otherwise drop this key
            return join(left, right);
        }
    }

    /* ---------------------- Convenience wrappers for Treap objects ---------------------- */

    // clone treap nodes so we don't destroy the originals when doing set ops
    private static Node clone(Node r) {
        if (r == null) return null;
        Node n = new Node(r.key, r.priority);
        n.left = clone(r.left);
        n.right = clone(r.right);
        return n;
    }

    public static Treap unionTreaps(Treap a, Treap b) {
        Treap result = new Treap();
        result.root = union(clone(a.root), clone(b.root));
        return result;
    }

    public static Treap intersectTreaps(Treap a, Treap b) {
        Treap result = new Treap();
        result.root = intersect(clone(a.root), clone(b.root));
        return result;
    }

    public static Treap differenceTreaps(Treap a, Treap b) {
        Treap result = new Treap();
        result.root = difference(clone(a.root), clone(b.root));
        return result;
    }
}
