# Quick Start Guide

## 🚀 Fast Setup (5 Minutes)

### Step 1: Compile All Modules
```bash
javac *.java
```

### Step 2: Start All Modules (5 Terminal Windows)

**Terminal 1:**
```bash
java Module1_Socket
```

**Terminal 2:**
```bash
java Module2_RMI
```

**Terminal 3:**
```bash
java Module3_REST
```

**Terminal 4:**
```bash
java Module4_P2P
```

**Terminal 5:**
```bash
java Module5_Shared
```

### Step 3: Open UI
Open `ui/index.html` in your web browser.

### Step 4: Test Each Module
1. **Module 1:** Submit a complaint
2. **Module 2:** Get room details (try "A-101")
3. **Module 3:** Add a notice, then fetch notices
4. **Module 4:** Share a resource, then discover resources
5. **Module 5:** Submit feedback and watch counters update

---

## ⚠️ Important Notes

- Keep all 5 terminals running while testing
- Each module runs on a different port (8081-8085)
- If a port is in use, close the previous instance
- For Module 2 (RMI), ensure port 1099 is available

---

## 🎯 Demo Script

1. **Socket Programming:** Show complaint submission → Explain TCP sockets
2. **RMI:** Show room lookup → Explain remote method invocation
3. **REST:** Show notice board → Explain stateless REST API
4. **P2P:** Show resource sharing → Explain peer-to-peer architecture
5. **Shared Memory:** Show feedback counter → Explain synchronization

---

**That's it! You're ready to demo! 🎉**

