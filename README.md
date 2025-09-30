# Educational Keylogger & Client-Server System

<div align="center">

![Java](https://img.shields.io/badge/Java-17+-blue.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)
![Purpose](https://img.shields.io/badge/Purpose-Educational-orange.svg)

**A comprehensive educational suite demonstrating networking, GUI development, and system programming**

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

This educational suite includes three interconnected components that demonstrate real-world software development concepts:

1. **Educational Keylogger** (Python) - System programming and event handling
2. **Client GUI Application** (Java) - Swing GUI and network communication
3. **Multi-threaded Server** (Java) - Server architecture and database integration

## 🛠️ System Architecture
┌─────────────────┐ ┌──────────────────┐ ┌─────────────────┐
│ Keylogger │ │ Client GUI │ │ Java Server │
│ (Python) │ │ (Java Swing) │ │ (Multi-thread) │
├─────────────────┤ ├──────────────────┤ ├─────────────────┤
│ • Event capture │ │ • Real-time chat │ │ • Thread pool │
│ • File logging │◄───│ • Connection mgmt│◄──►│ • Connection │
│ • Analytics │ │ • Error handling │ │ pooling │
└─────────────────┘ └──────────────────┘ └─────────┬───────┘
│
▼
┌─────────────────┐
│ Database │
│ (MariaDB) │
└─────────────────┘


## 📁 Project Structure
educational-suite/
├── java/
│ ├── ClientGUI.java
│ ├── Server.java
│ └── server.properties
│
├── docs/
│ └── ARCHITECTURE.md
│
└── README.md


## 🚀 Quick Start Guide

### Prerequisites
- **Java 17+** JDK
- **MariaDB/MySQL** database
- **Network connectivity** for client-server communication

### Installation
-- Create database and table for server
CREATE DATABASE mydb;
USE mydb;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
