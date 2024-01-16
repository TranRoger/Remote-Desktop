/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

import java.net.InetAddress;

/**
 *
 * @author Admin
 */
public class Local_IP{
    
    public String getIPAddress() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            String IpAddress = localHost.getHostAddress();
            
            return IpAddress;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
