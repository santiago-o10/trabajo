import java.util.concurrent.TimeUnit;

// Clase principal del juego
public class Juego {

    // Clase que representa al jugador
    public static class Player {
        private int vida; // Puntos de vida del jugador

        public Player (int vida){
            this.vida=vida;
        }

        // Método para recibir daño
        public synchronized void takeDamage(int daño, String attacker ){
            System.out.println (attacker + " le tira un dardo y hace " + daño + " de daño ");
            vida -= daño;
            System.out.println(" --- Vida de los jugadores :"+ vida);
        }

        // Verifica si el jugador sigue vivo
        public synchronized boolean estasVivo () {
            return vida > 0;
        }

        // Devuelve la vida actual del jugador
        public synchronized int getvida(){
            return vida ;
        }
    }

    // Clase que representa al enemigo
    public static class Enemy {
        private int hp; // Puntos de vida del enemigo

        public Enemy (int hp){
            this.hp = hp;
        }

        // Método para recibir daño
        public synchronized void takeDamage(int daño, String attacker ){
            System.out.println (attacker + " le tira machetazo y hace " + daño + " de daño ");
            hp -= daño;
            System.out.println(" --- Vida del enemigo :"+ hp );
        }

        // Verifica si el enemigo sigue vivo
        public synchronized boolean estasVivo () {
            return hp > 0;
        }

        // Devuelve la vida actual del enemigo
        public synchronized int gethp(){
            return hp ;
        }
    }

    // Clase que representa al jugador atacando al enemigo
    public static class jugador extends Thread{
        private final Player player;
        private final Enemy enemy;
        private final String nombre;
        private final int ataquePoder;
        private final int cantidadAtaques;

        public jugador (Player player,Enemy enemy, String nombre, int ataquePoder, int cantidadAtaques) {
            this.player = player;
            this.enemy = enemy;
            this.nombre = nombre;
            this.ataquePoder = ataquePoder;
            this.cantidadAtaques = cantidadAtaques;
        }

        // Método que ejecuta los ataques del jugador al enemigo
        @Override
        public void run() {
            for (int i = 0; i < cantidadAtaques; i++) {
                if (!player.estasVivo()) {
                    System.out.println(nombre + " ya está muerto y no puede seguir atacando.");
                    break;
                }
                if (!enemy.estasVivo()) {
                    System.out.println(nombre + " ve que el enemigo ya está muerto y deja de atacar.");
                    break;
                }

                enemy.takeDamage(ataquePoder, nombre);

                try {
                    // Espera medio segundo entre ataques
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println(nombre + " fue interrumpido.");
                }
            }
            System.out.println(nombre + " ya terminó sus ataques.");
        }
    }

    // Clase que representa a los enemigos atacando al jugador
    public static class ninja extends Thread {   
        private final Player player;
        private final String name;
        private final int ataquePoder;
        private final int cantidadAtaques;
            
        public ninja (Player player, String name, int ataquePoder, int cantidadAtaques) {
            this.player = player;
            this.name = name;
            this.ataquePoder = ataquePoder;
            this.cantidadAtaques = cantidadAtaques;
        }

        // Método que ejecuta los ataques del ninja al jugador
        @Override
        public void run() {
            for (int i = 0; i < cantidadAtaques; i++) {
                if (!player.estasVivo()) {
                    System.out.println(name + " ve que el jugador ya está muerto y deja de atacar.");
                    break;
                }

                player.takeDamage(ataquePoder, name);

                try {
                    // Espera medio segundo entre ataques
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println(name + " fue interrumpido.");
                }
            }
            System.out.println(name + " el enemigo terminó sus ataques.");
        }
    }

    // Método principal que inicia el juego
    public static void main(String[] args) {
        System.out.println("---combat---");
   
        // Se crea el jugador y el enemigo con 100 puntos de vida cada uno
        Player player = new Player(100);
        Enemy enemy = new Enemy(100);

        // Se crean dos ninjas enemigos y un jugador atacante
        ninja n1 = new ninja(player, "ninja", 20, 5);
        ninja n2 = new ninja(player, "ninjaDark", 15, 5);
        ninja n3 = new ninja(player, "orco", 40, 2);
        jugador j1 = new jugador(player,enemy, "Rivaldo", 25, 4);
        jugador j2 = new jugador(player,enemy, "Santiago", 30, 3);
        jugador j3 = new jugador(player,enemy, "James", 20, 5);


        // Se inician los hilos de combate
        n1.start();
        n2.start();
        n3.start();
        j1.start();
        j2.start();
        j3.start();

        try {
            // Espera a que todos los hilos terminen
            n1.join();
            n2.join();
            n3.start();
            j1.join();
            j2.start();
            j3.start();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Muestra el resultado final del combate
        if (player.estasVivo()) {
            System.out.println("El jugador sobrevivió con " + player.getvida() + " puntos de vida.");
        } else {
            System.out.println("El jugador ha sido derrotado en la arena.");
        }
        if (enemy.estasVivo()){
            System.out.println("El enemigo sobrevivió con " + enemy.gethp() + " puntos de vida.");
        }

        System.out.println("Fin del juego");
    }
}