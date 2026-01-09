import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.util.*;
import java.io.*;
import java.net.*;

/**
 * MODULE 2: Hostel Room Information Service
 * Communication Model: Java RMI (Remote Method Invocation)
 * 
 * This module demonstrates:
 * - RMI architecture (Stub-Skeleton pattern)
 * - Remote interface definition and implementation
 * - RMI Registry for service discovery
 * - Distributed object management
 * - Remote method invocation across JVMs
 * 
 * Why RMI?
 * - Object-oriented distributed computing
 * - Transparent remote method calls
 * - Built-in Java support for distributed systems
 * - Demonstrates stub-skeleton interaction
 * - Suitable for service-oriented architecture
 * 
 * Why In-Memory Storage?
 * - Focus on RMI concepts rather than persistence
 * - Fast access for demonstration
 * - Academic project requirement
 * - Simplifies architecture for learning
 */

// Remote Interface - defines methods that can be invoked remotely
interface RoomInfoService extends Remote {
    // Remote methods must throw RemoteException
    String getRoomDetails(String roomNumber) throws RemoteException;
    String getWardenContact() throws RemoteException;
}

// Remote Implementation - runs on server side
class RoomInfoServiceImpl extends UnicastRemoteObject implements RoomInfoService {
    // In-memory storage for room data
    private Map<String, String> roomDatabase;
    
    // Constructor must throw RemoteException
    RoomInfoServiceImpl() throws RemoteException {
        super();
        // Initialize in-memory database
        roomDatabase = new HashMap<>();
        populateRoomData();
    }
    
    /**
     * Populates sample room data
     * In a real system, this would come from a database
     * For academic purposes, we use in-memory storage
     */
    private void populateRoomData() {
        roomDatabase.put("A-101", "Block A, Floor 1, Room 101 | Capacity: 2 | Status: Occupied");
        roomDatabase.put("A-102", "Block A, Floor 1, Room 102 | Capacity: 2 | Status: Available");
        roomDatabase.put("B-201", "Block B, Floor 2, Room 201 | Capacity: 3 | Status: Occupied");
        roomDatabase.put("B-202", "Block B, Floor 2, Room 202 | Capacity: 3 | Status: Occupied");
        roomDatabase.put("C-301", "Block C, Floor 3, Room 301 | Capacity: 2 | Status: Available");
    }
    
    /**
     * Remote method: Get room details
     * This method is invoked remotely by clients
     * The RMI runtime handles serialization and network communication
     */
    @Override
    public String getRoomDetails(String roomNumber) throws RemoteException {
        System.out.println("RMI Call received: getRoomDetails(" + roomNumber + ")");
        
        String details = roomDatabase.get(roomNumber);
        if (details != null) {
            return "Room: " + roomNumber + "\nDetails: " + details;
        } else {
            return "Room " + roomNumber + " not found in database.";
        }
    }
    
    /**
     * Remote method: Get warden contact information
     * Demonstrates multiple remote methods in same interface
     */
    @Override
    public String getWardenContact() throws RemoteException {
        System.out.println("RMI Call received: getWardenContact()");
        return "Warden: Dr. Ramesh Kumar\nPhone: +91-44-1234-5678\nEmail: warden@amrita.edu\nOffice: Hostel Block A, Ground Floor";
    }
}

// HTTP Bridge Server for Web UI
class RMIHTTPServer {
    private ServerSocket httpServer;
    private RoomInfoService rmiService;
    private final int port = 8082;
    
    RMIHTTPServer(RoomInfoService service) {
        this.rmiService = service;
    }
    
    void start() throws IOException {
        httpServer = new ServerSocket(port);
        System.out.println("HTTP Bridge Server started on port " + port);
        System.out.println("UI can access at: http://localhost:" + port);
        
        while (true) {
            Socket client = httpServer.accept();
            new Thread(() -> handleHTTPRequest(client)).start();
        }
    }
    
    private void handleHTTPRequest(Socket client) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            
            String request = in.readLine();
            if (request == null) return;
            
            String[] parts = request.split(" ");
            String method = parts[0];
            String path = parts[1];
            
            String headers = "HTTP/1.1 200 OK\r\n" +
                           "Content-Type: application/json\r\n" +
                           "Access-Control-Allow-Origin: *\r\n" +
                           "Access-Control-Allow-Methods: GET, OPTIONS\r\n" +
                           "Access-Control-Allow-Headers: Content-Type\r\n" +
                           "\r\n";
            
            if (method.equals("OPTIONS")) {
                out.print(headers);
                out.flush();
                client.close();
                return;
            }
            
            if (method.equals("GET")) {
                String response = "";
                
                if (path.startsWith("/room/")) {
                    String roomNumber = path.substring(6);
                    try {
                        String details = rmiService.getRoomDetails(roomNumber);
                        response = "{\"success\":true,\"data\":\"" + escapeJson(details) + "\"}";
                    } catch (RemoteException e) {
                        response = "{\"success\":false,\"error\":\"" + e.getMessage() + "\"}";
                    }
                } else if (path.equals("/warden")) {
                    try {
                        String contact = rmiService.getWardenContact();
                        response = "{\"success\":true,\"data\":\"" + escapeJson(contact) + "\"}";
                    } catch (RemoteException e) {
                        response = "{\"success\":false,\"error\":\"" + e.getMessage() + "\"}";
                    }
                } else {
                    response = "{\"error\":\"Invalid endpoint\"}";
                }
                
                out.print(headers + response);
                out.flush();
            }
            
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private String escapeJson(String str) {
        return str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}

public class Module2_RMI {
    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("MODULE 2: Room Information Service");
        System.out.println("Communication Model: Java RMI");
        System.out.println("==========================================");
        System.out.println();
        
        try {
            // Create remote object instance
            RoomInfoServiceImpl service = new RoomInfoServiceImpl();
            
            // Start RMI Registry (if not already running)
            try {
                LocateRegistry.createRegistry(1099);
                System.out.println("RMI Registry started on port 1099");
            } catch (ExportException e) {
                System.out.println("RMI Registry already running on port 1099");
            }
            
            // Bind remote object to registry
            // Clients will look up "RoomInfoService" to get the stub
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("RoomInfoService", service);
            System.out.println("RoomInfoService bound to RMI Registry");
            System.out.println("Remote object ready for client connections");
            System.out.println();
            
            // Start HTTP bridge server for UI
            RMIHTTPServer httpServer = new RMIHTTPServer(service);
            new Thread(() -> {
                try {
                    httpServer.start();
                } catch (IOException e) {
                    System.err.println("HTTP Server error: " + e.getMessage());
                }
            }).start();
            
            System.out.println("Server is running. Press Ctrl+C to stop.");
            System.out.println();
            
            // Keep server running
            while (true) {
                Thread.sleep(1000);
            }
            
        } catch (Exception e) {
            System.err.println("RMI Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

