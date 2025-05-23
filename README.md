# BMTC Bus Route Planner – Feature Showcase

This document highlights the core features of the **BMTC Bus Route Planner** application, including screenshots and explanations for each module.

---

## 1. Registration

![Registration Screen](https://github.com/user-attachments/assets/e7de62ff-d9cc-4544-9d09-28e62ef8be04)

**Fig 1**: The user registration screen allows new users to create an account by entering a username, email, and password. Registration is handled securely using **Back4App authentication**.

---

## 2. Login Screen

![Login Screen](https://github.com/user-attachments/assets/81ce3e1c-7070-4297-b1e1-d46d366fc6dc)

**Fig 2**: This screen confirms successful user login. The system validates the credentials via **Back4App**, and displays a “Login Successful!” message upon successful authentication.

---

## 3. Finding Bus

![Finding Bus](https://github.com/user-attachments/assets/a4ad317c-f9fa-45b2-a44e-2467b756d054)

**Fig 3**: The bus search feature enables users to select a **source** and **destination**. Upon clicking "Find Bus", the app queries BMTC route data and displays a list of available buses, fares, and estimated travel times.

---

## 4. Stop Announcements

![Stop Announcements](https://github.com/user-attachments/assets/020818b9-2c57-4bac-9b94-73834cf84ff2)

**Fig 4**: The **Automated Stop Announcement Module** actively tracks the user's location. It announces upcoming stops both **visually and via audio**, enhancing accessibility for all users, especially those who are visually impaired.

---

## 5. User Database

![User Database](https://github.com/user-attachments/assets/759b02ea-e109-4d88-bb92-e1d8bf07b21e)

**Fig 5**: This shows the **Back4App database** schema for managing user accounts. It securely stores the username, email, password (hashed), and user role. Admin accounts are created manually, while regular users register and log in via the app.
