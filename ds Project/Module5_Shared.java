import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * MODULE 5: Mess Feedback Live Counter
 * Communication Model: Shared Memory
 * 
 * This module demonstrates:
 * - Shared memory concepts
 * - Race condition prevention
 * - Synchronization techniques (AtomicInteger, Semaphore)
 * - Concurrent access to shared data
 * - Thread-safe counter operations
 * 
 * Why Shared Memory?
 * - Multiple processes/threads access same data
 * - Demonstrates synchronization primitives
 * - Shows race condition handling
 * - Educational demonstration of concurrent programming
 * 
 * Why In-Memory Storage?
 * - Shared memory is inherently in-memory
 * - Focus on synchronization concepts
 * - Real-time counter updates
 * - Academic demonstration requirement
 */

// Shared Memory Counter Manager
class SharedCounterManager {
    // Using AtomicInteger for thread-safe operations
    // AtomicInteger ensures atomic read-modify-write operations
    private AtomicInteger goodCount;
    private AtomicInteger averageCount;
    private AtomicInteger poorCount;
    
    // Semaphore for additional synchronization control
    // Limits concurrent access if needed
    private Semaphore accessSemaphore;
    
    SharedCounterManager() {
        goodCount = new AtomicInteger(0);
        averageCount = new AtomicInteger(0);
        poorCount = new AtomicInteger(0);
        accessSemaphore = new Semaphore(1, true); // Fair semaphore
    }
    
    /**
     * Increments feedback counter based on rating
     * Demonstrates:
     * - Atomic operations for thread safety
     * - Semaphore-based synchronization
     * - Race condition prevention
     */
    void incrementFeedback(String rating) {
        try {
            // Acquire semaphore to ensure exclusive access
            // This prevents race conditions when multiple threads update simultaneously
            accessSemaphore.acquire();
            
            try {
                // Atomic increment operations
                // These are thread-safe and prevent race conditions
                switch (rating.toLowerCase()) {
                    case "good":
                        goodCount.incrementAndGet();
                        System.out.println("Good feedback received. Count: " + goodCount.get());
                        break;
                    case "average":
                        averageCount.incrementAndGet();
                        System.out.println("Average feedback received. Count: " + averageCount.get());
                        break;
                    case "poor":
                        poorCount.incrementAndGet();
                        System.out.println("Poor feedback received. Count: " + poorCount.get());
                        break;
                }
            } finally {
                // Always release semaphore
                accessSemaphore.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread interrupted: " + e.getMessage());
        }
    }
    
    /**
     * Gets current counter values
     * Returns a snapshot of the shared memory state
     */
    CounterSnapshot getCounters() {
        try {
            accessSemaphore.acquire();
            try {
                return new CounterSnapshot(
                    goodCount.get(),
                    averageCount.get(),
                    poorCount.get()
                );
            } finally {
                accessSemaphore.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new CounterSnapshot(0, 0, 0);
        }
    }
    
    // Counter snapshot for safe data retrieval
    static class CounterSnapshot {
        final int good;
        final int average;
        final int poor;
        
        CounterSnapshot(int good, int average, int poor) {
            this.good = good;
            this.average = average;
            this.poor = poor;
        }
        
        String toJSON() {
            return String.format("{\"good\":%d,\"average\":%d,\"poor\":%d}", good, average, poor);
        }
    }
}

// HTTP Server for Web UI
class SharedMemoryHTTPServer {
    private ServerSocket httpServer;
    private SharedCounterManager counterManager;
    private final int port = 8085;
    
    SharedMemoryHTTPServer(SharedCounterManager manager) {
        this.counterManager = manager;
    }
    
    void start() throws IOException {
        httpServer = new ServerSocket(port);
        System.out.println("==========================================");
        System.out.println("MODULE 5: Mess Feedback Live Counter");
        System.out.println("Communication Model: Shared Memory");
        System.out.println("==========================================");
        System.out.println();
        System.out.println("HTTP Server started on port " + port);
        System.out.println("UI can access at: http://localhost:" + port);
        System.out.println();
        System.out.println("Available endpoints:");
        System.out.println("  POST /feedback - Submit feedback (good/average/poor)");
        System.out.println("  GET  /counters - Get current counter values");
        System.out.println();
        
        while (true) {
            Socket client = httpServer.accept();
            // Each request handled in separate thread
            // Multiple threads can access shared memory simultaneously
            // Synchronization ensures thread-safe operations
            new Thread(() -> handleHTTPRequest(client)).start();
        }
    }
    
    /**
     * Handles HTTP requests
     * Demonstrates:
     * - Multiple threads accessing shared memory
     * - Thread-safe counter updates
     * - Concurrent read operations
     */
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
            
            if (method.equals("POST") && path.equals("/feedback")) {
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
                String rating = extractValue(json, "rating");
                
                if (rating.isEmpty() || (!rating.equals("good") && !rating.equals("average") && !rating.equals("poor"))) {
                    String response = "{\"success\":false,\"message\":\"Invalid rating. Use: good, average, or poor\"}";
                    out.print(headers + response);
                    out.flush();
                } else {
                    // Update shared memory counter
                    // This operation is thread-safe due to synchronization
                    counterManager.incrementFeedback(rating);
                    
                    String response = "{\"success\":true,\"message\":\"Feedback recorded successfully\"}";
                    out.print(headers + response);
                    out.flush();
                }
                
            } else if (method.equals("GET") && path.equals("/counters")) {
                // Read from shared memory
                // Multiple threads can read simultaneously
                // Atomic operations ensure consistent reads
                SharedCounterManager.CounterSnapshot snapshot = counterManager.getCounters();
                String response = snapshot.toJSON();
                out.print(headers + response);
                out.flush();
                
            } else {
                out.print(headers + "{\"error\":\"Invalid request\"}");
                out.flush();
            }
            
            client.close();
        } catch (IOException e) {
            System.err.println("Error handling request: " + e.getMessage());
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

public class Module5_Shared {
    // Shared memory counter manager
    // This object is shared across all threads
    private static SharedCounterManager counterManager = new SharedCounterManager();
    
    public static void main(String[] args) {
        try {
            SharedMemoryHTTPServer server = new SharedMemoryHTTPServer(counterManager);
            server.start();
        } catch (IOException e) {
            System.err.println("Shared Memory Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

