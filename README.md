# Java Client-Server Educational System

<div align="center">

![Java](https://img.shields.io/badge/Java-17+-blue.svg)
![License](https://img.shields.io/badge/License-MIT-green.svg)
![Purpose](https://img.shields.io/badge/Purpose-Educational-orange.svg)

**A multi-threaded client-server system demonstrating Java networking, GUI development, and database integration**

</div>

## ⚠️ Important Disclaimer

> **THIS SOFTWARE IS FOR EDUCATIONAL PURPOSES ONLY**
> 
> - 🚫 **Do not use on computers you don't own**
> - 🚫 **Do not use without explicit permission**
> - 🚫 **Do not use for malicious activities**
> - ✅ **Only use for learning and research**
> 
> The creators are not responsible for any misuse of this software. Use responsibly and ethically.

## 📚 Project Overview

This educational Java project demonstrates a complete client-server architecture with:

1. **ClientGUI** - A Swing-based client application with real-time messaging
2. **Server** - A multi-threaded server with connection pooling and database integration
3. **Advanced Features** - Thread management, security, and performance optimization

## 🏗️ System Architecture
┌─────────────────┐ ┌──────────────────┐ ┌─────────────────┐
│ ClientGUI │ │ Server │ │ Database │
│ (Swing GUI) │◄────────►│ (Multi-thread) │◄────────►│ (MariaDB) │
├─────────────────┤ TCP ├──────────────────┤ JDBC ├─────────────────┤
│ • Real-time UI │ Socket │ • Thread Pool │ │ • User data │
│ • Connection │ │ • Connection │ │ • Application │
│ Management │ │ Pooling │ │ data │
│ • Error Handling│ │ • Security │ └─────────────────┘
└─────────────────┘ │ • Command │
│ Processing │
└──────────────────┘
## 📁 Project Structure
java-client-server/
│
├── src/
│ ├── ClientGUI.java # Swing client application
│ ├── Server.java # Multi-threaded server
│ ├── ClientHandler.java # Client connection handler
│ ├── ConnectionPool.java # Database connection pool
│ └── ServerConfig.java # Configuration management
│
├── config/
│ └── server.properties # Server configuration
│
├── lib/ # Database drivers
│ └── mariadb-java-client-3.0.6.jar
│
└── README.md


## 🚀 Quick Start

### Prerequisites
- **Java 17 or higher**
- **MariaDB/MySQL database**
- **Network connectivity**

### Database Setup
```sql
CREATE DATABASE mydb;
USE mydb;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO users (username, email) VALUES ('testuser', 'test@example.com');
Installation & Execution
Compile the Java classes:


javac -cp .:lib/mariadb-java-client-3.0.6.jar src/*.java
Configure the server (optional):


# Create server.properties in config/ directory
echo "db.url=jdbc:mariadb://localhost:3306/mydb" > config/server.properties
echo "db.username=your_username" >> config/server.properties
echo "db.password=your_password" >> config/server.properties
Start the server:


java -cp .:lib/mariadb-java-client-3.0.6.jar Server
Start the client:


java -cp .:lib/mariadb-java-client-3.0.6.jar ClientGUI
