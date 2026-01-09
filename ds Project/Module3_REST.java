import java.io.*;
import java.net.*;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * MODULE 3: Hostel Notice Board System
 * Communication Model: REST API (RPC - Remote Procedure Call)
 * 
 * This module demonstrates:
 * - RESTful API design principles
 * - Stateless client-server communication
 * - HTTP methods (GET, POST) for different operations
 * - RPC-style remote procedure invocation over HTTP
 * - Resource-based URL design
 * 
 * Why REST/RPC?
 * - Stateless communication (each request is independent)
 * - Standard HTTP protocol (widely supported)
 * - Resource-oriented architecture
 * - Suitable for web-based applications
 * - Demonstrates modern API design patterns
 * 
 * Why In-Memory Storage?
 * - Focus on REST/RPC concepts
 * - Stateless design doesn't require persistence
 * - Fast response times for demo
 * - Academic project requirement
 */

// Notice data structure
class Notice {
    String title;
    String message;
    String date;
    int id;
    
    Notice(int id, String title, String message) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
    
    String toJSON() {
        return String.format(
            "{\"id\":%d,\"title\":\"%s\",\"message\":\"%s\",\"date\":\"%s\"}",
            id, escapeJson(title), escapeJson(message), date
        );
    }
    
    private String escapeJson(String str) {
        return str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}

// REST API Server
class RESTServer {
    private ServerSocket serverSocket;
    private List<Notice> notices;
    private int noticeIdCounter;
    private final int port = 8083;
    
    RESTServer() {
        notices = Collections.synchronizedList(new ArrayList<>());
        noticeIdCounter = 1;
        
        // Add sample notices
        addSampleNotices();
    }
    
    private void addSampleNotices() {
        notices.add(new Notice(noticeIdCounter++, 
            "Hostel Maintenance", 
            "Scheduled maintenance on Block A from 10 AM to 2 PM tomorrow."));
        notices.add(new Notice(noticeIdCounter++, 
            "Mess Timings", 
            "Mess will remain closed on Sunday for deep cleaning."));
    }
    
    void start() throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("==========================================");
        System.out.println("MODULE 3: Hostel Notice Board System");
        System.out.println("Communication Model: REST API (RPC)");
        System.out.println("==========================================");
        System.out.println();
        System.out.println("REST Server started on port " + port);
        System.out.println("UI can access at: http://localhost:" + port);
        System.out.println();
        System.out.println("Available endpoints:");
        System.out.println("  POST /notices - Add a new notice");
        System.out.println("  GET  /notices - Fetch all notices");
        System.out.println();
        
        while (true) {
            Socket client = serverSocket.accept();
            new Thread(() -> handleRequest(client)).start();
        }
    }
    
    /**
     * Handles HTTP requests
     * Demonstrates:
     * - RESTful API design
     * - Stateless request handling
     * - HTTP method routing
     */
    private void handleRequest(Socket client) {
        try {
            BufferedReader in = new BufferedReader(
                new InputStreamReader(client.getInputStream())
            );
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            
            // Read HTTP request line
            String requestLine = in.readLine();
            if (requestLine == null) {
                client.close();
                return;
            }
            
            String[] parts = requestLine.split(" ");
            String method = parts[0];
            String path = parts[1];
            
            // Read headers to get content length for POST
            int contentLength = 0;
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                if (line.toLowerCase().startsWith("content-length:")) {
                    contentLength = Integer.parseInt(line.substring(15).trim());
                }
            }
            
            // CORS headers
            String corsHeaders = "Access-Control-Allow-Origin: *\r\n" +
                               "Access-Control-Allow-Methods: GET, POST, OPTIONS\r\n" +
                               "Access-Control-Allow-Headers: Content-Type\r\n";
            
            // Handle OPTIONS (CORS preflight)
            if (method.equals("OPTIONS")) {
                out.print("HTTP/1.1 200 OK\r\n" + corsHeaders + "\r\n");
                out.flush();
                client.close();
                return;
            }
            
            // Read request body for POST
            String requestBody = "";
            if (method.equals("POST") && contentLength > 0) {
                char[] buffer = new char[contentLength];
                in.read(buffer, 0, contentLength);
                requestBody = new String(buffer);
            }
            
            String response = "";
            String statusCode = "200 OK";
            
            // Route based on HTTP method and path
            if (method.equals("POST") && path.equals("/notices")) {
                // RPC: Add notice procedure
                response = handleAddNotice(requestBody);
                System.out.println("POST /notices - Notice added");
                
            } else if (method.equals("GET") && path.equals("/notices")) {
                // RPC: Get notices procedure
                response = handleGetNotices();
                System.out.println("GET /notices - Notices fetched");
                
            } else {
                statusCode = "404 Not Found";
                response = "{\"error\":\"Endpoint not found\"}";
            }
            
            // Send HTTP response
            out.print("HTTP/1.1 " + statusCode + "\r\n");
            out.print("Content-Type: application/json\r\n");
            out.print(corsHeaders);
            out.print("Content-Length: " + response.length() + "\r\n");
            out.print("\r\n");
            out.print(response);
            out.flush();
            
            client.close();
            
        } catch (IOException e) {
            System.err.println("Error handling request: " + e.getMessage());
        }
    }
    
    /**
     * Handles POST /notices - Add new notice
     * Demonstrates RPC-style procedure call over HTTP
     */
    private String handleAddNotice(String requestBody) {
        try {
            // Simple JSON parsing
            String title = extractValue(requestBody, "title");
            String message = extractValue(requestBody, "message");
            
            if (title.isEmpty() || message.isEmpty()) {
                return "{\"success\":false,\"message\":\"Title and message are required\"}";
            }
            
            // Create and store notice (stateless operation)
            Notice notice = new Notice(noticeIdCounter++, title, message);
            synchronized (notices) {
                notices.add(notice);
            }
            
            return "{\"success\":true,\"message\":\"Notice added successfully\",\"id\":" + notice.id + "}";
            
        } catch (Exception e) {
            return "{\"success\":false,\"message\":\"Error: " + e.getMessage() + "\"}";
        }
    }
    
    /**
     * Handles GET /notices - Fetch all notices
     * Demonstrates stateless data retrieval
     */
    private String handleGetNotices() {
        synchronized (notices) {
            StringBuilder json = new StringBuilder("{\"success\":true,\"notices\":[");
            
            for (int i = 0; i < notices.size(); i++) {
                json.append(notices.get(i).toJSON());
                if (i < notices.size() - 1) json.append(",");
            }
            
            json.append("]}");
            return json.toString();
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

public class Module3_REST {
    public static void main(String[] args) {
        try {
            RESTServer server = new RESTServer();
            server.start();
        } catch (IOException e) {
            System.err.println("REST Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

