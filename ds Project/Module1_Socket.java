import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * MODULE 1: Hostel Complaint Management System
 * Communication Model: Socket Programming (TCP)
 * 
 * This module demonstrates:
 * - TCP socket-based client-server communication
 * - Multi-client handling using threads
 * - Server-side state management with in-memory storage
 * - Concurrency handling for concurrent complaint submissions
 * 
 * Why Sockets?
 * - Direct, low-level communication protocol
 * - Reliable TCP connection ensures data delivery
 * - Suitable for real-time complaint submission
 * - Foundation for understanding network programming
 * 
 * Why In-Memory Storage?
 * - Academic project focus on distributed systems concepts
 * - No persistence requirement for demo purposes
 * - Fast access for real-time operations
 * - Simplifies architecture for educational clarity
 */

// Complaint data structure
class Complaint {
    String roomNumber;
    String category;
    String description;
    String timestamp;
    
    Complaint(String room, String cat, String desc) {
        this.roomNumber = room;
        this.category = cat;
        this.description = desc;
        this.timestamp = new Date().toString();
    }
    
    @Override
    public String toString() {
        return String.format("Room: %s | Category: %s | Time: %s\nDescription: %s", 
            roomNumber, category, timestamp, description);
    }
}

// HTTP Server to bridge Socket server with Web UI
class HTTPServer {
    private ServerSocket httpServer;
    private List<Complaint> complaints;
    private final int port = 8081;
    
    HTTPServer(List<Complaint> complaints) {
        this.complaints = complaints;
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
            
            // CORS headers
            String headers = "HTTP/1.1 200 OK\r\n" +
                           "Content-Type: application/json\r\n" +
                           "Access-Control-Allow-Origin: *\r\n" +
                           "Access-Control-Allow-Methods: GET, POST, OPTIONS\r\n" +
                           "Access-Control-Allow-Headers: Content-Type\r\n" +
                           "\r\n";
            
            if (method.equals("OPTIONS")) {
                out.print(headers);
                out.flush();
                client.close();
                return;
            }
            
            if (method.equals("POST") && path.equals("/submit")) {
                // Read request body
                StringBuilder body = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null && !line.isEmpty()) {
                    if (line.startsWith("{")) {
                        body.append(line);
                        break;
                    }
                }
                
                // Parse JSON (simple parsing)
                String json = body.toString();
                String room = extractValue(json, "room");
                String category = extractValue(json, "category");
                String description = extractValue(json, "description");
                
                synchronized (complaints) {
                    complaints.add(new Complaint(room, category, description));
                }
                
                String response = "{\"message\":\"Complaint submitted successfully!\"}";
                out.print(headers + response);
                out.flush();
                
            } else if (method.equals("GET") && path.equals("/view")) {
                synchronized (complaints) {
                    StringBuilder json = new StringBuilder("{\"complaints\":[");
                    for (int i = 0; i < complaints.size(); i++) {
                        Complaint c = complaints.get(i);
                        json.append(String.format(
                            "{\"room\":\"%s\",\"category\":\"%s\",\"description\":\"%s\",\"timestamp\":\"%s\"}",
                            c.roomNumber, c.category, c.description, c.timestamp
                        ));
                        if (i < complaints.size() - 1) json.append(",");
                    }
                    json.append("]}");
                    
                    out.print(headers + json.toString());
                    out.flush();
                }
            } else {
                out.print(headers + "{\"error\":\"Invalid request\"}");
                out.flush();
            }
            
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private String extractValue(String json, String key) {
        int start = json.indexOf("\"" + key + "\"");
        if (start == -1) return "";
        start = json.indexOf(":", start) + 1;
        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '"')) start++;
        int end = start;
        while (end < json.length() && json.charAt(end) != '"' && json.charAt(end) != ',' && json.charAt(end) != '}') end++;
        return json.substring(start, end).replace("\"", "");
    }
}

// Socket Server for direct TCP communication
class SocketServer {
    private ServerSocket serverSocket;
    private List<Complaint> complaints;
    private final int port = 9001;
    
    SocketServer(List<Complaint> complaints) {
        this.complaints = complaints;
    }
    
    void start() throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Socket Server started on port " + port);
        System.out.println("Direct TCP clients can connect to: localhost:" + port);
        
        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("New client connected: " + clientSocket.getRemoteSocketAddress());
            
            // Handle each client in a separate thread
            // This demonstrates thread-based concurrency handling
            new Thread(() -> handleClient(clientSocket)).start();
        }
    }
    
    /**
     * Handles individual client connection
     * Demonstrates:
     * - Thread-based multi-client handling
     * - Synchronized access to shared data structure
     * - Proper resource cleanup
     */
    private void handleClient(Socket clientSocket) {
        try {
            BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream())
            );
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            
            // Read complaint data
            String roomNumber = in.readLine();
            String category = in.readLine();
            String description = in.readLine();
            
            // Create and store complaint
            Complaint complaint = new Complaint(roomNumber, category, description);
            
            // Synchronized block ensures thread-safe access to shared list
            // This prevents race conditions when multiple clients submit simultaneously
            synchronized (complaints) {
                complaints.add(complaint);
                System.out.println("Complaint received: " + complaint);
            }
            
            // Send acknowledgment
            out.println("Complaint received successfully! Complaint ID: " + (complaints.size()));
            out.println("Room: " + roomNumber + " | Category: " + category);
            
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        }
    }
}

public class Module1_Socket {
    // In-memory storage for complaints
    // Using ArrayList with synchronized access for thread safety
    private static List<Complaint> complaints = Collections.synchronizedList(new ArrayList<>());
    
    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("MODULE 1: Hostel Complaint System");
        System.out.println("Communication Model: Socket Programming");
        System.out.println("==========================================");
        System.out.println();
        
        try {
            // Start HTTP bridge server for UI communication
            HTTPServer httpServer = new HTTPServer(complaints);
            new Thread(() -> {
                try {
                    httpServer.start();
                } catch (IOException e) {
                    System.err.println("HTTP Server error: " + e.getMessage());
                }
            }).start();
            
            // Start Socket server for direct TCP communication
            SocketServer socketServer = new SocketServer(complaints);
            socketServer.start();
            
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

