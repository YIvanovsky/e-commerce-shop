# BuyNowDotCom 🛒

A modern, full-stack e-commerce application built with Spring Boot 3,
featuring AI-powered product search, secure authentication,
and comprehensive order management.

## 🚀 Features

### Core E-commerce Functionality
- **Product Management**: CRUD operations with categories, brands, and inventory tracking
- **Shopping Cart**: Add/remove items with real-time cart management
- **Order Processing**: Complete order lifecycle with status tracking
- **User Management**: Role-based authentication (Admin/Customer)
- **Address Management**: Multiple shipping/billing addresses per user
- **Image Upload**: Product image management with file upload support

### AI-Powered Features
- **Vector Search**: Image-based product search using ChromaDB and OpenAI embeddings
- **AI Integration**: Spring AI framework for intelligent product recommendations
- **Semantic Search**: Find products using natural language queries

### Security & Payment
- **JWT Authentication**: Secure token-based authentication with refresh tokens
- **Role-Based Access**: Admin and customer role management
- **Stripe Integration**: Ready for payment processing (requires API key)

## 🛠️ Technology Stack

### Backend
- **Spring Boot 3.5.5** - Main application framework
- **Java 21** - Latest Java LTS version
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database operations
- **MySQL** - Primary database
- **Lombok** - Code generation and boilerplate reduction

### AI & Vector Storage
- **Spring AI 1.1.0-M1** - AI integration framework
- **OpenAI API** - Embeddings and chat models
- **ChromaDB** - Vector database for semantic search
- **Docker Compose** - Container orchestration for ChromaDB

### Additional Libraries
- **ModelMapper 3.0.0** - DTO mapping
- **JWT (JJWT) 0.11.5** - Token handling
- **Stripe Java 29.5.0** - Payment processing

## 📋 Prerequisites

- Java 21 or higher
- Maven 3.6+ or the provided Maven wrapper
- MySQL 8.0+
- Docker and Docker Compose (for ChromaDB)
- OpenAI API key (for AI features)
