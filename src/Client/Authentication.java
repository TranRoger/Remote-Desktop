/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author Admin
 */
public class Authentication {
    private DataInputStream dataIn = null;
    private DataOutputStream password = null;
    String verify = "";
    String width = "", height = "";
    
    public void Verify(Socket cSocket, String serverIP, String serverPassword) {
        try {  
            dataIn = new DataInputStream(cSocket.getInputStream());
            password = new DataOutputStream(cSocket.getOutputStream());
            password.writeUTF(serverPassword);
            verify = "" + dataIn.readUTF();
        } catch (IOException ex) {
            Logger.getLogger(Authentication.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (verify.equals("valid")) {
            try {
                width = dataIn.readUTF();
                height = dataIn.readUTF();
            } catch (IOException e) {
                e.printStackTrace();
            }
            CreateFrame abc = new CreateFrame(cSocket, width, height);
        } else {
            System.out.println("Incorrect password");
            JOptionPane.showMessageDialog(null, "Incorrect password", "Wrong password", JOptionPane.ERROR_MESSAGE);
        }
    }
}
