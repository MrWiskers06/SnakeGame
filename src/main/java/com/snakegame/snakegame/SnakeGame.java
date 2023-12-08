package com.snakegame.snakegame;

import com.snakegame.snakegame.model.Direction;
import com.snakegame.snakegame.model.SnakeBody;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.*;

public class SnakeGame extends Application {
    // Variables de configuración del juego
    static Scene scene; // Escena del juego
    static GraphicsContext graphicsContext; // Contexto gráfico del área de juego
    static int speed = 5; // Velocidad inicial del juego
    static int foodcolor = 0; // Color inicial de la comida
    static int width = 30; // Ancho del área de juego
    static int height = 30; // Altura del área de juego
    static int foodX = 0; // Posición X de la comida
    static int foodY = 0; // Posición Y de la comida
    static int cornersize = 20; // Tamaño de los cuadrados en los que se representa la serpiente y la comida
    static List<SnakeBody> snake = new ArrayList<>(); // Lista que representa a la serpiente
    static Direction direction = Direction.left; // Dirección inicial de la serpiente
    static boolean gameOver = false; // Indica si el juego ha terminado
    static Random generadorRandom = new Random(); // Objeto para generar números aleatorios
    private Button restartButton; // Botón para reiniciar el juego
    private Button infoButton; // Botón para mostrar información sobre el juego
    private boolean isSnakeMoving = false; // Indica si la serpiente se está moviendo
    private long startTime; // Tiempo de inicio del juego
    private long elapsedTime; // Tiempo transcurrido en segundos
    private boolean isTimerActive = false; // Indica si el temporizador está activo
    private boolean isPaused = false; // Indica si el juego está en pausa


    // Método principal que inicia la aplicación
    public static void main(String[] args) {
        launch(args);
    }

    // Método principal que inicia la aplicación
    public void start(Stage primaryStage) {
        try {

            // Configuración de la interfaz gráfica
            VBox rootContainer = new VBox();
            Canvas canvas = new Canvas(width * cornersize, height * cornersize - 25); // Área de juego
            graphicsContext = canvas.getGraphicsContext2D(); // Contexto gráfico del área de juego
            rootContainer.getChildren().add(canvas);

            // Configuración de los botones
            restartButton = new Button("Reiniciar juego");
            restartButton.setOnAction(event -> restartGame());

            infoButton = new Button("Acerca de");
            infoButton.setOnAction(event -> showAboutDialog());

            // Configuración del contenedor de botones
            HBox buttonContainer = new HBox(10); // Espaciado de 10 píxeles entre los botones
            buttonContainer.setAlignment(Pos.CENTER);
            buttonContainer.getChildren().addAll(restartButton, infoButton);

            // Configuración de los márgenes
            VBox.setMargin(buttonContainer, new Insets(10, 0, 10, 0)); // Márgenes superior e inferior

            // Añadir el contenedor de botones al diseño
            rootContainer.getChildren().add(buttonContainer);

            // Configuración de la escena y presentación de la ventana
            scene = new Scene(rootContainer, (width) * cornersize, (height + 1) * cornersize); // Ajustar tamaño de la escena

            // Configuración de la escena y la interacción del teclado
            configureKeyboardInput(rootContainer);

            // Inicialización de la serpiente con partes iniciales
            snake.add(new SnakeBody(width / 2, height / 2));
            snake.add(new SnakeBody(width / 2, height / 2));

            startGameAnimation(graphicsContext); // Iniciar el bucle de animación del juego (tick)

            primaryStage.setScene(scene);
            primaryStage.setTitle("SNAKE GAME");
            primaryStage.show();

            // Establecer el foco en el nodo raíz de la escena
            scene.getRoot().requestFocus();

            // Mostrar el diálogo de bienvenida/instrucciones
            showWelcomeDialog();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Método que muestra un diálogo de bienvenida/instrucciones
    private void showWelcomeDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Bienvenido a Snake Game");
        alert.setHeaderText(null);
        alert.setContentText("""
                Instrucciones: \n
                    - Usa las teclas de dirección para mover la serpiente. \n
                    - Pulsa la barra espaciadora para pausar/reanudar el juego. \n
                    - Come la comida para hacer crecer la serpiente y aumentar\s
                      la velocidad. \n
                    - Evita chocar con las paredes y contigo mismo. \n
                    - Pulsa 'Restart Game' para reiniciar el juego.""");
        alert.showAndWait();
    }
    // Método que muestra un diálogo acerca del juego y su desarrollador
    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Acerca de Snake Game");
        alert.setHeaderText(null);
        alert.setContentText("Snake Game\n\nDesarrollado por Diego\nVersión 1.0");
        alert.showAndWait();
    }

    // Método para configurar la interacción del teclado
    private void configureKeyboardInput(VBox root) {
        Scene scene = root.getScene();

        // Agrega un manejador de eventos para las teclas de dirección en el contenedor raíz
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN || event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.RIGHT) {

                event.consume(); // Consume el evento para evitar que se propague a los botones
            }
        });

        // Agrega el manejador de eventos para las teclas en el contenedor raíz
        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPress);
    }
    // Método que maneja las pulsaciones de teclas
    private void handleKeyPress(KeyEvent keyEvent) {
        if (!isSnakeMoving) {
            isSnakeMoving = true; // La serpiente ha comenzado a moverse
            isTimerActive = true; // El temporizador se ha activado
            startTime = System.currentTimeMillis(); // Iniciar el temporizador
            newFood(); // Generar la primera comida
        }

        KeyCode code = keyEvent.getCode();

        if (code == KeyCode.UP || code == KeyCode.W) {
            direction = Direction.up;
        } else if (code == KeyCode.LEFT || code == KeyCode.A) {
            direction = Direction.left;
        } else if (code == KeyCode.DOWN || code == KeyCode.S) {
            direction = Direction.down;
        } else if (code == KeyCode.RIGHT || code == KeyCode.D) {
            direction = Direction.right;
        } else if (code == KeyCode.SPACE && !gameOver) {
            // Toggle para pausar o reanudar el juego al presionar la barra espaciadora
            isPaused = !isPaused;
            // Mostrar mensaje de pausa si el juego está en pausa
            if (isPaused) {
                graphicsContext.setFill(Color.BLACK);
                graphicsContext.setFont(Font.font("", FontWeight.BOLD, 80));
                graphicsContext.fillText("PAUSE", 180, 290);
            }
        } else if (code == KeyCode.R) {
            restartGame();
        } else {
            // Si la tecla no es "R" o la barra espaciadora, consume el evento
            keyEvent.consume();
        }
    }

    // Método para iniciar el bucle de animación del juego
    private void startGameAnimation(GraphicsContext graphicsContext) {
        new AnimationTimer() {
            long lastTick = 0; // Último tick del temporizador

            public void handle(long now) {

                if (lastTick == 0) {
                    lastTick = now;
                    tick(graphicsContext); // Actualizar el estado del juego en cada frame
                    return;
                }

                if (!isPaused && now - lastTick > 1000000000 / speed) {
                    lastTick = now;
                    tick(graphicsContext); // Actualizar el estado del juego a la velocidad indicada
                }
            }
        }.start();
    }
    // Método que actualiza el estado del juego en cada frame
    public void tick(GraphicsContext graphicsContext) {

        // Cargar la imagen de fondo
        Image backgroundImage = new Image(Objects.requireNonNull(getClass().getResource("/com/snakegame/snakegame/tableroSnake.png")).toExternalForm());

        // Rellenar el fondo del área de juego con la imagen
        graphicsContext.drawImage(backgroundImage, 0, 0, width * cornersize, height * cornersize);

        // Mostrar la puntuación en la pantalla
        graphicsContext.setFill(Color.BLACK);
        graphicsContext.setFont(Font.font("", FontWeight.BOLD, 25));
        graphicsContext.fillText("Score: " + (speed - 5), 10, 30);

        // Mostrar el tiempo en la pantalla
        graphicsContext.setFill(Color.BLACK);
        graphicsContext.setFont(Font.font("", FontWeight.BOLD, 20));
        graphicsContext.fillText("Time: " + elapsedTime + "s", 10, 60);

        // Dibujar la serpiente
        for (SnakeBody snakeBody : snake) {
            if (snakeBody == snake.get(0)) {
                // Dibujar la cabeza de la serpiente
                graphicsContext.setFill(Color.RED);
                double headSize = cornersize * 1.5; // Ajusta el tamaño de la cabeza según tus preferencias
                graphicsContext.fillOval(snakeBody.x * cornersize, snakeBody.y * cornersize, headSize, headSize);
            } else {
                // Dibujar el cuerpo de la serpiente
                graphicsContext.setFill(Color.BLUE);
                double bodySize = cornersize * 1.5; // Ajusta el tamaño del cuerpo según tus preferencias
                graphicsContext.fillOval(snakeBody.x * cornersize, snakeBody.y * cornersize, bodySize, bodySize);
            }
        }

        if (!isSnakeMoving) {
            return; // La serpiente no se mueve hasta que el jugador presione una tecla
        }

        // Mostrar mensaje de pausa si el juego está en pausa
        if (isTimerActive) {
            elapsedTime = (System.currentTimeMillis() - startTime) / 1000; // Tiempo en segundos
        }

        if (gameOver) {
            // Mostrar mensaje de fin de juego si el juego ha terminado
            graphicsContext.setFill(Color.RED);
            graphicsContext.setFont(Font.font("", FontWeight.BOLD, 80));
            graphicsContext.fillText("GAME OVER", 70, 290);
            isTimerActive = false; // Detener el temporizador
            return;
        }

        // Actualizar la posición de los segmentos de la serpiente
        for (int i = snake.size() - 1; i >= 1; i--) {
            snake.get(i).x = snake.get(i - 1).x; // Actualizar la posición X
            snake.get(i).y = snake.get(i - 1).y; // Actualizar la posición Y
        }

        // Mover la cabeza de la serpiente en la dirección indicada por el jugador
        switch (direction) {
            case up:
                snake.get(0).y--;
                if (snake.get(0).y < 0) {
                    gameOver = true;
                }
                break;
            case down:
                snake.get(0).y++;
                if (snake.get(0).y > height) {
                    gameOver = true;
                }
                break;
            case left:
                snake.get(0).x--;
                if (snake.get(0).x < 0) {
                    gameOver = true;
                }
                break;
            case right:
                snake.get(0).x++;
                if (snake.get(0).x > width) {
                    gameOver = true;
                }
                break;
        }

        // Comprobar si la serpiente ha comido la comida
        if (foodX == snake.get(0).x && foodY == snake.get(0).y) {
            snake.add(new SnakeBody(-1, -1));
            speed++;
            newFood(); // Generar nueva comida
        }

        // Comprobar si la serpiente se ha autodestruido al chocar consigo misma
        for (int i = 1; i < snake.size(); i++) {
            if (snake.get(0).x == snake.get(i).x && snake.get(0).y == snake.get(i).y) {
                gameOver = true;
                break;
            }
        }

        // Elegir el color de la comida en función de foodcolor
        Color food = Color.WHITE;
        switch (foodcolor) {
            case 0:
                food = Color.PURPLE;
                break;
            case 1:
                food = Color.LIGHTBLUE;
                break;
            case 2:
                food = Color.YELLOW;
                break;
            case 3:
                food = Color.PINK;
                break;
            case 4:
                food = Color.ORANGE;
                break;
        }

        // Dibujar la comida en la posición actual
        graphicsContext.setFill(food);
        double foodSize = cornersize * 1.3; // Ajusta el tamaño de la comida según tus preferencias
        graphicsContext.fillOval(foodX * cornersize, foodY * cornersize, foodSize, foodSize);
    }

    // Método para generar nueva comida en una posición aleatoria
    public static void newFood() {
        start: while (true) {
            foodX = generadorRandom.nextInt(width - 1);
            foodY = generadorRandom.nextInt(height - 1);

            // Comprobar si la nueva posición de la comida colisiona con la serpiente
            for (SnakeBody snakeBody : snake) {
                if (snakeBody.x == foodX && snakeBody.y == foodY) {
                    continue start;
                }
            }

            // Asegúrate de que la posición de la comida no esté fuera de los límites
            if (foodX >= width || foodY >= height) {
                continue;
            }

            foodcolor = generadorRandom.nextInt(5); // Asignar un nuevo color a la comida
            //speed++; // Aumentar la velocidad del juego
            break;
        }
    }

    // Método para reiniciar el juego
    private void restartGame() {
        Alert confirmRestart = new Alert(Alert.AlertType.CONFIRMATION);
        confirmRestart.setTitle("Reiniciar el juego");
        confirmRestart.setHeaderText(null);
        confirmRestart.setContentText("¿Estás seguro de que deseas reiniciar el juego?");
        Optional<ButtonType> result = confirmRestart.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Restablecer todas las variables del juego a su estado inicial
            isSnakeMoving = false;
            elapsedTime = 0; // Reiniciar el tiempo
            startTime = 0;
            speed = 5;
            foodcolor = 0;
            gameOver = false;
            snake.clear();
            snake.add(new SnakeBody(width / 2, height / 2));
            snake.add(new SnakeBody(width / 2, height / 2));
            direction = Direction.left;
            newFood();
            // Establecer el foco en el nodo raíz de la escena
            scene.getRoot().requestFocus();
        }
    }
}
