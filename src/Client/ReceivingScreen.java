/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;

import java.awt.BorderLayout;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class ReceivingScreen extends Thread {
    private JPanel cPanel = null;
    InputStream oin = null;
    Image image1 = null;

    public ReceivingScreen(InputStream in, JPanel cPanel) {
        this.cPanel = cPanel;
        this.oin = in;
        start();
    }

    public void run() {
        try {
            while (true) {
                byte[] bytes = new byte[1024 * 1024];
                int count = 0;
                do {
                    count += oin.read(bytes, count, bytes.length - count);
                } while (!(count > 4 && bytes[count - 2] == (byte) - 1 && bytes[count - 1] == (byte) - 39));
                image1 = ImageIO.read(new ByteArrayInputStream(bytes));
                image1 = image1.getScaledInstance(cPanel.getWidth(), cPanel.getHeight(), Image.SCALE_FAST);
                Graphics graphics = cPanel.getGraphics();
                graphics.drawImage(image1, 0, 0, cPanel.getWidth(), cPanel.getHeight(), cPanel);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void saveImageWithFileChooser() {
        BufferedImage bi = toBufferedImage(image1);
        displayImageInPane(bi);
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose a directory to save the image");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int userSelection = fileChooser.showSaveDialog(null);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            // Get the selected directory
            File selectedDirectory = fileChooser.getSelectedFile();

            // Specify the file name and path for the saved image
            File outputFile = new File(selectedDirectory, "saved_image.png");

            // Save the BufferedImage to the specified file
            try {
                ImageIO.write(bi, "png", outputFile);
                System.out.println("Image saved successfully to: " + outputFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private static void displayImageInPane(BufferedImage image) {
        JFrame frame = new JFrame("Image Display");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Create a JLabel to display the image
        JLabel label = new JLabel(new ImageIcon(image));

        // Set layout manager and add the label to the frame
        frame.setLayout(new BorderLayout());
        frame.add(label, BorderLayout.CENTER);

        // Set the size of the frame
        frame.setSize(400, 300);

        // Center the frame on the screen
        frame.setLocationRelativeTo(null);
        
        URL iconURL = ReceivingScreen.class.getResource("icons-image.png");
        // iconURL is null when not found
        ImageIcon icon = new ImageIcon(iconURL);
        frame.setIconImage(icon.getImage());

        // Make the frame visible
        frame.setVisible(true);
    }
    
    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage)
        {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }
}