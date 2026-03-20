# Hanap-Aral
A Cloud Based Integrated Study Group Finder

## 📌 Project Overview
**HanapAral** is a cloud-based mobile application designed to help students easily find, create, and manage study groups. The system integrates Firebase services to provide secure authentication, real-time data management, notifications, and dynamic app configuration. The goal of the application is to improve student collaboration, organization, and communication through a centralized platform.

## 🚀 Features
### 🔐 Authentication and Profile (Feature/Auth-Profile)

**Assigned to: King Amandy**

* Google Sign-In using Firebase Authentication
* Secure login system (only authenticated users can access the app)
* Student profile creation and management
* Stores user data in Firestore:

  * Name
  * Course / Program
  * Email

### 👥 Study Group Management (Feature/Study-Groups)

**Assigned to: Anthony Ambat**
* Create study groups
* Display list of available study groups
* Join study groups
* Automatic admin assignment (group creator)
* Member limit enforcement per group
* Tracks group members using Firebase Firestore

### 🔔 Notifications & Remote Config (Feature/Remoteconfig)

**Assigned to: Stephanie Aljo**
#### Firebase Cloud Messaging (FCM)
* Notifications for new members
* Announcements
* Reminders

#### Firebase Remote Config
* Enable/Disable group creation
* Update announcement headers dynamically
* Set maximum group members

#### Biometric Authentication
* Used for superuser/admin controls

## 🏗️ System Architecture
The application follows a modular and cloud-integrated architecture:
* **Frontend:** Android (Kotlin)
* **Backend Services:** Firebase
  * Firebase Authentication
  * Cloud Firestore
  * Firebase Cloud Messaging (FCM)
  * Firebase Remote Config

## 🌿 Branching Strategy

| Branch Name            | Purpose                                        |
| ---------------------- | ---------------------------------------------- |
| `main`                 | Final stable version of the project            |
| `develop`              | Integration and testing branch                 |
| `Feature/Auth-Profile` | Authentication & Profile (King Amandy)         |
| `Feature/Study-Groups` | Study Group Management (Anthony Ambat)         |
| `Feature/Remoteconfig` | Notifications & Remote Config (Stephanie Aljo) |

## 🔄 Development Workflow

1. Each member works on their assigned feature branch
2. Features are tested and merged into the `develop` branch
3. Conflicts are resolved in the `develop` branch
4. Once stable, code is merged into the `main` branch

## 🛠️ Technologies Used

* **Kotlin** – Android Development
* **Firebase Authentication** – User login system
* **Cloud Firestore** – Real-time database
* **Firebase Cloud Messaging (FCM)** – Notifications
* **Firebase Remote Config** – Dynamic configuration
* **Biometric Authentication** – Secure admin controls

## 👨‍💻 Developers

* **King Amandy** – Authentication & Profile
* **Anthony Ambat** – Study Group Management
* **Stephanie Aljo** – Notifications & Remote Config


