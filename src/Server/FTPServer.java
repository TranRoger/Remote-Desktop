/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Admin
 */
public class FTPServer extends Thread {
    private ServerSocket server = null;    
    private ServerSocket checkS = null;
    private Socket cSocket = null;
    private ObjectInputStream in = null;
    private ObjectOutputStream out = null;
    
    public FTPServer() {
        try {
            server = new ServerSocket(2004);            
            checkS = new ServerSocket(1234);
            System.out.println("FTP ready");
            start();
        } catch (IOException ex) {
            Logger.getLogger(FTPServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void run() {
        try {
            while (true) {
                cSocket = server.accept();
                System.out.println("FTP client connected");
                in = new ObjectInputStream(cSocket.getInputStream());
                out = new ObjectOutputStream(cSocket.getOutputStream());
                
                new check();
                SendDirectory();
                System.out.println("Sending folders completed");
                while (true) {
                    String request = in.readUTF();
                    System.out.println(request);
                    if (request.equals("send")) {
                        System.out.println("Receive up file request");
                        SendFile();
                    }
                    if (request.equals("receive")) {
                        System.out.println("Receive down file request");
                        ReceiveFile();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void SendDirectory() {
        try {
            File[] localDrives = File.listRoots();
            out.writeObject(localDrives);
            out.flush();

            // Receive the remote folder path from the client
            String remoteFolderPath;
            try {
                while ((remoteFolderPath = (String) in.readObject()) != null) {
                    // Send information about remote drives
                    File files = new File(remoteFolderPath);
                    File[] remoteFiles = files.listFiles();
                    out.writeObject(remoteFiles);
                    out.flush();
                    System.out.println("Sent " + files + " list");
                }
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(FTPServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
        
    private void ReceiveFile() {
        try {            
            // Read the file name and size from the client
            String filePath = in.readUTF();
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            long fileSize = (long) in.readObject();
            System.out.println("Received file: " + filePath + " (Size: " + fileSize + " bytes)");

            // Read the file content from the client and write it to a file
            byte[] buffer = new byte[1024];
            int bytesRead;
            while (fileSize > 0 && (bytesRead = in.read(buffer, 0, (int)Math.min(buffer.length, fileSize))) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
                fileSize -= bytesRead;
            }
            fileOutputStream.close();
            System.out.println("File transfer completed");
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(FTPServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void SendFile() {
        try {
            // Read file path from client
            String filePath = in.readUTF();
            FileInputStream fileInputStream = new FileInputStream(filePath);
            long fileSize = new File(filePath).length();
            out.writeObject(fileSize);
            System.out.println("Sending file: " + filePath + " (Size: " + fileSize + " bytes)");

            // Send the file content to the server
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                out.flush();
            }
            fileInputStream.close();
            System.out.println("File transfer completed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    class check extends Thread {
        ObjectInputStream checkIn = null;
        DataOutputStream checkOut = null;
        
        public check() {
            System.out.println("Waiting for checking connection...");
            start();
        }  
        
        @Override
        public void run() {
            try {
                while (true) {
                    Socket chS = checkS.accept();
                    System.out.println("Connected");
                    checkIn = new ObjectInputStream(chS.getInputStream());
                    checkOut = new DataOutputStream(chS.getOutputStream());
                    
                    try {
                        while (true) {
                            File check = (File) checkIn.readObject();
                            System.out.println("Received request check folder for " + check);
                            if (check.isDirectory()) {
                                checkOut.writeBoolean(true);
                                checkOut.flush();
                            }
                            else {
                                checkOut.writeBoolean(false);
                                checkOut.flush();
                            }
                        }
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(FTPServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
}
