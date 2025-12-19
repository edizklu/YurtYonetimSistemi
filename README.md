# Yurt Otomasyon Sistemi (Dormitory Automation System)

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-17-FF0000?style=for-the-badge&logo=java&logoColor=white)
![SQLite](https://img.shields.io/badge/SQLite-07405E?style=for-the-badge&logo=sqlite&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)

Bu proje, üniversite yurtlarının yönetim süreçlerini dijitalleştirmek, öğrenci-yurt yönetimi arasındaki etkileşimi hızlandırmak ve veri güvenliğini sağlamak amacıyla geliştirilmiş kapsamlı bir masaüstü uygulamasıdır.

MVC (Model-View-Controller) mimarisi üzerine inşa edilmiş olup, Singleton ve Observer gibi tasarım desenlerini içermektedir.

---

## Özellikler

### Yönetici Paneli (Admin)
* **Dinamik Dashboard:** Anlık doluluk oranları (PieChart) ve izin talep istatistikleri (BarChart).
* **İnteraktif Kat Planı:** Odaların doluluk durumunu renkli kutucuklarla görselleştirme (Kırmızı: Dolu, Yeşil: Müsait).
* **Gelişmiş Öğrenci Yönetimi:**
    * Öğrenci Ekleme / Silme / Güncelleme.
    * **Akıllı Arama:** İsim, Soyisim veya TC Kimlik No ile anlık filtreleme.
    * **Güvenli Taşıma (Transaction):** Bir öğrenciyi başka odaya taşırken veri bütünlüğünü koruyan transaction yapısı.
* **Oda Yönetimi:** Yeni oda oluşturma ve kapasite kontrolü.
* **İzin Onay Sistemi:** Öğrencilerden gelen talepleri Onaylama/Reddetme.

### Öğrenci Paneli (Student)
* **Kişisel Profil:** Kişisel bilgileri görüntüleme ve Telefon Numarası Güncelleme.
* **Oda Bilgisi:** Kaldığı odayı ve oda arkadaşlarını görüntüleme.
* **İzin İşlemleri:** Yeni izin talebi oluşturma ve talep durumunu (Beklemede/Onaylandı) takip etme.

---

## Mimari ve Teknik Detaylar

Bu proje sadece çalışan bir uygulama değil, aynı zamanda temiz kod prensiplerine uygun bir mühendislik çalışmasıdır.

### 1. MVC Mimarisi
Proje, kodun okunabilirliğini ve bakımını kolaylaştırmak için 3 ana katmana ayrılmıştır:
* **Model:** Veritabanı nesneleri (Student, Room, LeaveRequest).
* **View:** FXML dosyaları (Arayüz tasarımları).
* **Controller:** İş mantığı ve kullanıcı etkileşimleri.

### 2. Tasarım Desenleri (Design Patterns)
* **Singleton Pattern:** DatabaseConnection sınıfı ile uygulama genelinde tek bir veritabanı bağlantısı yönetilir.
* **Observer Pattern:** İzin taleplerinin durum değişikliklerinde ilgili birimlerin tetiklenmesi için kullanılmıştır.

### 3. Veritabanı Transaction Yönetimi
Öğrenci taşıma (Transfer) ve silme işlemleri kritik öneme sahiptir. Olası bir hata durumunda (elektrik kesintisi vb.) verinin bozulmaması için Commit/Rollback mekanizması kurulmuştur.

---

## Ekran Görüntüleri

| Giriş Ekranı | Yönetici Dashboard |
| :---: | :---: |
| <img src="https://github.com/user-attachments/assets/47defdaf-401a-41d6-972a-53249b070988" width="400"> | <img src="https://github.com/user-attachments/assets/85b2b79a-8870-499a-a4f9-443de2ed7613" width="400"> |

| Oda Haritası | Öğrenci Paneli |
| :---: | :---: |
| <img src="https://github.com/user-attachments/assets/a79c0caf-a032-45f0-96cd-8e087e36a07d" width="400"> | <img src="https://github.com/user-attachments/assets/3b49fe0d-7b38-427b-8a01-aa51f659d95c" width="400"> |

*(Not: Görsellerin görünmesi için proje dizininde screenshots adında bir klasör oluşturup ilgili resimleri ekleyiniz.)*

---

## Kurulum ve Çalıştırma

Projeyi yerel makinenizde çalıştırmak için aşağıdaki adımları izleyin:

1.  **Projeyi Klonlayın:**
    ```bash
    git clone [https://github.com/edizklu/YurtYonetimSistemi.git](https://github.com/edizklu/YurtYonetimSistemi.git)
    cd YurtYonetimSistemi
    ```

2.  **Veritabanını Hazırlayın:**
    Proje ilk açılışta `yurt_otomasyon.db` dosyasını otomatik oluşturur.

3.  **Projeyi Derleyin ve Çalıştırın (IntelliJ IDEA):**
    * Maven bağımlılıklarını yükleyin (Load Maven Changes).
    * `src/main/java/com/yurt/main/Launcher.java` dosyasını çalıştırın.

### Giriş Bilgileri (Varsayılan)
* **Yönetici:** `admin` / `1234`
* **Öğrenci:** Yönetici panelinden yeni öğrenci oluşturarak giriş yapabilirsiniz.

---

## Veritabanı Şeması

Proje SQLite veritabanını kullanır ve aşağıdaki ilişkisel yapıya sahiptir:

* **USERS:** (id, username, password, role)
* **ROOMS:** (id, room_number, capacity, current_count, type)
* **STUDENTS:** (id, user_id, room_id, name, surname, tc_no...)
* **REQUESTS:** (id, student_id, start_date, end_date, status)

---

## Geliştirici Ekibi

* **Ediz ŞENTÜRK** - Back-End & Database Architecture
* **Cem DÖNMEZ** - Front-End & UI Design

---

## Lisans

Bu proje Kırklareli Üniversitesi Yazılım Mühendisliği bölümü projesi olarak geliştirilmiştir.
