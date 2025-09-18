
# Rentello - Car Rental Web Application

Rentello is a modern web application designed for seamless car rental operations. Built with a Java Spring Boot backend, Microsoft SQL Server database, and a React frontend, Rentello offers a complete solution for customers, employees, and administrators in the car rental industry.

## Table of Contents

* [Features](#features)
* [Tech Stack](#tech-stack)
* [Getting Started](#getting-started)
* [Project Structure](#project-structure)
* [Screenshots](#screenshots)
* [Contributing](#contributing)
* [License](#license)
* [Contact](#contact)

---

## Features

* **User Authentication:** Secure registration, login, and role-based access (Admin, Employee, Customer)
* **Car Management:** Add, update, and manage car inventory with availability tracking
* **Reservation System:** Real-time booking, modification, and cancellation of reservations
* **Customer Profile:** View and manage personal bookings, profile details, and rental history
* **Admin Dashboard:** Oversight of all rentals, users, cars, and business analytics
* **Notifications:** Email and in-app notifications for bookings and updates
* **Reporting:** Exportable reports for revenue, car usage, and customer activity
* **Responsive Design:** Fully responsive user interface for desktop and mobile

---

## Tech Stack

**Backend:**

* Java 17+
* Spring Boot
* Spring Data JPA (Hibernate)
* Microsoft SQL Server (MSSQL)
* Spring Security
* JWT Authentication

**Frontend:**

* React (with Hooks)
* Redux (optional, if state management used)
* Axios (API requests)
* Styled Components / Tailwind CSS / CSS Modules

**Other:**

* Docker (optional, for containerization)
* Git & GitHub (version control)

---

## Getting Started

### Prerequisites

* Java 17 or higher
* Node.js (v18+ recommended)
* npm or yarn
* Microsoft SQL Server (local or cloud instance)

### Backend Setup

1. **Clone the repository:**

   ```bash
   git clone https://github.com/your-username/rentello.git
   cd rentello/backend
   ```

2. **Configure the database connection:**

   * Update `src/main/resources/application.properties`:

     ```
     spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=rentello
     spring.datasource.username=your_username
     spring.datasource.password=your_password
     spring.jpa.hibernate.ddl-auto=update
     ```

3. **Build & run:**

   ```bash
   ./mvnw spring-boot:run
   ```

### Frontend Setup

1. **Navigate to the frontend:**

   ```bash
   cd ../frontend
   ```

2. **Install dependencies:**

   ```bash
   npm install
   # or
   yarn install
   ```

3. **Start the React app:**

   ```bash
   npm start
   # or
   yarn start
   ```

---

## Project Structure

```
rentello/
  ├─ backend/           # Spring Boot backend (Java)
  │   ├─ src/
  │   └─ ...
  └─ frontend/          # React frontend (JS/TS)
      ├─ src/
      └─ ...
```

---

## Screenshots

![Ekran görüntüsü 2025-06-16 220215](https://github.com/user-attachments/assets/cde2420f-be5a-44f5-9ba1-89229ad39ca1)
![Ekran görüntüsü 2025-06-16 220240](https://github.com/user-attachments/assets/db357726-38ac-4cf9-8c64-64258dbfc328)
![Ekran görüntüsü 2025-06-16 220304](https://github.com/user-attachments/assets/fbaf29d6-7346-4467-bf35-1575f7af4432)




---

## Contributing

Contributions are welcome! Please open an issue or submit a pull request.

---


## Contact

For any questions, suggestions, or issues, please contact:

* Project Owner: Berke Kamişoğlu
* Email: \[[berkekamisoglu1@gmai.com](mailto:berkekamisoglu1@gmai.com)]
* GitHub: [github.com/berkekamisoglu/rentello](https://github.com/berkekamisoglu/rentello)

---

**Happy Renting!**

---
