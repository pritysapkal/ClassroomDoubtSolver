# Classroom Doubt Solver

An Android app that connects students and faculty: students post doubts, and faculty answer them, all synced in real time through Firebase.

## Features

**Students**
- Register and log in with email and password
- Post doubts with a subject, priority (low / medium / high) and an optional image attachment
- Post anonymously or privately
- Upvote doubts and follow their status (pending → answered → resolved)
- Chat one-to-one with faculty
- Get push notifications when a doubt is answered

**Faculty**
- A separate faculty registration and login
- A dashboard with pending, answered and total doubt counts
- Answer doubts and update their status
- Chat with students

**Shared**
- A profile screen with password change
- An in-app notification feed

## Tech stack

- **Language:** Java (JDK 17)
- **Platform:** Android, minSdk 24, targetSdk 34
- **UI:** Material Components, RecyclerView, CardView, SwipeRefreshLayout
- **Backend:** Firebase Authentication, Realtime Database, Storage and Cloud Messaging (FCM)
- **Images:** Glide

## Project structure

```
app/src/main/java/com/example/classroomdoubt/
├── activities/   # Screens: splash, role selection, login/register, dashboards, doubt, chat, profile
├── adapters/     # RecyclerView adapters for doubts, answers, chat and notifications
├── models/       # Doubt, Answer, ChatMessage, Notification, User
└── utils/        # FirebaseHelper, SessionManager, FCM service, TimeUtil
```

## Getting started

1. Clone the repository:
   ```bash
   git clone https://github.com/pritysapkal/ClassroomDoubtSolver.git
   ```
2. Open the project in Android Studio.
3. Create a Firebase project and add an Android app with the package name `com.example.classroomdoubt`.
4. Download `google-services.json` from Firebase and place it in the `app/` folder. It isn't in this repository.
5. In the Firebase console, enable **Email/Password** authentication, **Realtime Database**, **Storage** and **Cloud Messaging**.
6. Sync Gradle and run the app on an emulator or device (Android 7.0+).

## Author

**Prity Sapkal** — [@pritysapkal](https://github.com/pritysapkal)
