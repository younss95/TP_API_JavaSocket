import java.io.*;
import java.net.*;

public class Scalabilitefaible {
    public static void main(String[] args) throws Exception {
        int ntot_par_worker = 16000000; // Charge FIXE par worker
        int portBase = 25545;

        System.out.println("Nb Workers | Points Totaux | Temps (ms) | Efficacité");

        long t1 = 0;

        for (int n = 1; n <= 8; n++) {
            Thread[] threads = new Thread[n];
            long start = System.currentTimeMillis();

            // Chaque worker reçoit la même charge
            for (int i = 0; i < n; i++) {
                int port = portBase + i;
                threads[i] = new Thread(() -> {
                    try (Socket s = new Socket("127.0.0.1", port);
                         PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                         BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {
                        out.println(ntot_par_worker);
                        in.readLine();
                    } catch (Exception e) {}
                });
                threads[i].start();
            }

            for (Thread t : threads) t.join();

            long duration = System.currentTimeMillis() - start;
            if (n == 1) t1 = duration;

            // Efficacité = T1 / Tn (Idéalement proche de 100%)
            double efficacite = (double) t1 / duration * 100;

            System.out.printf("%d          | %-13d | %-10d | %.2f%%\n",
                    n, (long)ntot_par_worker * n, duration, efficacite);
        }
    }
}