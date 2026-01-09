import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * MODULE 4: Student Resource Sharing System
 * Communication Model: Peer-to-Peer (P2P)
 * 
 * This module demonstrates:
 * - Decentralized architecture (no central server)
 * - Each peer acts as both client and server
 * - Peer discovery mechanism
 * - Direct socket-based file/resource transfer
 * - Basic fault tolerance
 * 
 * Why P2P?
 * - No single point of failure
 * - Distributed resource sharing
 * - Scalable architecture
 * - Direct peer-to-peer communication
 * - Demonstrates decentralized systems
 * 
 * Why In-Memory Storage?
 * - Focus on P2P communication patterns
 * - Resources are shared across peers
 * - No central database needed
 * - Academic demonstration of P2P concepts
 */

// Resource data structure
class Resource {
    String name;
    String type;
    String peerAddress;
    int peerPort;
    String timestamp;
    
    Resource(String name, String type, String peerAddress, int peerPort) {
        this.name = name;
        this.type = type;
        this.peerAddress = peerAddress;
        this.peerPort = peerPort;
        this.timestamp = new Date().toString();
    }
    
    String toJSON() {
        return String.format(
            "{\"name\":\"%s\",\"type\":\"%s\",\"peer\":\"%s:%d\",\"timestamp\":\"%s\"}",
            name, type, peerAddress, peerPort, timestamp
        );
    }
}

// P2P Peer Server - Listens for incoming connections from other peers
class PeerServer extends Thread {
    private ServerSocket serverSocket;
    private List<Resource> localResources;
    private int port;
    
    PeerServer(int port, List<Resource> resources) {
        this.port = port;
        this.localResources = resources;
    }
    
    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("P2P Server listening on port " + port);
            
            while (true) {
                Socket peerSocket = serverSocket.accept();
                System.out.println("Peer connected: " + peerSocket.getRemoteSocketAddress());
                
                // Handle peer request in separate thread
                new Thread(() -> handlePeerRequest(peerSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Peer Server error: " + e.getMessage());
        }
    }
    
    /**
     * Handles requests from other peers
     * Demonstrates peer-to-peer communication
     */
    private void handlePeerRequest(Socket peerSocket) {
        try {
            BufferedReader in = new BufferedReader(
                new InputStreamReader(peerSocket.getInputStream())
            );
            PrintWriter out = new PrintWriter(peerSocket.getOutputStream(), true);
            
            String request = in.readLine();
            
            if (request != null && request.equals("DISCOVER")) {
                // Send local resources to requesting peer
                synchronized (localResources) {
                    StringBuilder response = new StringBuilder();
                    response.append("RESOURCES:");
                    for (Resource res : localResources) {
                        response.append(res.toJSON()).append("|");
                    }
                    out.println(response.toString());
                }
            } else if (request != null && request.startsWith("GET_RESOURCE:")) {
                String resourceName = request.substring(13);
                // In a real P2P system, this would transfer the actual file
                // For demo, we send resource metadata
                out.println("RESOURCE_DATA:" + resourceName);
            }
            
            peerSocket.close();
        } catch (IOException e) {
            System.err.println("Error handling peer request: " + e.getMessage());
        }
    }
}

// P2P Client - Connects to other peers to discover resources
class PeerClient {
    private List<String> knownPeers; // List of peer addresses (IP:Port)
    private List<Resource> discoveredResources;
    
    PeerClient(List<String> peers) {
        this.knownPeers = peers;
        this.discoveredResources = Collections.synchronizedList(new ArrayList<>());
    }
    
    /**
     * Discovers resources from all known peers
     * Demonstrates P2P resource discovery
     */
    void discoverResources() {
        discoveredResources.clear();
        
        for (String peer : knownPeers) {
            String[] parts = peer.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);
            
            try {
                Socket peerSocket = new Socket(host, port);
                PrintWriter out = new PrintWriter(peerSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(peerSocket.getInputStream())
                );
                
                // Send discovery request
                out.println("DISCOVER");
                
                // Receive resources
                String response = in.readLine();
                if (response != null && response.startsWith("RESOURCES:")) {
                    String resourcesStr = response.substring(10);
                    String[] resources = resourcesStr.split("\\|");
                    
                    for (String resJson : resources) {
                        if (!resJson.isEmpty()) {
                            // Parse and add to discovered resources
                            // Simplified parsing for demo
                            discoveredResources.add(parseResource(resJson, host, port));
                        }
                    }
                }
                
                peerSocket.close();
                System.out.println("Discovered resources from peer " + peer);
                
            } catch (IOException e) {
                System.out.println("Could not connect to peer " + peer + ": " + e.getMessage());
            }
        }
    }
    
    private Resource parseResource(String json, String host, int port) {
        // Simple parsing - extract name and type
        String name = extractValue(json, "name");
        String type = extractValue(json, "type");
        return new Resource(name, type, host, port);
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
    
    List<Resource> getDiscoveredResources() {
        return discoveredResources;
    }
}

// HTTP Bridge Server for Web UI
class P2PHTTPServer {
    private ServerSocket httpServer;
    private List<Resource> localResources;
    private PeerClient peerClient;
    private final int port = 8084;
    private int peerServerPort;
    
    P2PHTTPServer(List<Resource> resources, PeerClient client, int peerPort) {
        this.localResources = resources;
        this.peerClient = client;
        this.peerServerPort = peerPort;
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
                           "Access-Control-Allow-Methods: GET, POST, OPTIONS\r\n" +
                           "Access-Control-Allow-Headers: Content-Type\r\n" +
                           "\r\n";
            
            if (method.equals("OPTIONS")) {
                out.print(headers);
                out.flush();
                client.close();
                return;
            }
            
            if (method.equals("POST") && path.equals("/share")) {
                // Read request body
                StringBuilder body = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null && !line.isEmpty()) {
                    if (line.startsWith("{")) {
                        body.append(line);
                        break;
                    }
                }
                
                String json = body.toString();
                String name = extractValue(json, "name");
                String type = extractValue(json, "type");
                
                // Add to local resources
                Resource resource = new Resource(name, type, "localhost", peerServerPort);
                synchronized (localResources) {
                    localResources.add(resource);
                }
                
                String response = "{\"message\":\"Resource shared successfully! Other peers can now discover it.\"}";
                out.print(headers + response);
                out.flush();
                
            } else if (method.equals("GET") && path.equals("/discover")) {
                // Discover resources from other peers
                peerClient.discoverResources();
                
                // Combine local and discovered resources
                List<Resource> allResources = new ArrayList<>();
                synchronized (localResources) {
                    allResources.addAll(localResources);
                }
                allResources.addAll(peerClient.getDiscoveredResources());
                
                StringBuilder json = new StringBuilder("{\"resources\":[");
                for (int i = 0; i < allResources.size(); i++) {
                    json.append(allResources.get(i).toJSON());
                    if (i < allResources.size() - 1) json.append(",");
                }
                json.append("]}");
                
                out.print(headers + json.toString());
                out.flush();
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

public class Module4_P2P {
    // In-memory storage for local resources
    private static List<Resource> localResources = Collections.synchronizedList(new ArrayList<>());
    
    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("MODULE 4: Student Resource Sharing");
        System.out.println("Communication Model: Peer-to-Peer (P2P)");
        System.out.println("==========================================");
        System.out.println();
        
        // Define known peers (in a real system, this would be dynamic)
        // For demo, we use localhost with different ports to simulate multiple peers
        List<String> knownPeers = new ArrayList<>();
        knownPeers.add("localhost:9004"); // This peer
        knownPeers.add("localhost:9005"); // Simulated peer 2
        knownPeers.add("localhost:9006"); // Simulated peer 3
        
        int peerServerPort = 9004;
        
        try {
            // Start P2P server (listens for other peers)
            PeerServer peerServer = new PeerServer(peerServerPort, localResources);
            peerServer.start();
            System.out.println("This peer is listening on port " + peerServerPort);
            System.out.println("Other peers can connect to discover resources");
            System.out.println();
            
            // Create P2P client (connects to other peers)
            PeerClient peerClient = new PeerClient(knownPeers);
            System.out.println("Known peers: " + knownPeers);
            System.out.println();
            
            // Start HTTP bridge server for UI
            P2PHTTPServer httpServer = new P2PHTTPServer(localResources, peerClient, peerServerPort);
            new Thread(() -> {
                try {
                    httpServer.start();
                } catch (IOException e) {
                    System.err.println("HTTP Server error: " + e.getMessage());
                }
            }).start();
            
            System.out.println("P2P System is running. Press Ctrl+C to stop.");
            System.out.println("Note: For full P2P demo, run multiple instances on different ports");
            System.out.println();
            
            // Keep running
            while (true) {
                Thread.sleep(1000);
            }
            
        } catch (Exception e) {
            System.err.println("P2P Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

