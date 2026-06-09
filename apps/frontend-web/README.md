# Web Frontend Application (`frontend-web`) 🖥️

This is the central user portal of the internship and outreach management system. Built using **React and Vite**, it provides a modern, responsive user interface allowing students, tutors, and coordinators to interact with the backend services.

---

## 🚀 Key Features

* **JWT-Based Authentication**: Seamless registration and login flows, storing authorization tokens securely in local storage.
* **Microservices Dashboard**: Tabbed user interface to interact with the system modules:
  * **💼 Internship Offers**: Post new internship opportunities and view available listings.
  * **👤 Academic Profiles**: Create and display student, tutor, or coordinator profiles.
  * **🔗 Outreach Projects**: Manage university social outreach projects.
* **System Status Monitor**: Real-time diagnostic panel that performs periodic health checks on downstream services through the API Gateway.
* **Responsive Styling**: Crafted using vanilla CSS for premium design and glassmorphic card effects.

---

## 🛠️ Technology Stack

* **React (v18+)**
* **Vite** (Next-generation frontend toolchain)
* **ESLint** (Static code analysis)
* **CSS3** (Sleek layouts, modern typography, glassmorphism, and responsive grids)

---

## 📦 Main Directory Structure

```text
apps/frontend-web/
├── public/                 # Static assets
├── src/
│   ├── App.jsx             # Main client logic, state control, and endpoints calls
│   ├── App.css             # Component-wide responsive styling rules
│   ├── index.css           # Global typography and design tokens
│   └── main.jsx            # React root injection point
├── Dockerfile              # Multi-stage production Nginx container build
├── index.html              # HTML entry point template
├── vite.config.js          # Vite compilation config
└── package.json            # Front-end dependencies and run commands
```

---

## ⚙️ Environment Configurations

The application detects the target backend URL dynamically based on the browser's current address (`window.location.hostname`).

You can customize the API gateway port using environment variables or in `.env` files:

* **`VITE_GATEWAY_PORT`** (Default: `8082`): Defines the port used to contact the central API Gateway.

---

## 🚀 Development and Build Guide

### 1. Install Dependencies
```bash
npm install
```

### 2. Run Locally (Dev Server)
```bash
npm run dev
```
By default, the application runs on [http://localhost:5173](http://localhost:5173).

### 3. Build for Production
```bash
npm run build
```
This compiles optimized assets in the `/dist` directory.

---

## 🐳 Running inside Docker (Nginx server)

The `Dockerfile` performs a multi-stage build: compiles the assets using Node.js and serves them from a lightweight Nginx web server.

### Build Image
```bash
docker build \
  --build-arg VITE_GATEWAY_PORT=8082 \
  -t gdmuzo/frontend-web:latest .
```

### Run Container
```bash
docker run -d \
  --name frontend-web \
  --network pasantias-net \
  -p 80:80 \
  gdmuzo/frontend-web:latest
```
Once started, the application will be served on port `80` of your docker host.
