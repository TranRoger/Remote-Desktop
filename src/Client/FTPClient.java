/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author Admin
 */
public class FTPClient extends Thread {
    private Socket socket = null;
    private Socket checkSocket = null;
    private ObjectOutputStream out = null;
    private ObjectInputStream in = null;
    private JFrame frame;
    private JTree client, server;
    private DefaultMutableTreeNode clientRoot, serverRoot;
    private JButton upload, download;
    private JPanel panel;
    private ObjectOutputStream checkOut = null;
    private DataInputStream checkIn = null;
    String up, down;
    
    public FTPClient(InetAddress ip) {
        try {
            socket = new Socket(ip, 2004);
            checkSocket = new Socket(ip, 1234);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            checkOut = new ObjectOutputStream(checkSocket.getOutputStream());
            checkIn = new DataInputStream(checkSocket.getInputStream());
            start();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void run() {
        drawGUI();
    }
    
    private void drawGUI() {
        frame = new JFrame();
        upload = new JButton("Send to server");
        download = new JButton("Download to local folder");
        panel = new JPanel();
        
        upload.setEnabled(false);
        download.setEnabled(false);
                 
        // Client folder tree
        clientRoot = new DefaultMutableTreeNode("Local");
        client = new JTree(clientRoot);
        JScrollPane clientScrollPane = new JScrollPane(client);   
        client.addTreeSelectionListener((TreeSelectionEvent e) -> {
            up = handleTreeSelection(e, client);
            File file = new File(up);
            if (file.isDirectory()) {
                AddLocalFolders(file, (DefaultMutableTreeNode) client.getLastSelectedPathComponent());
                client.expandPath(client.getSelectionPath());
            }
        });
        
        // Server folder tree
        serverRoot = new DefaultMutableTreeNode("Server");
        server = new JTree(serverRoot);
        JScrollPane serverScrollPane = new JScrollPane(server);
        server.addTreeSelectionListener((TreeSelectionEvent e) -> {
            down = handleTreeSelection(e, server);
            File file = new File(down);
            if (checkFolder(file)) {
                AddLocalFolders(file, (DefaultMutableTreeNode) server.getLastSelectedPathComponent());
                server.expandPath(server.getSelectionPath());
            }
        });
        
        // Button action               
        upload.addActionListener(e -> {
            if (up == null || down == null) {
                JOptionPane.showMessageDialog(null, "Please choose a file or folder", "ERROR", JOptionPane.ERROR_MESSAGE);
            }
            else if (!new File(up).isFile()) {
                JOptionPane.showMessageDialog(null, "Please select a file in client directory", "Not a file", JOptionPane.ERROR_MESSAGE);
            }
            else if (!checkFolder(new File(down)))
                JOptionPane.showMessageDialog(null, "Please select a folder in server directory", "Not a folder", JOptionPane.ERROR_MESSAGE);
            else {
                try {
                    System.out.println("Send up file request");
                    out.writeUTF("receive");
                    out.flush();
                    SendFile(up, down + File.separator + client.getSelectionPath().getLastPathComponent());
                    JOptionPane.showMessageDialog(null, "File transfer completed");
                } catch (IOException ex) {
                    Logger.getLogger(FTPClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        download.addActionListener(e -> {
            if (up == null || down == null) {
                JOptionPane.showMessageDialog(null, "Please choose a file or folder", "ERROR", JOptionPane.ERROR_MESSAGE);
            }
            else if (checkFolder(new File(down)))
                JOptionPane.showMessageDialog(null, "Please select a file in server directory", "Not a file", JOptionPane.ERROR_MESSAGE);
            else if (!new File(up).isDirectory())
                JOptionPane.showMessageDialog(null, "Please select a folder in client directory", "Not a folder", JOptionPane.ERROR_MESSAGE);
            else {
                try {
                    System.out.println("Send down file request");
                    out.writeUTF("send");
                    out.flush();
                    ReceiveFile(up + File.separator + server.getSelectionPath().getLastPathComponent(), down);
                    JOptionPane.showMessageDialog(null, "File transfer completed");
                } catch (IOException ex) {
                    Logger.getLogger(FTPClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        // draw
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, clientScrollPane, serverScrollPane);
        splitPane.setResizeWeight(0.5);
        
        panel.setLayout(new GridLayout(0, 2));
        panel.add(upload);
        panel.add(download);
        frame.add(splitPane);
                
        frame.setTitle("File Transfer");
        frame.add(panel, BorderLayout.SOUTH);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        URL iconURL = getClass().getResource("icons-transfer.png");
        // iconURL is null when not found
        ImageIcon icon = new ImageIcon(iconURL);
        frame.setIconImage(icon.getImage());  
        
        fetchRemoteFiles();
        populateLocalFiles();
        System.out.println("Display folders completed");
        upload.setEnabled(true);
        download.setEnabled(true);
    }
    
    private void fetchRemoteFiles() {
        try {
            // Receive information about remote drives
            Object remoteDrivesObj = in.readObject();
            if (remoteDrivesObj instanceof File[] remoteDrives) {
                updateRemoteTree(remoteDrives);
            }
            out.writeObject(null);
            System.out.println("Send end syncronize signal");
            out.flush();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void populateLocalFiles() {
        File[] localDrives = File.listRoots();
        updateLocalTree(localDrives);
    }

    private void updateLocalTree(File[] drives) {
        for (File drive : drives) {
            DefaultMutableTreeNode driveNode = new DefaultMutableTreeNode(drive);
            clientRoot.add(driveNode);
            AddLocalFolders(drive, driveNode);
        }
        ((DefaultTreeModel) client.getModel()).reload();
    }

    private void updateRemoteTree(File[] drives) {
        for (File drive : drives) {
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(drive);
            serverRoot.add(newNode);
            if (checkFolder(drive)) {
                AddServerFolders(drive, newNode);
            }
        }
        ((DefaultTreeModel) server.getModel()).reload();
    }
    
    private boolean checkFolder(File file) {
        try {
            System.out.println("Send check folder for " + file);
            checkOut.writeObject(file);
            checkOut.flush();            
            return checkIn.readBoolean();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void AddLocalFolders(File folder, DefaultMutableTreeNode parentNode) {
        if ("Windows".equals(folder.getName()) || "Program Files".equals(folder.getName()) || "Program Files (x86)".equals(folder.getName()) || folder.getName().equals("Microsoft"))
            return;
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                DefaultMutableTreeNode newNode = createNode(file);
                parentNode.add(newNode);
            }
        }
    }
    
    private void AddServerFolders(File folder, DefaultMutableTreeNode parentNode) {
        if (folder.getName().equals("Windows") ||folder.getName().equals("Program Files") ||folder.getName().equals("Program Files (x86)"))
            return;
        try {
            System.out.println("Send request for " + folder + " list");
            out.writeObject(folder.getPath());
            out.flush();
            File[] files = (File[]) in.readObject();
            if (files != null) {
                for (File file : files) {
                    DefaultMutableTreeNode newNode = createNode(file);
                    parentNode.add(newNode);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private DefaultMutableTreeNode createNode(File file) {
        return new DefaultMutableTreeNode(file.getName());
    }

    private String handleTreeSelection(TreeSelectionEvent e, JTree tree) {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

        if (selectedNode != null) {
            // Get the path of the selected node
            TreePath path = e.getPath();
            StringBuilder folderPath = new StringBuilder();

            // Traverse the path to construct the folder path
            for (int i = 1; i < path.getPathCount(); i++) {
                Object pathComponent = path.getPathComponent(i);
                if (pathComponent instanceof DefaultMutableTreeNode node) {
                    folderPath.append(node.getUserObject());
                    if (i < path.getPathCount() - 1) {
                        folderPath.append(File.separator);
                    }
                }
            }
            return folderPath.toString();
        }
        return null;
    }
    
    private void SendFile(String upPath, String downPath) {
        try {
            FileInputStream fileInputStream = new FileInputStream(upPath);
            // Send the new file server path
            out.writeUTF(downPath);
            out.flush();
            long fileSize = new File(upPath).length();
            out.writeObject(fileSize);
            out.flush();
            System.out.println("Sending file: " + upPath + " (Size: " + fileSize + " bytes)");

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
    
    private void ReceiveFile(String upFile, String downFile) {
        try {
            out.writeUTF(downFile);
            out.flush();
            
            FileOutputStream fileOutputStream = new FileOutputStream(upFile);
            long fileSize = (long) in.readObject();
            System.out.println("Received file: " + upFile + " (Size: " + fileSize + " bytes)");

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
            Logger.getLogger(FTPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
