/**
 * Created by Prelich on 5/10/2016.
 */
import javax.swing.*;

public class snake extends JComponent
{
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                int frameRate = 60;
                int speed = 5;

                // Check for valid arguments
                if (args.length == 2){
                    frameRate = Integer.parseInt(args[0]);
                    speed = Integer.parseInt(args[1]);
                }

                // Create Window
                JFrame f = new JFrame("Snake");
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setSize(800, 600);
                f.setResizable(false);

                // Create and add board
                Board board = new Board(frameRate, speed);
                board.setFocusable(true);
                f.add(board);

                f.setVisible(true);
            }
        });
    }
}
