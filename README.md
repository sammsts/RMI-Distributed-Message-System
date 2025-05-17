# 📡 DistributedMessageSystemRMI

A Java RMI-based distributed messaging system implementing **Reliable Broadcast** and **Atomic Broadcast** with fault tolerance and message ordering guarantees.

---

## 🎯 Objectives

- Implement a reliable group communication protocol using Java RMI.
- Ensure **reliable delivery**, **atomic ordering**, and **fault resilience**.
- Simulate and handle different types of failures in a distributed system.

---

## 🧩 System Architecture

- **Three or more RMI nodes** communicate and deliver messages among themselves.
- Nodes can:
  - Send and receive broadcast messages.
  - Acknowledge or retransmit lost messages.
  - Maintain delivery logs and detect failures.

---

## ✅ Features Implemented

### ✔️ Reliable Broadcast
- **Validity**: If a correct process sends a message, all correct processes eventually deliver it.
- **Agreement**: If a correct process delivers a message, all others do too.
- **Integrity**: No message is delivered more than once or without being sent.

### ✔️ Atomic Broadcast
- Messages are delivered **in the same order across all nodes** using timestamps (logical clocks).

### ✔️ Failure Handling
| Failure Type         | Detection Strategy              | Recovery Mechanism                  |
|----------------------|---------------------------------|-------------------------------------|
| Crash (Colapso)      | Unresponsive node               | Retransmit, continue with others    |
| Omission             | No ACK received                 | Timeout + resend message            |
| Timing (Temporização)| Late delivery or out of sync    | Logical clock ordering, timeouts    |

### ✔️ Real-Time Logs
- Logs of all broadcasts, deliveries, ACKs, NACKs, and recovery actions are printed to the console.

---

## 📂 Folder Structure
DistributedMessageSystemRMI/
├── shared/ → RMI shared interface
├── servidor/ → Node implementation (NodoImpl.java)
├── cliente/ → CLI-based controller to send messages

## 🚀 How to Run

### 1. Compile all files
```bash
javac shared/*.java servidor/*.java cliente/*.java
```

### 2. Start the RMI registry
```bash
rmiregistry
```
Keep this terminal open.

### 3. Launch nodes in separate terminals
```bash
# Terminal 1
java servidor.NodoImpl Nodo1 Nodo2,Nodo3

# Terminal 2
java servidor.NodoImpl Nodo2 Nodo1,Nodo3

# Terminal 3
java servidor.NodoImpl Nodo3 Nodo1,Nodo2
```

### 4. Run the controller client
```bash
java cliente.Controlador
```

## 🛠 Technologies
- Java 8+

- Java RMI

- Multithreading and concurrent collections

- Timeout and retry logic

- Simulated failure injection (manual)

## 📌 Notes
Works in a local environment (localhost); to test in distributed machines, adjust RMI URLs.
For academic and prototype purposes only; not production-ready.