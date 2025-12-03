import java.util.*;

public class TreapDemo {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("Choose mode:");
        System.out.println("1) Manual input (read sets from keyboard)");
        System.out.println("2) Random test (generate large random sets)");
        System.out.print("Enter 1 or 2: ");

        int choice = 1;
        try {
            String line = sc.nextLine().trim();
            if (!line.isEmpty()) {
                choice = Integer.parseInt(line);
            }
        } catch (Exception e) {
            // default to 1
            choice = 1;
        }

        if (choice == 2) {
            runRandomTest();
        } else {
            runManualInput(sc);
        }

        sc.close();
    }

    // --------- MODE 1: MANUAL INPUT ----------------
    private static void runManualInput(Scanner sc) {
        Treap A = new Treap();
        Treap B = new Treap();

        // ------- INPUT SET A -------
        System.out.println("Enter elements for Set A (space separated):");
        String[] aVals = sc.nextLine().trim().split("\\s+");
        for (String s : aVals) {
            if (!s.isEmpty()) {
                A.insert(Integer.parseInt(s));
            }
        }

        // ------- INPUT SET B -------
        System.out.println("Enter elements for Set B (space separated):");
        String[] bVals = sc.nextLine().trim().split("\\s+");
        for (String s : bVals) {
            if (!s.isEmpty()) {
                B.insert(Integer.parseInt(s));
            }
        }

        // ------- DISPLAY INPUT ------
        System.out.print("\nTreap A (inorder): ");
        A.printInOrder();

        System.out.print("Treap B (inorder): ");
        B.printInOrder();

        // ------- OPERATIONS ----------
        Treap union = Treap.unionTreaps(A, B);
        Treap inter = Treap.intersectTreaps(A, B);
        Treap diff  = Treap.differenceTreaps(A, B);

        // ------- RESULTS --------------
        System.out.println("\n--- Results ---");
        System.out.print("Union (A ∪ B):         ");
        union.printInOrder();

        System.out.print("Intersection (A ∩ B):  ");
        inter.printInOrder();

        System.out.print("Difference (A \\ B):   ");
        diff.printInOrder();
    }

    // --------- MODE 2: RANDOM TEST -----------------
    private static void runRandomTest() {
        Random rand = new Random();

        Treap A = new Treap();
        Treap B = new Treap();

        int N = 5000;      // nodes per tree
        int RANGE = 5050;  // value range

        // Treap A 
        Set<Integer> usedA = new HashSet<>();
        while (A.size() < N) {
            int val = rand.nextInt(RANGE) + 1;
            if (usedA.add(val)) {
                A.insert(val);
            }
        }

        // Treap B 
        Set<Integer> usedB = new HashSet<>();
        while (B.size() < N) {
            int val = rand.nextInt(RANGE) + 1;
            if (usedB.add(val)) {
                B.insert(val);
            }
        }

        System.out.println("Treap A size: " + A.size());
        System.out.println("Treap B size: " + B.size());

        // Perform set operations
        Treap union = Treap.unionTreaps(A, B);
        Treap inter = Treap.intersectTreaps(A, B);
        Treap diff  = Treap.differenceTreaps(A, B);

        // Print results (preorder just to see structure)
        System.out.print("Union (A ∪ B): ");
        union.printPreOrder();

        System.out.print("\n\nIntersection (A ∩ B): ");
        inter.printPreOrder();

        System.out.print("\n\nDifference (A \\ B): ");
        diff.printPreOrder();

        System.out.println();
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

    /* ------------ Basic treap operations (insert, size, print) ------------ */

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

    public void printPreOrder() {
        printPreOrder(root);
        System.out.println();
    }

    private void printPreOrder(Node r) {
        if (r == null) return;
        System.out.print(r.key + " ");
        printPreOrder(r.left);
        printPreOrder(r.right);
    }

    public int size() {
        return size(root);
    }

    private int size(Node r) {
        if (r == null) return 0;
        return 1 + size(r.left) + size(r.right);
    }

    /* ------------------ Core building blocks: split, join ------------------ */

    // split(root, key):
    //   less    = all nodes with key < given key
    //   equal   = node with that key (or null if not present)
    //   greater = all nodes with key > given key
    static Treap.SplitResult split(Node root, int key) {
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
