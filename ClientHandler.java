import java.io.*;
import java.net.*;

// Maneja la comunicación con un cliente (una conexión)
public class ClientHandler extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ClientHandler opponent; // referencia al oponente cuando esté emparejado
    private String playerName;
    private GamePlayer player;

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    public void setOpponent(ClientHandler opp) {
        this.opponent = opp;
    }

    // Enviar mensaje al cliente
    public void sendMessage(String msg) {
        out.println(msg);
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("Recibido: " + line);

                if (line.startsWith("NAME:")) {
                    playerName = line.substring(5);
                    sendMessage("WELCOME " + playerName);
                } else if (line.startsWith("CHARACTER:")) {
                    String character = line.substring(10).trim();
                    player = new GamePlayer(playerName, character);
                    sendMessage("CHARACTER_SELECTED " + character + " (HP:" + player.getMaxHp() + ", DAÑO:" + player.getDamage() + ")");
                } else if (line.equals("ATTACK") && opponent != null && player != null && opponent.player != null) {
                    // Atacar al oponente: sincronizamos para evitar condiciones de carrera
                    synchronized (opponent) {
                        opponent.player.takeDamage(player.getDamage());
                        opponent.sendMessage("DAMAGE:" + player.getDamage() + " de " + player.getName() + " (" + player.getCharacter() + ")");
                        // Mostrar en el servidor quién atacó y la vida de ambos
                        System.out.println("[INFO] " + player.getName() + " (" + player.getCharacter() + ") atacó a " +
                            opponent.player.getName() + " (" + opponent.player.getCharacter() + "). " +
                            "HP atacante: " + player.getHp() + " | HP oponente: " + opponent.player.getHp());
                        // Si el oponente muere, notificar a ambos
                        if (!opponent.player.isAlive()) {
                            sendMessage("YOU_WIN");
                            opponent.sendMessage("YOU_LOSE");
                        }
                        // Efecto especial para el mago: quemadura
                        if (player.getCharacter().equalsIgnoreCase("mago")) {
                            new Thread(() -> {
                                try {
                                    Thread.sleep(7000); // 7 segundos
                                    if (opponent.player.isAlive()) {
                                        opponent.player.takeDamage(4);
                                        opponent.sendMessage("el enemigo se resintio por la quemadura (-4 HP)");
                                        if (!opponent.player.isAlive()) {
                                            sendMessage("YOU_WIN");
                                            opponent.sendMessage("YOU_LOSE");
                                        }
                                    }
                                } catch (InterruptedException ignored) {}
                            }).start();
                        }
                        // Efecto especial para el vampiro: curarse al atacar
                        if (player.getCharacter().equalsIgnoreCase("vampiro")) {
                            int before = player.getHp();
                            player.heal(5);
                            int healed = player.getHp() - before;
                            if (healed > 0) {
                                sendMessage("VAMPIRE_HEAL +" + healed + " (HP:" + player.getHp() + ")");
                            }
                        }
                    }

                } else if (line.equals("HEAL") && player != null) {
                    // El vampiro no puede usar HEAL
                    if (player.getCharacter().equalsIgnoreCase("vampiro")) {
                        sendMessage("HEAL_PROHIBIDO: El vampiro no puede usar HEAL.");
                    } else {
                        // Curarse a uno mismo
                        int before = player.getHp();
                        player.heal(10);
                        int healed = player.getHp() - before;
                        if (healed > 0) {
                            sendMessage("YOU_HEALED " + healed + " (HP:" + player.getHp() + ")");
                            if (opponent != null && opponent.player != null) {
                                opponent.sendMessage(player.getName() + " HEALED " + healed + " (HP:" + player.getHp() + ")");
                            }
                        } else {
                            sendMessage("HEAL_FAILED (HP al máximo)");
                        }
                    }
                } else if (line.equals("STATUS") && player != null) {
                    sendMessage("HP:" + player.getHp() + "/" + player.getMaxHp() + " | DAÑO:" + player.getDamage() + " | PERSONAJE: " + player.getCharacter());
                } else {
                    sendMessage("UNKNOWN_CMD");
                }
            }
        } catch (IOException e) {
            System.out.println("Error en handler: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }
}
