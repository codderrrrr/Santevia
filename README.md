# Santevia | AI-Powered Healthcare & Doctor Booking Platform

**Santevia** is a comprehensive Android-based healthcare solution designed to bridge the gap between patients and healthcare providers. By leveraging Artificial Intelligence and real-time communication protocols, Santevia provides a seamless experience for appointment scheduling, health monitoring, and instant medical consultation.

---

## Key Features
- **Smart Appointment Scheduling:** Real-time booking system with automated reminders and doctor availability tracking.
- **AI Health Insights:** Integrated **Gemini API** to provide users with preliminary health insights and symptom analysis.
- **Real-Time Consultation:** High-fidelity in-app voice and video calling powered by **ZegoCloud**.
- **Instant Messaging:** Secure doctor-patient chat interface for sharing reports and quick consultations.
- **Medical Dashboard:** Centralized profile for managing prescriptions, appointment history, and vital health metrics.

---

## Technical Implementation

### Mobile Architecture
- **Language & Framework:** Java (Android SDK) with XML for responsive UI layouts.
- **Real-Time Engine:** **ZegoCloud SDK** integration for low-latency video/audio streaming and signaling.
- **AI Integration:** **Gemini API** for natural language processing, enabling a sophisticated health-assistant chatbot.

### Backend & Infrastructure
- **Authentication:** Firebase Auth (Email/Google) for secure user onboarding.
- **Real-Time Database:** Firebase Realtime Database / Firestore for instant synchronization of chat messages and appointment statuses.
- **Cloud Storage:** Firebase Storage for secure handling of medical documents and user profile images.

---

## Design & UX
The application follows a **Minimalist Healthcare Aesthetic**:
- **Clean UI:** High readability with a focus on accessibility for all age groups.
- **Intuitive Navigation:** Card-based layout for quick access to "Book Now" and "Emergency" features.
- **Glassmorphism Elements:** Modern UI components designed originally in Figma to ensure a premium look and feel.

---

## Installation & Setup

1. **Clone the Repository**
   ```bash
   git clone [https://github.com/codderrrrr/santevia.git](https://github.com/codderrrrr/santevia.git)
   cd santevia
