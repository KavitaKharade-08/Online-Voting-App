# 🗳️ Online Voting Application with Face Recognition  

This project is a secure **Online Voting System** that uses **face recognition with liveness detection** to verify voters.  
It ensures fair elections by preventing duplicate or fake votes.  

---

## 🚀 Features  
- 👤 **Voter Registration** with face image storage.  
- 📸 **Real-time Face Verification** using liveness detection (smile & eye movement).  
- 🔒 **Secure Login with OTP Verification** before voting.  
- 🗳️ **Vote Casting** stored safely in the database.  
- 📊 **Live Results Dashboard** with Pie Chart visualization of votes.  
- 📱 **Android App (Java + ML Kit)** for voter interaction.  
- 🖥️ **Backend (Flask + DeepFace)** for face matching.  

---

## 🛠️ Tech Stack  
### Frontend (Android App)  
- Java (Android Studio)  
- ML Kit (Face detection, liveness check)  
- Volley (API communication)  

### Backend (Server)  
- Python (Flask Framework)  
- DeepFace (Face Recognition & Matching)  
- MySQL (Voter & Voting Data Storage)  

### Database Tables  
- **Table_Voter** → Stores voter details (VoterID, Name, FaceImage).  
- **Table_Vote** → Stores votes with voter ID & selected party.  
- **Table_TotalVote** → Tracks voters who successfully cast their vote.  

---

## ⚙️ How It Works  
1. ✅ Voter registers with details & face image.  
2. 📩 Voter logs in with OTP verification.  
3. 📷 Camera opens → detects **liveness (smile, eyes open)**.  
4. 🔍 Captured image is sent to server → compared with stored face.  
5. 🗳️ If verified → voter casts their vote.  
6. 📊 Votes are counted & shown in a pie chart on the dashboard.  

---

## 🎯 Learning Outcomes  
- Implemented **Face Recognition with Liveness Detection**.  
- Used **Android + Python Flask backend integration**.  
- Applied **secure OTP & database validation** for voting.  
- Gained experience in **real-world secure application design**.  

---

## 🚧 Future Improvements  
- Blockchain integration for **tamper-proof voting**.  
- Multi-language support for accessibility.  
- Web-based version in addition to Android app.  

---

## 📌 Author  
👩‍💻 **Kavita Suresh Kharade**  
- 📧 [kavitakharade22@gmail.com](mailto:kavitakharade22@gmail.com)  
- 🌐 [GitHub Profile](https://github.com/KavitaKharade-08)  
- 🔗 [LinkedIn Profile](https://www.linkedin.com/in/kavita-kharade-32a02724b)  

---
