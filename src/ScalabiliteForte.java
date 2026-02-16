import java.io.*;
import java.net.*;

public class ScalabiliteForte {
    public static void main(String[] args) throws Exception {
        int ntotGlobal = 16000000;
        int portBase = 25545;
        long t1 = 0;

        System.out.println("Nb Workers | Temps (ms) | Speedup");

        for (int n = 1; n <= 8; n++) {
            int pointsParWorker = ntotGlobal / n;
            Thread[] clients = new Thread[n];

            long start = System.currentTimeMillis();

            // On lance n requêtes en PARALLÈLE
            for (int i = 0; i < n; i++) {
                final int port = portBase + i;
                clients[i] = new Thread(() -> {
                    try (Socket s = new Socket("127.0.0.1", port);
                         PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                         BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {

                        out.println(pointsParWorker);
                        in.readLine(); // Attend que le worker finisse son calcul

                    } catch (Exception e) {
                        // Silencieux si erreur
                    }
                });
                clients[i].start();
            }

            // On attend que tout le monde ait fini
            for (Thread t : clients) t.join();

            long duration = System.currentTimeMillis() - start;
            if (n == 1) t1 = duration;
            double speedup = (double) t1 / duration;

            System.out.printf("%d          | %-10d | %.2fx\n", n, duration, speedup);
        }
    }
}