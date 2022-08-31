import javax.swing.*;
import java.awt.*;
import javax.imageio.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class VisualGameOfLife extends JFrame{

    public VisualGameOfLife() {
        int width = 1420, height = 1080;
        setTitle("Program 6");
        GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        environment.getMaximumWindowBounds();
        GraphicsDevice device = environment.getDefaultScreenDevice();
        GraphicsConfiguration config = device.getDefaultConfiguration();
        setSize(width, height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // this method display the JFrame to center position of a screen
        JLayeredPane jLP = new JLayeredPane();
        try {
            BufferedImage myPicture = ImageIO.read(new File("Program6.jpg"));
             JLabel mainLabel = new JLabel(new ImageIcon(myPicture.getScaledInstance(this.getBounds().getSize().width, this.getBounds().getSize().height, Image.SCALE_FAST)));
             mainLabel.setBounds(0, 0, this.getBounds().getSize().width, this.getBounds().getSize().height);
             jLP.add(mainLabel, JLayeredPane.PALETTE_LAYER);
        } catch (Exception e) {}
        MyPanel example;
        final MyPanel[][] panelArray = new MyPanel[9][10];
        int offsetW = (this.getBounds().getSize().width/2)-(51*10)/2;
        int offsetH = (this.getBounds().getSize().height/2)-(51*9)/2;
        JPanel background = new JPanel();
        background.setBounds(offsetW-15, offsetH-15, 51*10+30, 51*9+27);
        jLP.add(background, JLayeredPane.MODAL_LAYER);
        for (int row = 0; row < 9; ++row)
            for (int col = 0; col < 10; ++col) {
                example = new MyPanel(""+row+col);
                example.setBounds((col*50+1*col)+offsetW, (row*50+1*row)+offsetH, 50, 50);
                panelArray[row][col] = example;
	        jLP.add(example, JLayeredPane.POPUP_LAYER);
            }
        final MyPanel constantPanel = new MyPanel("");
        JButton resetBoard = new JButton("Reset Board");
        resetBoard.setFont(new Font("Arial", Font.PLAIN, 40));
        resetBoard.setBounds(50,offsetH+(51*9/2), resetBoard.getText().length()*30 + 10 , 50);
        resetBoard.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            for (int i=0; i<9; ++i)
                for (int j=0; j<10; ++j){
                    constantPanel.getBoard().setCell(j, i, 0);
                    panelArray[i][j].reset();
                }
         }});
        jLP.add(resetBoard, JLayeredPane.MODAL_LAYER);
        JButton nextGen = new JButton("Next Generation");
        nextGen.setFont(new Font("Arial", Font.PLAIN, 40));
        nextGen.setBounds(offsetW*2+50,offsetH+(51*9/2), nextGen.getText().length()*25 + 10 , 50);
        nextGen.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            constantPanel.getBoard().computeNextGeneration(1);
            for (int i=0; i<9; ++i){
                for (int j=0; j<10; ++j)
                    panelArray[i][j].setActivity(constantPanel.getBoard().getCell(j, i));
            }
         }});
        jLP.add(nextGen, JLayeredPane.MODAL_LAYER);
        add(jLP);
        setVisible(true);
        addComponentListener(new ComponentAdapter() 
{  
    public void componentResized(ComponentEvent evt) {
        JLayeredPane jLP = new JLayeredPane();
        try {
            BufferedImage myPicture = ImageIO.read(new File("Program6.jpg"));
             JLabel mainLabel = new JLabel(new ImageIcon(myPicture.getScaledInstance(evt.getComponent().getBounds().getSize().width, evt.getComponent().getBounds().getSize().height, Image.SCALE_FAST)));
             mainLabel.setBounds(0, 0, evt.getComponent().getBounds().getSize().width, evt.getComponent().getBounds().getSize().height);
             jLP.add(mainLabel, JLayeredPane.PALETTE_LAYER);
        add(jLP);
        setVisible(true);
        } catch (Exception e) {}
    }
});
   }

    public static void main (String[] args) {
        new VisualGameOfLife();
    }
}

class MyPanel extends JPanel{
    private int squareX = 0;
    private int squareY = 0;
    private int squareW = 50;
    private int squareH = 50;
    boolean active = false;
    static GameOfLife gOL;
    String id;

    public MyPanel(String identity){
        id = identity;
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                active = !active;
                gOL.setCell(Integer.parseInt(id.charAt(1)+""), Integer.parseInt(id.charAt(0)+""), active?1:0);
                repaint();
            }
        });
        createFile();	
        if (gOL == null)
            gOL = new GameOfLife();
    }
    
    public void reset(){
        active = false;
        repaint();
    }

    public void setActivity(int n){
        if(n==1)
            active = true;
        else
            active = false;
        repaint();
    }

    public static void createFile(){
        try {
            PrintWriter pw = new PrintWriter("golInput.dat");
            pw.println("9 10");
            pw.println("0 0 0 0 0 0 0 0 0 0");
            pw.println("0 0 0 0 0 0 0 0 0 0");
            pw.println("0 0 0 0 0 0 0 0 0 0");
            pw.println("0 0 0 0 0 0 0 0 0 0");
            pw.println("0 0 0 0 0 0 0 0 0 0");
            pw.println("0 0 0 0 0 0 0 0 0 0");
            pw.println("0 0 0 0 0 0 0 0 0 0");
            pw.println("0 0 0 0 0 0 0 0 0 0");
            pw.println("0 0 0 0 0 0 0 0 0 0");
            pw.close();
            pw = new PrintWriter("golStarter.dat");
            pw.println("golInput.dat");
            pw.close();
            File f = new File("golStarter.dat");
            FileInputStream stream = new FileInputStream(f);
            System.setIn(stream);
        } catch (Exception e) {}
    }

    public GameOfLife getBoard(){
        return gOL;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (active)
            g.setColor(Color.GREEN);
        else {
        	float color[] = Color.RGBtoHSB(97, 32, 0, null);
            g.setColor(Color.getHSBColor(color[0], color[1], color[2]));
        }
        g.fillRect(squareX,squareY,squareW,squareH);
    }
}
