# SmartFan: IoT Temperature Controlled Stand Fan

## Overview
SmartFan is an IoT-based Android application that allows users to monitor and control a stand fan equipped with an ESP32 microcontroller and temperature sensor. The system features real-time temperature monitoring, animated gauge visualization, manual and automatic fan speed control, device linking, user authentication, and customizable settings—all powered by Firebase.

---

## Features

### 1. **User Authentication**
- Secure login and registration using Firebase Authentication (email/password).
- Only authenticated users can access the dashboard and device controls.

### 2. **Device Linking**
- Users can link their ESP32-powered fan by entering or scanning a Device ID.
- Each device is associated with a user account in Firebase.
- Prevents unauthorized access to devices.

### 3. **Dashboard**
- **Animated Temperature Gauge:** Real-time temperature display using SpeedView for a modern, intuitive UI.
- **Fan Status:** Shows current fan speed and mode (auto/manual).
- **Manual Control:** Users can set fan speed via a slider when in manual mode.
- **Auto Mode:** Toggle to let the system automatically adjust fan speed based on temperature thresholds.

### 4. **Settings**
- Light and dark mode toggle for personalized appearance.
- Set temperature thresholds for auto mode.
- Manage linked devices (rename, unlink).
- Account management (logout, change password).

### 5. **History & Logs** (Planned)
- View historical temperature and fan speed logs.
- Visualize data with charts.
- Delete logs as needed.

### 6. **Notifications** (Planned)
- Push notifications for temperature threshold alerts and device status changes.

### 7. **Security**
- Firebase security rules restrict data access to authenticated users and device owners.
- Input validation throughout the app.

---

## System Architecture

- **Android App:** Modern Material UI, Firebase integration, real-time updates.
- **ESP32 Device:** Reads temperature, controls fan via VFD, communicates with Firebase.
- **Firebase Backend:** Authentication, Realtime Database, Cloud Messaging (for notifications).

---

## Database Structure (Realtime Database Example)
```json
{
  "users": {
    "uid123": {
      "email": "user@email.com",
      "devices": {
        "deviceIdABC": true
      },
      "settings": {
        "theme": "dark",
        "tempMin": 25,
        "tempMax": 30
      }
    }
  },
  "devices": {
    "deviceIdABC": {
      "owner": "uid123",
      "name": "Living Room Fan",
      "current": {
        "temperature": 28.5,
        "fanSpeed": 2,
        "mode": "auto",
        "lastUpdate": 1692620000
      },
      "logs": {
        "logId1": {
          "timestamp": 1692620000,
          "temperature": 28.5,
          "fanSpeed": 2
        }
      }
    }
  }
}
```

---

## Development Progress

### ✅ Authentication Flow
- Login and registration screens with validation.
- Only authenticated users can access the dashboard.

### ✅ Device Linking
- Users must link a device before accessing the dashboard.
- Device linking UI and Firebase logic implemented.

### ✅ Dashboard
- Real-time temperature and fan status display.
- Animated gauge (SpeedView) for temperature.
- Manual and auto fan speed controls.

### ⏳ Settings
- UI and logic for theme and temperature thresholds (to be implemented).

### ⏳ History/Logs
- Planned: View and manage temperature/fan speed logs.

### ⏳ Notifications
- Planned: Push notifications for alerts.

---

## How It Works
1. **User registers/logs in.**
2. **Links their ESP32-powered fan** using Device ID.
3. **Dashboard displays real-time temperature** (animated gauge) and fan status.
4. **User can control fan manually or enable auto mode.**
5. **Settings and logs** provide further customization and insights (in progress).

---

## Libraries & Tools
- **Firebase Auth, Database, Messaging**
- **SpeedView** (animated gauge)
- **Material Components**
- **AndroidX**

---

## Future Enhancements
- Device management (rename/unlink)
- Advanced settings (thresholds, schedules)
- Data visualization (charts, history)
- Push notifications
- Multi-device support

---

## Getting Started
1. Clone the repository.
2. Open in Android Studio.
3. Add your `google-services.json` to the `app/` directory.
4. Build and run on your device.

---

## License
This project is licensed under the Apache 2.0 License.
