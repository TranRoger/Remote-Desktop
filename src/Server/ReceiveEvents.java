/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

import java.awt.Robot;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ReceiveEvents extends Thread {
    private Socket socket = null;
    private Robot robot = null;
    boolean continueLoop = true;

    public ReceiveEvents(Socket s, Robot r) {
        socket = s;
        robot = r;
        start();
    }

    @Override
    public void run() {
        Scanner scanner;
        scanner = null;
        try {
            scanner = new Scanner(socket.getInputStream());

            while (continueLoop) {
                int command = scanner.nextInt();
                switch(command) {
                    case -1 -> robot.mousePress(scanner.nextInt());
                    case -2 -> robot.mouseRelease(scanner.nextInt());
                    case -3 -> robot.keyPress(scanner.nextInt());
                    case -4 -> robot.keyRelease(scanner.nextInt());
                    case -5 -> robot.mouseMove(scanner.nextInt(), scanner.nextInt());
                    case -6 -> robot.mouseWheel(scanner.nextInt());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}