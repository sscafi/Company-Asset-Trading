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

