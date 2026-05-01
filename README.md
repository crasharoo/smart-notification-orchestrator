# Smart Notification Orchestrator

## 🚀 Overview

A **multi-threaded notification orchestration system** built using Java 21 and Spring Boot, designed to handle high-throughput asynchronous workloads with intelligent scheduling, batching, and rate limiting.

The system ensures reliable and efficient notification delivery by combining concurrency, prioritization, and fault-tolerant processing.

---

## 🧱 High-Level Architecture

```
Client Request → Notification Service → Queue → Worker Pool
                                         ↓
                              Scheduler + Rate Limiter
                                         ↓
                                Delivery Handler
```

---

## 🔥 Key Features

### 1. Multi-Threaded Processing

* Worker pool processes notifications in parallel
* Improves throughput and reduces latency

### 2. Priority-Based Scheduling

* Supports **HIGH, MEDIUM, LOW** priority notifications
* Ensures urgent messages are delivered first

### 3. DND (Do Not Disturb) Handling

* Respects user preferences
* Automatically delays notifications during DND window

### 4. Retry Mechanism

* Handles transient failures
* Uses retry with backoff for reliable delivery

### 5. Rate Limiting (Token Bucket)

* Controls per-user notification rate
* Prevents system overload and spam

### 6. Batch Processing

* Aggregates low-priority notifications
* Reduces redundant delivery operations

---

## ✔️ Core Concepts Used

* Multithreading & Concurrency
* Thread Pools
* Scheduling & Delayed Execution
* Rate Limiting (Token Bucket Algorithm)
* Queue-based Processing
* Fault Tolerance (Retry Handling)

---

## ⚙️ Tech Stack

* **Java 21**
* **Spring Boot**
* **Maven**
* **Concurrent Data Structures**

---

## ▶️ How to Run

### 1. Clone the repository

```bash
git clone https://github.com/<your-username>/smart-notification-orchestrator.git
cd smart-notification-orchestrator
```

### 2. Build the project

```bash
mvn clean install
```

### 3. Run the application

```bash
mvn spring-boot:run
```

---

## Sample API

### Create Notification

**POST /notifications**

```json
{
  "userId": "user1",
  "message": "Hello from system",
  "channel": "EMAIL",
  "priority": "HIGH"
}
```

---

## 🔄 Flow

1. Notification request received
2. Added to processing queue
3. Worker picks notification
4. Applies:

   * DND logic
   * Priority scheduling
   * Rate limiting
5. Notification delivered or retried

---

## 📈 Future Improvements

* Add persistent queue (Kafka/RabbitMQ)
* Distributed rate limiting (Redis)
* Notification analytics dashboard
* Multi-channel delivery (Email/SMS/Push)

---

## 👨‍💻 Author
@crasharoo (Ashutosh K)
