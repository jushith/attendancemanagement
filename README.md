# Attendance Management System

An Android application designed to streamline attendance management for **Admins**, **Faculty**, and **Students**. This app utilizes **Firebase Authentication** for secure login and **Firestore** for real-time database management. The goal is to provide a seamless and efficient user experience with features such as role-based dashboards, attendance tracking, and low-attendance monitoring.

## Features

### **Role-Based Dashboards**
- **Admin**:  
  - Add Faculty, Students, Classes, and Courses.  
  - Manage and monitor data through a centralized dashboard.  
  - Secure role-based authentication and navigation.  
- **Faculty**:  
  - Mark attendance for assigned classes and courses.  
  - Edit attendance records for specific dates and classes.  
  - View low attendance details of students.  
- **Students**:  
  - Check attendance percentage and view attended dates.  
  - Track attendance **course-wise** and **class-wise**.

### **Firebase Integration**
- **Authentication**:  
  - Secure login/logout functionality for Admin, Faculty, and Students.  
- **Firestore Database**:  
  - Real-time database for storing user roles, attendance records, student data, and course details.  
- **Offline Caching**:  
  - Enhanced performance with faster data retrieval even offline.

### **Core Functionalities**
- **Class and Course Management**:  
  - Add and manage Classes, Courses, Students, and Faculty.  
- **Attendance Tracking**:  
  - Mark students as present or absent.  
  - Edit attendance for specific dates.  
  - Automatic attendance percentage calculation.  
- **Low Attendance Tracker**:  
  - Identify students with attendance below 75%.  
  - Dynamically calculates data using Firestore collections.

---

## Tech Stack
- **Frontend**: Android (Java)
- **Backend**: Firebase Authentication, Firestore
- **Database**: Firestore with offline caching enabled

---


## Usage
1. **Admin Login**: Add Faculty, Students, Classes, and Courses via the dashboard.  
2. **Faculty Login**: Mark attendance, edit records, and track low-attendance students.  
3. **Student Login**: View attendance statistics and track attendance details.

