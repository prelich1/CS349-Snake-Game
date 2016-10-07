import java.awt.*;
import java.awt.event.*;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import javax.swing.*;

/**
 * Created by Prelich on 5/15/2016.
 */
public class Board extends JPanel {

    // Position class used to store x and y coordinates of objects
    public class Position {
        int x;
        int y;

        Position(int a, int b){
            x = a;
            y = b;
        }
    }

    public class SnakeObject extends JComponent {

        // A vector stores the position of each snake segment,
        // the first element being the head
        Vector<Position> segments = new Vector<Position>();
        int direction = 1; // 0=up, 1=right, 2=down, 3=left

        // SnakeObject starts with three segments
        SnakeObject() {
            for(int i = 0; i < 3; i++) {
                segments.addElement(new Position(100 - (i * DOT_SIZE), 100));
            }
        }

        public void move() {
            // Increment each segment based on the position of the segment before it
            for (int i = segments.size() - 1; i > 0; i--) {
                segments.get(i).x = segments.get(i - 1).x;
                segments.get(i).y = segments.get(i - 1).y;
            }

            // Move the head according to the current direction
            Position pos = segments.firstElement();
            switch (direction) {
                case 0:
                    pos.y -= DOT_SIZE;
                    break;
                case 1:
                    pos.x += DOT_SIZE;
                    break;
                case 2:
                    pos.y += DOT_SIZE;
                    break;
                case 3:
                    pos.x -= DOT_SIZE;
                    break;
            }
            segments.setElementAt(pos, 0);
        }

        public void paintComponent(Graphics g) {
            for(int i = 0; i < segments.size(); i++) {
                g.drawRect(FRAME_OFFSET + segments.get(i).x, FRAME_OFFSET + segments.get(i).y, DOT_SIZE, DOT_SIZE);
            }
        }
    }

    public class SplashScreen extends JPanel{

        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            int offsetX = 190;
            int offsetY = 150;
            int width = 420;
            int height = 300;

            // BG
            g.setColor(Color.BLACK);
            g.fillRect(offsetX, offsetY, width, height);

            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.setColor(Color.WHITE);
            g.drawString("userid: mprelich", offsetX + 20, offsetY+80);
            g.drawString("Control the snake around the board and collect the apples to grow.", offsetX + 20, offsetY+120);
            g.drawString("Do not run into the bounds or yourself!", offsetX + 20, offsetY+140);
            g.drawString("Arrow keys to change direction of snake.", offsetX + 20, offsetY+160);
            g.drawString("P - Pause/Resume", offsetX + 20, offsetY+180);
            g.drawString("R - Restart Game", offsetX + 20, offsetY+200);
            g.drawString("PRESS ANY KEY TO START GAME", offsetX + 20, offsetY+240);
        }
    }

    // CONSTANTS
    private int BOARD_WIDTH = 760;
    private int BOARD_HEIGHT = 500;
    private int DOT_SIZE = 20;          // "Pixel size"
    private int FRAME_OFFSET = 15;      // Used to center the board in the frame

    private SnakeObject snake;
    private Position applePos;
    private SplashScreen splashScreen;
    private Timer paintTimer;               // Use one timer for repainting according to framerate
    private Timer moveTimer;                // Use another for moving the snake and checking collision according to speed

    private boolean paused = false;
    private boolean inGame = false;
    private int score = 0;
    private int FPS = 0;
    private int gameSpeed = 0;

    // Sounds
    private Clip biteClip  = null;
    private Clip gameOverClip  = null;

    public Board(int frameRate, int speed) {

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();

                // Press any key to start game from splashscreen
                if(!inGame){
                    initGame();
                }
                else {
                    if (key == KeyEvent.VK_UP && snake.direction != 2 && !paused) {
                        snake.direction = 0;
                    } else if (key == KeyEvent.VK_RIGHT && snake.direction != 3 && !paused) {
                        snake.direction = 1;
                    } else if (key == KeyEvent.VK_DOWN && snake.direction != 0 && !paused) {
                        snake.direction = 2;
                    } else if (key == KeyEvent.VK_LEFT && snake.direction != 1 && !paused) {
                        snake.direction = 3;
                    } else if (key == KeyEvent.VK_P) {
                        if (!paused) {
                            paused = true;
                            moveTimer.stop();
                        } else {
                            paused = false;
                            moveTimer.start();
                        }
                    } else if (key == KeyEvent.VK_R && !paused) {   // Restart
                        initGame();
                    }
                }

            }
        });

        // Action for paintTimer
        ActionListener paintPerformer = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                repaint();
            }
        };
        // Action for moveTimer
        ActionListener movePerformer = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                snake.move();
                checkSnakeCollision();
                checkAppleCollision();
            }
        };

        loadSounds();
        splashScreen = new SplashScreen();

        FPS =  frameRate;
        gameSpeed = speed;
        paintTimer = new Timer(1000/frameRate, paintPerformer);
        moveTimer = new Timer(500/speed, movePerformer);

        // We need to initialize these so they are drawn under the splashscreen
        snake = new SnakeObject();
        applePos = createApplePos();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        snake.paintComponent(g);                                                              // Draw Snake
        g.setFont(new Font("Arial", Font.BOLD, 30));
        g.drawRect(FRAME_OFFSET, FRAME_OFFSET, BOARD_WIDTH, BOARD_HEIGHT);                    // Draw Board Bound
        g.drawString("Score: " + score, 17, BOARD_HEIGHT + 50);                               // Draw Score
        g.drawString("FPS: " + FPS + "   Speed: " + gameSpeed, 490, BOARD_HEIGHT + 50);       // Draw FPS and Speed
        g.setColor(Color.RED);
        g.fillRect(FRAME_OFFSET+applePos.x, FRAME_OFFSET+applePos.y, DOT_SIZE, DOT_SIZE);     // Draw Apple

        g.setColor(Color.BLACK);
        if(paused) {
            g.drawString("PAUSED", BOARD_WIDTH/2-50, BOARD_HEIGHT/2);
        }

        if(!inGame) {
            splashScreen.paintComponent(g);
        }
    }

    private void initGame() {
        snake = new SnakeObject();
        applePos = createApplePos();
        score = 0;
        paintTimer.start();
        moveTimer.start();
        inGame = true;
    }

    private void loadSounds() {
        try {
            File soundFile = new File("bite.wav");
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            biteClip = AudioSystem.getClip();
            biteClip.open(audioIn);

            soundFile = new File("game_over.wav");
            audioIn = AudioSystem.getAudioInputStream(soundFile);
            gameOverClip = AudioSystem.getClip();
            gameOverClip.open(audioIn);

        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void checkSnakeCollision() {
        int x = snake.segments.firstElement().x;
        int y = snake.segments.firstElement().y;

        // Check Bounds
        if (x < 0 || y < 0 || x >= BOARD_WIDTH || y >= BOARD_HEIGHT) {
            inGame = false;
            moveTimer.stop();
            gameOverClip.setFramePosition(0);
            gameOverClip.start();
        }

        // Check Snake self collision
        for (int i = 1; i < snake.segments.size(); i++) {
            if (x == snake.segments.get(i).x && y == snake.segments.get(i).y) {
                inGame = false;
                moveTimer.stop();
                gameOverClip.setFramePosition(0);
                gameOverClip.start();
            }
        }
    }

    private Position createApplePos() {
        // Get random position for apple so always offset by the DOT_SIZE
        int randomX = (int) (Math.random() * BOARD_WIDTH / DOT_SIZE) ;
        int randomY = (int) (Math.random() * BOARD_HEIGHT / DOT_SIZE);
        Position newApplePos = new Position(randomX * DOT_SIZE, randomY * DOT_SIZE);

        // Make sure the apple is no in the body of the snake
        // If it is, create a new one instead
        if ( checkAppleInBody(newApplePos) ) {
            newApplePos = createApplePos();
        }
        return newApplePos;
    }

    private boolean checkAppleInBody(Position applePos) {
        for (int i = 0; i < snake.segments.size(); i++) {
            if (snake.segments.get(i).x == applePos.x && snake.segments.get(i).y == applePos.y){
                return true;
            }
        }
        return false;
    }

    private void checkAppleCollision() {
        if (snake.segments.firstElement().x == applePos.x &&
                snake.segments.firstElement().y == applePos.y) {
            score++;
            snake.segments.addElement(applePos);
            applePos = createApplePos();

            biteClip.setFramePosition(0);
            biteClip.start();
        }
    }

}
