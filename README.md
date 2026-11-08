# LumaLedger
## Personal Finance Intelligence System

LumaLedger is a personal finance management system developed using Java, Spring Boot, JDBC, and MySQL. It helps users manage their financial transactions, track income and expenses, generate reports, and maintain transaction history.

## Technologies Used

- **Backend:** Java, Spring Boot
- **Database:** MySQL
- **Database Connectivity:** JDBC, JdbcTemplate
- **Build Tool:** Maven
- **API Testing:** Postman
- **Development Environment:** Visual Studio Code

## Features

### 1. Transaction Management
- Create new financial transactions.
- View transaction details.
- Update existing transactions.
- Delete transactions.
- Manage income and expense records.

### 2. CSV Import and Export
- Import transaction records from CSV files.
- Export transaction data into CSV format.
- Support bulk transaction handling.

### 3. Audit History
- Maintain a history of transaction activities.
- Record CREATE, UPDATE, and DELETE actions.
- Store transaction details and change timestamps.
- Retrieve audit logs through REST API endpoints.

### 4. Report Generation
- Generate financial reports using transaction data.
- Organize financial information for easier analysis.

### 5. Database Management
- Store transaction and audit information in MySQL.
- Perform database operations using JDBC and JdbcTemplate.

## Project Structure

The project follows a layered Spring Boot architecture:

- **Controller:** Handles REST API requests.
- **Service:** Contains business logic.
- **Repository:** Handles database operations.
- **Model:** Represents application data.
- **CSV:** Handles CSV import and export functionality.

## How to Run the Project

### Prerequisites
- Java Development Kit (JDK)
- MySQL Server
- Visual Studio Code or another Java IDE

### Steps

1. Clone or download the project.
2. Open the project folder in Visual Studio Code.
3. Create the required MySQL database.
4. Configure the database URL, username, and password in the application configuration file.
5. Open the terminal in the project directory.
6. Run the following command:

```powershell
.\mvnw.cmd spring-boot:run
```

7. The Spring Boot application will start on the configured port.

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/transactions` | Retrieve transactions |
| POST | `/api/transactions/import` | Import transactions from CSV |
| GET | `/api/transactions/export` | Export transactions to CSV |
| GET | `/api/audit-logs` | Retrieve audit history |
| GET | `/api/audit-logs/check` | Check audit log table information |

Additional transaction endpoints support creating, updating, and deleting transaction records.

## Testing

The project has been tested using Maven and Postman.

- CSV Import tested successfully.
- CSV Export tested successfully.
- Audit history tested for CREATE, UPDATE, and DELETE operations.
- Automated tests executed successfully using:

```powershell
.\mvnw.cmd clean test
```

**Build Status:** BUILD SUCCESS

## Conclusion

LumaLedger provides a structured way to manage personal financial transactions. By combining Java, Spring Boot, JDBC, and MySQL, the application supports transaction management, CSV operations, audit history, and financial reporting.
