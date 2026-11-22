# QR Based Smart Inventory App

An Android supermarket app (Java + XML) where shoppers scan product QR codes into a basket, pay, and get a PDF bill, while the store manager manages stock, prints QR labels and sees sales reports.

Mobile App Development end-semester project. Team: Ashith (backend, data, reports) and Sriniketh (UI, scanner, device features).

## Features
- **Shopper:** browse, search (text or voice) and filter products; scan a QR code and pick a quantity in a bottom sheet; edit the basket (stepper, swipe to delete, shake to clear); checkout with a discount, GST and a simulated UPI payment; share or view a PDF bill; order history with re-order.
- **Admin:** add, edit and delete products, fill details from a real barcode (Open Food Facts), generate and print QR codes, import inventory from CSV.
- **Reports:** pie, bar and line charts, inventory and low-stock CSV export, order history.
- **Device features:** CameraX + ML Kit scanning, light sensor auto torch, accelerometer shake, GPS with an OpenStreetMap nearest-store map, low-battery and offline banners.
- **Background:** WorkManager stock and expiry alerts, a foreground backup service that saves a CSV to Downloads.
- **Settings:** alert switch, expiry warning window, account details.

## Tech
Java, XML, Material 3, Navigation Component, Room (SQLite), ViewModel and LiveData, CameraX, ML Kit, ZXing, Retrofit and Gson, WorkManager, FileProvider, ContentProvider, MPAndroidChart, osmdroid. Gradle with Kotlin DSL and a version catalog.

## Run it
1. Clone the repository and open it in Android Studio.
2. Wait for the Gradle sync to finish.
3. Run on an emulator or a phone (a phone is best for the camera, torch and shake).

## Demo accounts
| Role | Email | Password |
|---|---|---|
| Customer | shopper@qrmart.com | shop123 |
| Admin | admin@qrmart.com | admin123 |

New admin accounts need the code `QRMART-ADMIN`. The first launch seeds 24 products (QR codes `PROD-1001` to `PROD-1024`) and a week of sample sales.

## Project structure
`ui` screens and adapters, `viewmodel` ViewModels, `data` database, repository and ContentProvider, `model` entities, `network` Retrofit, `service` WorkManager, backup service and receivers, `utils` PDF, CSV, charts and helpers.

## Notes
- The emulator has no flash and cannot read QR codes from its virtual camera; use a phone for those parts.
- Login and payment are simulated and local to the device.