📚 Digital Library System – Java Swing

A lightweight desktop-based Digital Library System built using Java Swing, Object-Oriented Programming, and serialized file storage (.data files).
This project provides separate panels for Admins and Students, enabling complete automation of core library operations such as book management, borrowing, returning, and viewing transaction history.

🚀 Features
🔐 Authentication

Login system with role-based access

Register new Admin or Student accounts

🛠 Admin Features

Add, Edit, and Delete books

View all registered users

Delete user accounts (except admin)

View complete borrow/return history

Refresh and save all records

🎓 Student Features

Browse and search all books

Borrow available books

Return previously borrowed books

View personal borrow history

Logout securely

💾 Persistent Data Storage

All data is stored locally in serialized .data files:

books.data

users.data

history.data

This ensures offline functionality without requiring a database server.

🧱 Tech Stack
Component	Technology
Programming Language	Java (JDK 8+)
GUI Framework	Java Swing
Storage	Java Serialization (.data files)
Architecture	OOP + MVC-inspired modular design
📂 Project Structure
/src
│
├── models
│   ├── Book.java
│   ├── User.java
│   ├── Student.java
│   ├── Admin.java
│   └── Transaction.java
│
├── core
│   └── LibraryDatabase.java
│
├── ui
│   ├── LoginFrame.java
│   ├── RegisterDialog.java
│   ├── AdminPanel.java
│   ├── StudentPanel.java
│   ├── HistoryFrame.java
│   └── BackgroundPanel.java
│
└── main
    └── Library3.java   (Application entry point)



📦 Installation & Setup
1. Clone the Repository
git clone https://github.com/your-username/Digital-Library-System.git
cd Digital-Library-System

2. Compile the Project
javac *.java

3. Run the Application
java Library3

🧪 Testing

The system has been tested for:

✔ Authentication
✔ Book CRUD operations
✔ Borrow/Return workflow
✔ Data persistence after restart
✔ Error handling for invalid inputs

All tests passed successfully for small to medium datasets.

🛠 Future Improvements

Database integration (MySQL/SQLite)

Password encryption (SHA-256/BCrypt)

Multi-user network support

Barcode scanner support

Automatic backup system

🤝 Contribution

Contributions are welcome!
Feel free to fork this repository and submit pull requests.

📄 License

This project is open-source and available under the MIT License.
