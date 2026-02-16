import java.io.*;
import java.net.*;

public class MasterSocket {
    static final int[] tab_port = {25545, 25546, 25547, 25548, 25549, 25550, 25551, 25552};
    static final String ip = "127.0.0.1";

    public static void main(String[] args) throws Exception {
        int ntotGlobal = 16000000; // Charge totale fixe
        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("Combien de workers (1-8) : ");
        int numWorkers = Integer.parseInt(bufferRead.readLine());

        // 1. LANCEMENT AUTOMATIQUE DES WORKERS
        for (int i = 0; i < numWorkers; i++) {
            final int p = tab_port[i];
            new Thread(() -> {
                try {
                    WorkerSocket.main(new String[]{String.valueOf(p)});
                } catch (Exception e) {}
            }).start();
        }
        Thread.sleep(1000); // Pause pour laisser les serveurs s'ouvrir

        // 2. CONNEXION
        Socket[] sockets = new Socket[numWorkers];
        PrintWriter[] writers = new PrintWriter[numWorkers];
        BufferedReader[] readers = new BufferedReader[numWorkers];

        for (int i = 0; i < numWorkers; i++) {
            sockets[i] = new Socket(ip, tab_port[i]);
            writers[i] = new PrintWriter(sockets[i].getOutputStream(), true);
            readers[i] = new BufferedReader(new InputStreamReader(sockets[i].getInputStream()));
        }

        // SCALABILITÉ FORTE : On divise le travail
        int pointsParWorker = ntotGlobal / numWorkers;
        long startTime = System.currentTimeMillis();

        // Envoi de la charge
        for (int i = 0; i < numWorkers; i++) {
            writers[i].println(pointsParWorker);
        }

        // Réception des résultats
        int totalInside = 0;
        for (int i = 0; i < numWorkers; i++) {
            totalInside += Integer.parseInt(readers[i].readLine());
        }

        long stopTime = System.currentTimeMillis();

        // CALCULS FINAUX
        double pi = 4.0 * totalInside / ntotGlobal;
        double error = Math.abs(pi - Math.PI);

        // AFFICHAGE FORMATÉ
        System.out.println("\nPi : " + pi);
        System.out.println("Error : " + error);
        System.out.println("Ntot : " + ntotGlobal);
        System.out.println("Available processors : " + Runtime.getRuntime().availableProcessors());
        System.out.println("Time Duration (ms) : " + (stopTime - startTime));


        // Nettoyage
        for (int i = 0; i < numWorkers; i++) {
            writers[i].println("END");
            sockets[i].close();
        }
        System.exit(0);
    }
}