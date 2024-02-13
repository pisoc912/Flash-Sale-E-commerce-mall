# Flash-Sale-E-commerce-mall
FlashSale is a high-performance, scalable, and resilient online shopping system designed to handle high concurrency and large traffic flows, especially tailored for events like Black Friday. Built with Spring Boot, this system integrates a robust tech stack to ensure reliability, speed, and security for flash sale scenarios.

## Features

- **Spring Boot Framework**: Utilizes Spring Boot for rapid development and deployment.
- **MyBatis Persistence Layer**: Leverages MyBatis for efficient database operations and interactions.
- **Redis Caching**: Implements Redis to reduce database load by caching frequently accessed data, significantly speeding up response times.
- **RocketMQ Messaging**: Employs RocketMQ for reliable messaging, ensuring data consistency and asynchronous communication between microservices.
- **Snowflake Algorithm**: Utilizes the Snowflake algorithm for generating distributed unique IDs, crucial for order processing and tracking.
- **MySQL Database**: Uses MySQL as the primary data store, providing a robust and reliable database management system.
- **Sentinel for Traffic Control**: Integrates Sentinel for real-time traffic control, ensuring system stability during peak times.
- **JMeter for Load Testing**: Tested with JMeter to simulate high traffic conditions and ensure system performance under load.

## Getting Started

### Prerequisites
- Java JDK 1.8 or later
- MySQL 5.7 or later
- Redis 5.0 or later
- RocketMQ 4.8.0 or later
- Sentinel 1.8.0 or later
- Apache JMeter 5.3 for load testing
