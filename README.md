
# 📚 ThinkSync – Collaborative Summary Sharing App

**ThinkSync** is a mobile app designed to help students upload, share, and discover academic summaries with ease. The app aims to improve the way students study by enabling access to high-quality, peer-generated notes, anytime and anywhere.

---

## 🚀 Key Features

- ✅ Upload PDF summaries with metadata (course name, lecturer, year)
- 🔍 Filter summaries by course, lecturer, or academic year
- ❤️ Save favorite summaries for later reading
- 🗨️ Leave reviews and receive responses from uploaders
- 🔔 Receive personalized notifications for relevant updates
- 👤 User profile with uploaded and saved summaries
- 📅 Display upload dates and review timestamps for better tracking

---

## 🧪 Getting Started

To run the project locally on Android Studio:

1. **Clone the repository**:
   ```bash
   git clone https://github.com/RotemLer/FinalThinkSync.git
   ```

2. **Open the project** in **Android Studio**.

3. **Connect Firebase** to your app via Tools > Firebase or using Firebase CLI.

4. **Build and run** on an emulator or physical Android device.

5. **Ensure internet connection** is available for Firebase to function properly.

---

## 📱 Screens Overview

| Screen / Activity         | Description                                                                 | Firebase / APIs              | Libraries / Components              |
|--------------------------|-----------------------------------------------------------------------------|------------------------------|-------------------------------------|
| **LoginActivity**         | Allows users to log in with email and password. Redirects to registration. | Firebase Auth                | Material Design                     |
| **RegisterActivity**      | User registration with email, password, and profile setup.                 | Firebase Auth, Firestore     | Material Design                     |
| **MainActivity**          | Home navigation between summaries, saved items, upload and profile tabs.  | Firestore                    | BottomNavigationView                |
| **UploadSummaryActivity** | Upload PDF summaries with course, lecturer, and year.                      | Firebase Storage, Firestore  | File Picker                         |
| **SummaryListFragment**   | Displays all summaries, filterable by course/year/lecturer.                | Firestore                    | RecyclerView                        |
| **SummaryDetailsActivity**| Full summary info and PDF preview with review section.                     | Firestore                    | PDFView, Glide                      |
| **SavedSummariesFragment**| Displays saved summaries by the user.                                      | Firestore                    | RecyclerView                        |
| **ProfileActivity**       | User info and access to uploaded summaries.                                | Firestore                    | CardView                            |
| **NotificationsActivity** | Lists real-time user notifications.                                        | Firestore                    | LinearLayout                        |
| **ReviewsSection**        | Review system inside summary details.                                      | Firestore                    | RecyclerView                        |

---

## 🛠 Tech Stack Summary

- **Language**: Kotlin
- **Architecture**: Clean separation between data logic and UI components (Firebase logic handled outside of Activities)
- **UI**: Material Design, RecyclerView, BottomNavigationView, ViewBinding
- **Firebase Services**:
  - Authentication – for secure login and signup
  - Firestore – to store summaries, users, reviews, notifications
  - Storage – for uploading and retrieving PDF files
- **Libraries**:
  - PDFView – to display PDF documents
  - ViewBinding – for safe UI interactions


---

## 📸 Screenshots & Video

<img width="974" height="379" alt="image" src="https://github.com/user-attachments/assets/82ecd6a1-0d6a-489b-9377-ab6b1e85b768" />

<img width="785" height="382" alt="image" src="https://github.com/user-attachments/assets/6b919a56-753e-4f38-a7cf-f237c5182106" />

<img width="602" height="381" alt="image" src="https://github.com/user-attachments/assets/849e4664-9172-4f74-b09a-bc10abdf844a" />

🎥 [Watch the video demo](https://github.com/user-attachments/assets/90ed2950-a0d3-4db2-b5c1-66a07b824df0)

