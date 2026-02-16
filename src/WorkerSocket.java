import java.io.*;
import java.net.*;
import java.util.concurrent.ThreadLocalRandom; // Import nécessaire

public class WorkerSocket {
    public static void main(String[] args) throws Exception {
        int port = 25545; // Port par défaut
        if (args.length > 0) port = Integer.parseInt(args[0]);

        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Worker démarré sur le port " + port);

            while (true) {
                try (Socket soc = server.accept();
                     BufferedReader bRead = new BufferedReader(new InputStreamReader(soc.getInputStream()));
                     PrintWriter pWrite = new PrintWriter(new BufferedWriter(new OutputStreamWriter(soc.getOutputStream())), true)) {

                    String msg;
                    while ((msg = bRead.readLine()) != null) {
                        if (msg.equals("END")) break;

                        int totalCount = Integer.parseInt(msg);
                        int inside = 0;

                        // On récupère le générateur propre à ce thread/worker
                        ThreadLocalRandom random = ThreadLocalRandom.current();

                        // Calcul Monte Carlo optimisé pour le multi-threading
                        for (int i = 0; i < totalCount; i++) {
                            double x = random.nextDouble();
                            double y = random.nextDouble();
                            if (x * x + y * y <= 1.0) inside++;
                        }
                        pWrite.println(inside);
                    }
                } catch (Exception e) {
                    System.err.println("Erreur connexion sur port " + port);
                }
            }
        }
    }
}