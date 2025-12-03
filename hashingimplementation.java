import java.util.HashSet;
import java.util.Random;

class Main {

    public static HashSet<Integer> unionHash(HashSet<Integer> A, HashSet<Integer> B) {
        HashSet<Integer> result = new HashSet<>(A);
        result.addAll(B);
        return result;
    }

    public static HashSet<Integer> intersectHash(HashSet<Integer> A, HashSet<Integer> B) {
        HashSet<Integer> result = new HashSet<>();
        HashSet<Integer> small = A.size() < B.size() ? A : B;
        HashSet<Integer> large = A.size() < B.size() ? B : A;

        for (int x : small) {
            if (large.contains(x)) {
                result.add(x);
            }
        }
        return result;
    }

    public static HashSet<Integer> differenceHash(HashSet<Integer> A, HashSet<Integer> B) {
        HashSet<Integer> result = new HashSet<>();
        for (int x : A) {
            if (!B.contains(x)) {
                result.add(x);
            }
        }
        return result;
    }

    public static void main(String[] args) {
        Random rand = new Random();
        int N = 50;
        int RANGE = 5050;

        HashSet<Integer> A = new HashSet<>();
        HashSet<Integer> B = new HashSet<>();

        while (A.size() < N) A.add(rand.nextInt(RANGE) + 1);
        while (B.size() < N) B.add(rand.nextInt(RANGE) + 1);

        HashSet<Integer> union = unionHash(A, B);
        HashSet<Integer> inter = intersectHash(A, B);
        HashSet<Integer> diff  = differenceHash(A, B);

        System.out.println("A: " + A);
        System.out.println("B: " + B);
        System.out.println("Union: " + union);
        System.out.println("Intersection: " + inter);
        System.out.println("Difference: " + diff);
    }
}
