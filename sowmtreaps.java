import java.util.*;

public class Main {

    public static void main(String[] args) {
        Random rand = new Random();

        Treap A = new Treap();
        Treap B = new Treap();

        int N = 5000;       // nodes per tree
        int RANGE = 5050;  //  range

        // Treap A 
        Set<Integer> usedA = new HashSet<>();
        while (A.size() < N) {
            int val = rand.nextInt(RANGE) + 1;
            if (usedA.add(val)) A.insert(val);
        }

        //  Treap B 
        Set<Integer> usedB = new HashSet<>();
        while (B.size() < N) {
            int val = rand.nextInt(RANGE) + 1;
            if (usedB.add(val)) B.insert(val);
        }

        System.out.println("Treap A size: " + A.size());
        System.out.println("Treap B size: " + B.size());

        // Perform set operations
        Treap union = Treap.unionTreaps(A, B);
        Treap inter = Treap.intersectTreaps(A, B);
        Treap diff  = Treap.differenceTreaps(A, B);

        // Print results 
        System.out.print("Union (A ∪ B):   ");
        union.printPreOrder();

        System.out.print("\n\nIntersection (A ∩ B):  ");
        inter.printPreOrder();

        System.out.print("\n\nDifference (A \\ B):   ");
        diff.printPreOrder();
    }
}

/*  Treap Implementation  */
class Treap {

    static class Node {
        int key;
        int priority;
        Node left, right;

        Node(int key, int priority) {
            this.key = key;
            this.priority = priority;
        }
    }

    private static final Random RAND = new Random();
    Node root;

    // Insert node 
    public void insert(int key) {
        root = insert(root, key);
    }

    private Node insert(Node r, int key) {
        if (r == null) return new Node(key, RAND.nextInt(1000));
        if (key == r.key) return r; // discard duplicates
        if (key < r.key) {
            r.left = insert(r.left, key);
            if (r.left.priority > r.priority) r = rotateRight(r);
        } else {
            r.right = insert(r.right, key);
            if (r.right.priority > r.priority) r = rotateLeft(r);
        }
        return r;
    }

    private Node rotateRight(Node y) {
        Node x = y.left;
        y.left = x.right;
        x.right = y;
        return x;
    }

    private Node rotateLeft(Node x) {
        Node y = x.right;
        x.right = y.left;
        y.left = x;
        return y;
    }

    public int size() { return size(root); }
    private int size(Node r) {
        if (r == null) return 0;
        return 1 + size(r.left) + size(r.right);
    }

    // Pre-order traversal printing (root first)
    public void printPreOrder() {
        List<String> list = new ArrayList<>();
        preOrder(root, list);
        for (int i = 0; i < list.size(); i++) {
            System.out.print(list.get(i));
            if (i != list.size() - 1) System.out.print(" ");
        }
        System.out.println();
    }

    private void preOrder(Node r, List<String> list) {
        if (r == null) return;
        list.add("(" + r.key + "," + r.priority + ")");
        preOrder(r.left, list);
        preOrder(r.right, list);
    }

    /* Split & Join */
    private static class SplitResult { Node less, gtr, duplicate; }

    private static SplitResult split(Node r, int key) {
        SplitResult res = new SplitResult();
        if (r == null) return res;

        Node root = new Node(r.key, r.priority);
        root.left = r.left;
        root.right = r.right;

        if (r.key < key) {
            SplitResult rightRes = split(root.right, key);
            root.right = rightRes.less;
            res.less = root;
            res.gtr = rightRes.gtr;
            res.duplicate = rightRes.duplicate;
        } else if (r.key > key) {
            SplitResult leftRes = split(root.left, key);
            root.left = leftRes.gtr;
            res.less = leftRes.less;
            res.gtr = root;
            res.duplicate = leftRes.duplicate;
        } else {
            res.less = root.left;
            res.gtr = root.right;
            res.duplicate = root;
            root.left = root.right = null;
        }
        return res;
    }

    private static Node join(Node r1, Node r2) {
        if (r1 == null) return r2;
        if (r2 == null) return r1;

        Node root;
        if (r1.priority > r2.priority) {
            root = new Node(r1.key, r1.priority);
            root.left = r1.left;
            root.right = join(r1.right, r2);
        } else {
            root = new Node(r2.key, r2.priority);
            root.left = join(r1, r2.left);
            root.right = r2.right;
        }
        return root;
    }

    /*  Set Operations */
    private static Node union(Node r1, Node r2) {
        if (r1 == null) return r2;
        if (r2 == null) return r1;

        if (r1.priority < r2.priority) { Node tmp = r1; r1 = r2; r2 = tmp; }

        SplitResult s = split(r2, r1.key);
        Node root = (s.duplicate != null && s.duplicate.priority > r1.priority)
                ? new Node(s.duplicate.key, s.duplicate.priority)
                : new Node(r1.key, r1.priority);
        root.left = union(r1.left, s.less);
        root.right = union(r1.right, s.gtr);
        return root;
    }

    private static Node intersect(Node r1, Node r2) {
        if (r1 == null || r2 == null) return null;
        if (r1.priority < r2.priority) { Node tmp = r1; r1 = r2; r2 = tmp; }

        SplitResult s = split(r2, r1.key);
        Node left = intersect(r1.left, s.less);
        Node right = intersect(r1.right, s.gtr);

        if (s.duplicate == null) return join(left, right);

        Node root = (s.duplicate.priority > r1.priority)
                ? new Node(s.duplicate.key, s.duplicate.priority)
                : new Node(r1.key, r1.priority);
        root.left = left;
        root.right = right;
        return root;
    }

    private static Node diff(Node r1, Node r2, boolean r2IsSubtr) {
        if (r1 == null || r2 == null) return r2IsSubtr ? r1 : r2;

        if (r1.priority < r2.priority) {
            Node tmp = r1; r1 = r2; r2 = tmp;
            r2IsSubtr = !r2IsSubtr;
        }

        SplitResult s = split(r2, r1.key);
        Node left = diff(r1.left, s.less, r2IsSubtr);
        Node right = diff(r1.right, s.gtr, r2IsSubtr);

        if ((s.duplicate == null) && r2IsSubtr) {
            Node root = new Node(r1.key, r1.priority);
            root.left = left;
            root.right = right;
            return root;
        } else {
            return join(left, right);
        }
    }

    /*  Wrappers  */
    private static Node clone(Node r) {
        if (r == null) return null;
        Node n = new Node(r.key, r.priority);
        n.left = clone(r.left);
        n.right = clone(r.right);
        return n;
    }

    public static Treap unionTreaps(Treap a, Treap b) {
        Treap t = new Treap();
        t.root = union(clone(a.root), clone(b.root));
        return t;
    }

    public static Treap intersectTreaps(Treap a, Treap b) {
        Treap t = new Treap();
        t.root = intersect(clone(a.root), clone(b.root));
        return t;
    }

    public static Treap differenceTreaps(Treap a, Treap b) {
        Treap t = new Treap();
        t.root = diff(clone(a.root), clone(b.root), true);
        return t;
    }
}
