# Redis-like In-Memory Store in Java

This project is a simple Redis-like in-memory key-value store implemented in Java. This project is made to understand how redis works, networking, concurrency and persistence(not implementation yet).It supports basic Redis commands such as `SET`, `GET`, `INCR`, `LPUSH`, `RPUSH`, `LPOP`, `RPOP`, `LLEN`, and `LRANGE`. The project uses Java NIO for non-blocking I/O operations, making it suitable for handling multiple client connections concurrently.

## Features
- **In-Memory Storage**: Stores key-value pairs in memory with optional expiration.
- **Basic Redis Commands**: Supports a subset of Redis commands for string and list operations.
- **Non-Blocking I/O**: Uses Java NIO for handling multiple client connections efficiently.
- **Expiration**: Supports setting expiration time for keys.
- **List Operations**: Supports operations on lists such as `LPUSH`, `RPUSH`, `LPOP`, `RPOP`, `LLEN`, and `LRANGE`.

## Prerequisites

- Java Development Kit (JDK) 21
- Maven (for building the project)

## Getting Started

### Clone the Repository

```sh
git clone https://github.com/your-username/your-repo-name.git
cd your-repo-name
```

### Build the Project
```sh
mvn clean install
```

### Run the Server
```sh
java -jar target/redis-java.jar
```

The server will start on port 6379 by default.
