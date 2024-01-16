/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.io.IOException;
import java.net.Socket;
import javax.imageio.ImageIO;

public class SendScreen extends Thread {
    Socket socket = null;
    Robot robot = null;
    Rectangle rectangle = null;
    boolean continueLoop = true;
    OutputStream oos = null;

    public SendScreen(Socket s, Robot ro, Rectangle re) {
        socket = s;
        robot = ro;
        rectangle = re;
        start();
    }

    @Override
    public void run() {
        try {
            oos = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (continueLoop) {
            BufferedImage image = robot.createScreenCapture(rectangle);
            try {
                ImageIO.write(image, "jpeg", oos);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}