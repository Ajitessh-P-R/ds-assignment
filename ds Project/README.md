# Hostel Assist – Distributed Web Application

**Course:** 23CSE312 – Distributed Systems Lab  
**Institution:** Amrita Vishwa Vidyapeetham – Chennai Campus  
**Project Type:** Academic Project

---

## 📋 Project Overview

Hostel Assist is a comprehensive distributed web application designed to demonstrate five fundamental distributed systems communication models. The application provides practical hostel management services while showcasing different communication paradigms used in distributed computing.

### Key Features

- **Single Unified UI** - One HTML interface for all modules
- **Five Independent Modules** - Each demonstrating a different communication model
- **In-Memory Storage** - Focus on distributed systems concepts, not persistence
- **Demo-Ready** - Fully functional and ready for lab evaluation
- **Educational Focus** - Clear comments and explanations throughout

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Web UI (index.html)                       │
│              Single HTML file with 5 sections                 │
└─────────────────────────────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
        ▼                   ▼                   ▼
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│  Module 1    │   │  Module 2    │   │  Module 3    │
│   Socket     │   │     RMI      │   │    REST      │
│  Port: 8081  │   │  Port: 8082  │   │  Port: 8083  │
└──────────────┘   └──────────────┘   └──────────────┘
        │                   │                   │
        ▼                   ▼                   ▼
┌──────────────┐   ┌──────────────┐
│  Module 4    │   │  Module 5    │
│     P2P      │   │   Shared     │
│  Port: 8084  │   │  Port: 8085  │
└──────────────┘   └──────────────┘
```

### Communication Models

1. **Module 1: Socket Programming (TCP)** - Direct socket-based communication
2. **Module 2: Java RMI** - Remote Method Invocation
3. **Module 3: REST API** - Representational State Transfer / RPC
4. **Module 4: Peer-to-Peer** - Decentralized peer communication
5. **Module 5: Shared Memory** - Shared memory with synchronization

---

## 📁 Project Structure

```
HostelAssist/
│
├── ui/
│   └── index.html          ← Single UI for all modules
│
├── Module1_Socket.java     ← Socket Programming
├── Module2_RMI.java        ← Java RMI
├── Module3_REST.java       ← REST / RPC
├── Module4_P2P.java        ← Peer-to-Peer
├── Module5_Shared.java     ← Shared Memory
│
└── README.md               ← This file
```

---

## 🚀 Compilation and Execution

### Prerequisites

- **Java Development Kit (JDK) 8 or higher**
- **Web Browser** (Chrome, Firefox, Edge, etc.)
- **Terminal/Command Prompt**

### Step-by-Step Execution

#### 1. Compile All Modules

Open terminal in the project directory and compile all Java files:

```bash
javac Module1_Socket.java
javac Module2_RMI.java
javac Module3_REST.java
javac Module4_P2P.java
javac Module5_Shared.java
```

**Or compile all at once:**
```bash
javac *.java
```

#### 2. Start Module 1 (Socket Programming)

```bash
java Module1_Socket
```

**Expected Output:**
```
==========================================
MODULE 1: Hostel Complaint System
Communication Model: Socket Programming
==========================================

HTTP Bridge Server started on port 8081
Socket Server started on port 9001
```

#### 3. Start Module 2 (Java RMI)

**In a NEW terminal window:**
```bash
java Module2_RMI
```

**Expected Output:**
```
==========================================
MODULE 2: Room Information Service
Communication Model: Java RMI
==========================================

RMI Registry started on port 1099
RoomInfoService bound to RMI Registry
HTTP Bridge Server started on port 8082
```

#### 4. Start Module 3 (REST API)

**In a NEW terminal window:**
```bash
java Module3_REST
```

**Expected Output:**
```
==========================================
MODULE 3: Hostel Notice Board System
Communication Model: REST API (RPC)
==========================================

REST Server started on port 8083
```

#### 5. Start Module 4 (Peer-to-Peer)

**In a NEW terminal window:**
```bash
java Module4_P2P
```

**Expected Output:**
```
==========================================
MODULE 4: Student Resource Sharing
Communication Model: Peer-to-Peer (P2P)
==========================================

P2P Server listening on port 9004
HTTP Bridge Server started on port 8084
```

#### 6. Start Module 5 (Shared Memory)

**In a NEW terminal window:**
```bash
java Module5_Shared
```

**Expected Output:**
```
==========================================
MODULE 5: Mess Feedback Live Counter
Communication Model: Shared Memory
==========================================

HTTP Server started on port 8085
```

#### 7. Open the Web UI

1. Open `ui/index.html` in your web browser
2. All five modules should be accessible from the single interface
3. Test each module's functionality

---

## 📚 Module-Wise Details

### Module 1: Hostel Complaint Management System
**Communication Model:** Socket Programming (TCP)

**Features:**
- TCP socket-based client-server communication
- Multi-client handling using threads
- Complaint submission with room number, category, and description
- Thread-safe in-memory storage

**Why Sockets?**
- Direct, low-level network communication
- Reliable TCP connection ensures data delivery
- Foundation for understanding network programming
- Suitable for real-time operations

**Why In-Memory Storage?**
- Focus on socket programming concepts
- Fast access for real-time operations
- Academic project requirement
- Simplifies architecture for educational clarity

**Ports Used:**
- HTTP Bridge: `8081` (for UI)
- Socket Server: `9001` (for direct TCP clients)

---

### Module 2: Hostel Room Information Service
**Communication Model:** Java RMI (Remote Method Invocation)

**Features:**
- RMI architecture with stub-skeleton pattern
- Remote interface definition and implementation
- RMI Registry for service discovery
- Remote method invocation across JVMs

**Why RMI?**
- Object-oriented distributed computing
- Transparent remote method calls
- Built-in Java support for distributed systems
- Demonstrates stub-skeleton interaction

**Why In-Memory Storage?**
- Focus on RMI concepts rather than persistence
- Fast access for demonstration
- Academic project requirement

**Ports Used:**
- RMI Registry: `1099`
- HTTP Bridge: `8082` (for UI)

**Remote Methods:**
- `getRoomDetails(String roomNumber)` - Returns room information
- `getWardenContact()` - Returns warden contact details

---

### Module 3: Hostel Notice Board System
**Communication Model:** REST API (RPC - Remote Procedure Call)

**Features:**
- RESTful API design principles
- Stateless client-server communication
- HTTP methods (GET, POST) for different operations
- Resource-based URL design

**Why REST/RPC?**
- Stateless communication (each request is independent)
- Standard HTTP protocol (widely supported)
- Resource-oriented architecture
- Suitable for web-based applications

**Why In-Memory Storage?**
- Focus on REST/RPC concepts
- Stateless design doesn't require persistence
- Fast response times for demo

**Ports Used:**
- REST Server: `8083`

**API Endpoints:**
- `POST /notices` - Add a new notice
- `GET /notices` - Fetch all notices

---

### Module 4: Student Resource Sharing System
**Communication Model:** Peer-to-Peer (P2P)

**Features:**
- Decentralized architecture (no central server)
- Each peer acts as both client and server
- Peer discovery mechanism
- Direct socket-based resource sharing

**Why P2P?**
- No single point of failure
- Distributed resource sharing
- Scalable architecture
- Direct peer-to-peer communication

**Why In-Memory Storage?**
- Focus on P2P communication patterns
- Resources are shared across peers
- No central database needed

**Ports Used:**
- P2P Server: `9004`
- HTTP Bridge: `8084` (for UI)

**Note:** For full P2P demonstration, run multiple instances on different ports (9004, 9005, 9006).

---

### Module 5: Mess Feedback Live Counter
**Communication Model:** Shared Memory

**Features:**
- Shared memory concepts
- Race condition prevention
- Synchronization using AtomicInteger and Semaphore
- Thread-safe counter operations

**Why Shared Memory?**
- Multiple processes/threads access same data
- Demonstrates synchronization primitives
- Shows race condition handling
- Educational demonstration of concurrent programming

**Why In-Memory Storage?**
- Shared memory is inherently in-memory
- Focus on synchronization concepts
- Real-time counter updates

**Ports Used:**
- HTTP Server: `8085`

**Synchronization Techniques:**
- `AtomicInteger` - Thread-safe atomic operations
- `Semaphore` - Additional synchronization control

---

## 🎯 Demo Order Recommendation

For lab evaluation, demonstrate modules in this order:

1. **Module 1 (Socket Programming)**
   - Show complaint submission
   - Explain TCP socket communication
   - Demonstrate multi-client handling

2. **Module 2 (Java RMI)**
   - Show room information retrieval
   - Explain RMI architecture
   - Demonstrate remote method invocation

3. **Module 3 (REST API)**
   - Show notice board operations
   - Explain REST principles
   - Demonstrate stateless communication

4. **Module 4 (Peer-to-Peer)**
   - Show resource sharing
   - Explain P2P architecture
   - Demonstrate peer discovery (if multiple instances running)

5. **Module 5 (Shared Memory)**
   - Show feedback counter
   - Explain synchronization
   - Demonstrate concurrent access handling

---

## 🔧 Troubleshooting

### Port Already in Use
If you get "Address already in use" error:
- Close the previous instance of the module
- Or change the port number in the code

### RMI Registry Error
If Module 2 fails to start:
- Ensure port 1099 is not in use
- Try: `rmiregistry` in a separate terminal before running Module 2

### CORS Errors in Browser
- All modules include CORS headers
- If issues persist, ensure all modules are running

### Module Not Responding
- Check if the module is running in terminal
- Verify the correct port number
- Check browser console for errors

---

## 📝 Key Design Decisions

### Why No Database?
- **Academic Focus:** Project emphasizes distributed systems concepts, not data persistence
- **Simplicity:** In-memory storage keeps code clean and educational
- **Performance:** Fast access for real-time demonstrations
- **Requirement:** Explicit project constraint

### Why Single HTML File?
- **Unified Interface:** One UI for all modules
- **Simplicity:** Easy to deploy and demonstrate
- **Educational:** Shows how different backends can serve same frontend

### Why Independent Modules?
- **Isolation:** Each module demonstrates one communication model
- **Clarity:** No mixing of communication paradigms
- **Educational:** Clear separation of concepts
- **Requirement:** Explicit project constraint

### Why HTTP Bridge Servers?
- **Web Compatibility:** Modern browsers don't support direct socket/RMI connections
- **Practical Solution:** HTTP bridges allow web UI to communicate with all modules
- **Educational:** Still demonstrates core communication models

---

## 🎓 Learning Outcomes

After completing this project, students will understand:

1. **Socket Programming**
   - TCP/IP communication
   - Multi-threaded server design
   - Client-server architecture

2. **Java RMI**
   - Remote method invocation
   - Stub-skeleton pattern
   - Distributed object management

3. **REST/RPC**
   - RESTful API design
   - Stateless communication
   - HTTP-based remote procedure calls

4. **Peer-to-Peer Systems**
   - Decentralized architecture
   - Peer discovery mechanisms
   - Direct peer communication

5. **Shared Memory**
   - Concurrent access patterns
   - Synchronization primitives
   - Race condition prevention

---

## 📄 License

This is an academic project for educational purposes.

---

## 👨‍💻 Development Notes

- All code is original and written for educational purposes
- Code follows Java best practices
- Extensive comments explain distributed systems concepts
- Each module is self-contained and can run independently
- UI uses modern CSS with Amrita Chennai campus theme

---

## 🔗 Architecture Diagram (Text-Based)

```
┌──────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                          │
│                    (Web Browser - index.html)                 │
└──────────────────────────────────────────────────────────────┘
                              │
                              │ HTTP Requests
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
        ▼                     ▼                     ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│   MODULE 1   │    │   MODULE 2   │    │   MODULE 3   │
│   SOCKET     │    │     RMI      │    │    REST      │
│              │    │              │    │              │
│ TCP Socket   │    │ RMI Registry │    │ HTTP Server  │
│ Port: 9001   │    │ Port: 1099   │    │ Port: 8083   │
│              │    │              │    │              │
│ HTTP: 8081   │    │ HTTP: 8082   │    │              │
└──────────────┘    └──────────────┘    └──────────────┘
        │                     │                     │
        │                     │                     │
        ▼                     ▼                     ▼
┌──────────────┐    ┌──────────────┐
│   MODULE 4   │    │   MODULE 5   │
│     P2P      │    │   SHARED     │
│              │    │   MEMORY     │
│ P2P Server   │    │              │
│ Port: 9004   │    │ HTTP: 8085   │
│              │    │              │
│ HTTP: 8084   │    │ AtomicInt +  │
│              │    │ Semaphore    │
│ Peer Network │    │              │
└──────────────┘    └──────────────┘
```

---

## ✅ Project Checklist

- [x] Single HTML file for UI
- [x] Five independent Java modules
- [x] Each module uses exactly one communication model
- [x] In-memory storage (no database)
- [x] UI communicates with different ports
- [x] All modules run independently
- [x] Thread-safe operations where needed
- [x] Comprehensive comments
- [x] Demo-ready code
- [x] Complete README documentation

---

## 📞 Support

For questions or issues:
1. Check the troubleshooting section
2. Verify all modules are running
3. Check terminal output for error messages
4. Ensure correct port numbers in UI

---

**Project Status:** ✅ Complete and Demo-Ready

**Last Updated:** 2025

---

*This project is designed for academic evaluation in the Distributed Systems Lab course at Amrita Vishwa Vidyapeetham, Chennai Campus.*

