<div align="center">

<img src="app/src/main/ic_launcher-playstore.png" width="180"/>

# 🐄 SapiCare

### Aplikasi Manajemen Kesehatan dan Perawatan Sapi Berbasis Android

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-blueviolet?logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/Jetpack-Compose-4285F4)
![Firebase](https://img.shields.io/badge/Firebase-Enabled-orange?logo=firebase)
![License](https://img.shields.io/badge/License-Academic-green)

Dikembangkan sebagai bagian dari **Kuliah Kerja Nyata Tematik (KKNT)**  
Universitas Telkom

</div>

---

# 📖 Tentang

**SapiCare** merupakan aplikasi mobile berbasis Android yang dirancang untuk membantu digitalisasi pencatatan kesehatan dan perawatan sapi pada peternakan.

Aplikasi ini menghubungkan **Peternak**, **Pengurus/Dokter Hewan**, dan **Dinas** dalam satu sistem sehingga proses pelaporan keluhan, penjadwalan kunjungan, pencatatan riwayat pemeriksaan, hingga monitoring kesehatan sapi dapat dilakukan secara lebih efektif.

---

# ✨ Fitur Utama

## 👨‍🌾 Peternak

- Login menggunakan Google
- Melihat daftar sapi
- Menambah data sapi
- Mengubah data sapi
- Menghapus data sapi
- Mengirim keluhan kesehatan sapi
- Melihat status keluhan
- Melihat jadwal kunjungan
- Melihat riwayat keluhan
- Menerima notifikasi

---

## 👨‍⚕️ Pengurus / Dokter Hewan

- Melihat seluruh data sapi
- Melihat detail sapi
- Melihat keluhan masuk
- Menjadwalkan kunjungan
- Melakukan tindak lanjut kunjungan
- Menambahkan riwayat pemeriksaan
- Mengubah status kesehatan sapi
- Monitoring jadwal kunjungan
- Menerima notifikasi

---

## 🏢 Dinas

- Dashboard monitoring
- Monitoring seluruh data sapi
- Monitoring keluhan
- Monitoring jadwal kunjungan
- Monitoring riwayat pemeriksaan
- Persetujuan akun Pengurus/Dokter Hewan
- Melihat seluruh data peternak

---

# 📱 Fitur Sistem

- ✅ Multi Role
- ✅ Google Authentication
- ✅ Multi Account
- ✅ Approval Pengurus
- ✅ CRUD Data Sapi
- ✅ Pengelolaan Keluhan
- ✅ Penjadwalan Kunjungan
- ✅ Riwayat Pemeriksaan
- ✅ Push Notification
- ✅ Firebase Cloud Messaging
- ✅ Monitoring oleh Dinas
- ✅ Status Sinkronisasi Keluhan & Jadwal
- ✅ Offline Synchronization (Firestore)

---

# 🛠️ Teknologi

| Teknologi | Digunakan |
|------------|-----------|
| Kotlin | ✅ |
| Jetpack Compose | ✅ |
| Material Design 3 | ✅ |
| Firebase Authentication | ✅ |
| Cloud Firestore | ✅ |
| Firebase Cloud Messaging | ✅ |
| Hilt Dependency Injection | ✅ |
| Coroutines | ✅ |
| Kotlin Flow | ✅ |
| Navigation Compose | ✅ |
| DataStore | ✅ |
| Coil | ✅ |

---

# 🏗️ Arsitektur

```
Presentation
│
├── Jetpack Compose UI
│
├── ViewModel
│
├── Repository
│
├── Firebase
│   ├── Authentication
│   ├── Firestore
│   └── Cloud Messaging
│
└── Android Device
```

---

# 👥 Role Pengguna

| Role | Hak Akses |
|------|-----------|
| Peternak | Mengelola data sapi milik sendiri dan mengirim keluhan |
| Pengurus | Menangani keluhan, jadwal kunjungan, serta riwayat pemeriksaan |
| Dinas | Monitoring seluruh data dan melakukan persetujuan akun |

---

# 🔔 Push Notification

Aplikasi menggunakan **Firebase Cloud Messaging (FCM)** untuk memberikan notifikasi secara real-time.

Notifikasi meliputi:

- Persetujuan akun Pengurus
- Keluhan baru
- Jadwal kunjungan
- Perubahan status keluhan

---

# 📂 Struktur Project

```
app
│
├── data
│   ├── model
│   ├── remote
│   └── repository
│
├── navigation
│
├── ui
│   ├── auth
│   ├── components
│   ├── dinas
│   ├── notification
│   ├── pengurus
│   ├── peternak
│   └── riwayat
│
└── MainActivity
```

---




# 🧪 Pengujian

Pengujian yang telah dilakukan pada aplikasi:

- ✅ User Acceptance Testing (UAT)
- ✅ Penetration Testing
- ✅ Black Box Testing
- ✅ Multi Role Testing
- ✅ Firebase Authentication Testing
- ✅ Firestore CRUD Testing
- ✅ Push Notification Testing
- ✅ APK Testing pada Perangkat Android

---

# 🔒 Keamanan

Repository ini **tidak menyertakan** credential Firebase Admin SDK maupun secret key lainnya demi menjaga keamanan sistem.

---

# 📌 Versi

Versi saat ini

```
v1.0
```

---

# 👨‍💻 Developer

**Muhammad Arkan**

Program Studi D3 Rekayasa Perangkat Lunak Aplikasi

Universitas Telkom

---

# 🙏 Acknowledgements

Terima kasih kepada:

- Universitas Telkom
- Dinas Pangan dan Pertanian Kota Cimahi
- Sentra Susu Cipageran
- Seluruh dosen pembimbing KKNT
- Tim KKNT

---

<div align="center">


KKNT Cipageran

2026

</div>