# SoundPay - UPI Payment Announcement App

SoundPay is an Android application that automatically announces UPI payment notifications using Text-to-Speech and tracks your earnings in real-time.

## Features

✅ **Automatic Payment Announcements**
- Announces UPI payments instantly using Text-to-Speech
- Works even when phone is locked
- No internet required
- Supports English and Hindi languages

✅ **Earnings Tracking**
- Today's earnings
- This week's earnings (Sunday to Saturday)
- This month's earnings
- All calculations in Indian Standard Time (IST)

✅ **Supported UPI Apps**
- PhonePe
- Google Pay
- Paytm
- BHIM
- Amazon Pay
- WhatsApp Pay

✅ **Privacy-First**
- All data stored locally on your device
- No cloud sync
- No data sharing
- No internet connectivity required

## Requirements

- Android 7.0 (API 24) or higher
- Notification access permission
- Post notifications permission (Android 13+)

## How to Use

### 1. Install the App
- Install the APK on your Android device
- Open SoundPay

### 2. Enable Notification Access
- Tap "Enable Access" button on the home screen
- Find "SoundPay" in the notification access settings
- Toggle it ON
- Return to the app

### 3. Configure Settings
- **Enable Announcements**: Toggle ON to announce payments
- **Language**: Choose English or हिंदी for announcements
- **Auto Start on Boot**: Enable to start listening after device restart

### 4. Start Receiving Payments
- When a UPI payment is received, you'll hear an announcement like:
  - English: "Payment received of ₹150 from Rahul"
  - Hindi: "₹150 रुपये राहुल से प्राप्त हुए"

## Permissions

The app requires the following permissions:

- **Notification Access**: To read UPI payment notifications
- **Foreground Service**: To announce payments in the background
- **Post Notifications**: To show service status (Android 13+)
- **Boot Completed**: To auto-start after device restart (optional)

## Architecture

### Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Database**: Room (local SQLite)
- **State Management**: StateFlow
- **Build Tool**: Gradle with KSP (No KAPT)

### Key Components

1. **NotificationListenerService**
   - Listens to notifications from supported UPI apps
   - Filters payment-related notifications
   - Parses payment details

2. **PaymentParser**
   - Extracts amount and sender name from notification text
   - Validates payment keywords
   - Filters out OTPs, failed payments, and promotions

3. **PaymentAnnouncementService**
   - Foreground service for announcements
   - Uses Android Text-to-Speech API
   - Supports multiple languages

4. **Room Database**
   - Stores payment history locally
   - Calculates earnings by time period
   - Maintains payment records

5. **ViewModel**
   - Manages UI state
   - Observes database changes
   - Updates earnings in real-time

## Build Instructions

### Debug Build
```bash
./gradlew assembleDebug
```

The debug build includes:
- Debug card showing notification statistics
- Verbose logging
- Development tools

### Release Build
```bash
./gradlew assembleRelease
```

The release build:
- Removes debug UI elements
- Disables logging
- Optimized for production

## Project Structure

```
app/src/main/java/com/example/soundpay1/
├── data/
│   ├── Payment.kt              # Room entity
│   ├── PaymentDao.kt           # Database access object
│   ├── PaymentDatabase.kt      # Room database
│   └── PaymentRepository.kt    # Data repository
├── parser/
│   ├── ParsedPayment.kt        # Parsed payment data class
│   └── PaymentParser.kt        # Payment parsing logic
├── service/
│   ├── PaymentNotificationListenerService.kt  # Notification listener
│   └── PaymentAnnouncementService.kt          # TTS announcement service
├── receiver/
│   └── BootReceiver.kt         # Boot completed receiver
├── util/
│   ├── ISTTimeHelper.kt        # IST time utilities
│   ├── Logger.kt               # Debug logging
│   └── UpiAppConfig.kt         # Supported UPI apps config
├── viewmodel/
│   └── PaymentViewModel.kt     # UI state management
├── ui/
│   ├── components/
│   │   └── Components.kt       # Reusable UI components
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
└── MainActivity.kt             # Main activity
```

## Adding New UPI Apps

To add support for a new UPI app:

1. Open `util/UpiAppConfig.kt`
2. Add the app's package name and display name to `SUPPORTED_UPI_APPS` map:

```kotlin
val SUPPORTED_UPI_APPS = mapOf(
    // ...existing apps...
    "com.newapp.package" to "New App Name"
)
```

3. Test with notifications from the new app

## Logging

All logs are tagged with `SoundPay` and include:
- Notification received events
- Payment parsing success/failure
- Service lifecycle events
- TTS operations
- Database operations
- UI state updates

Logs are automatically removed in release builds.

## Play Store Compliance

✅ Clear disclosure for notification access usage
✅ No data collection or sharing
✅ No background audio abuse
✅ Accurate app description
✅ No misleading claims

## Known Limitations

- Only supports phone speaker output (no Bluetooth routing)
- Requires notification access permission
- Works only with supported UPI apps
- Hindi TTS quality depends on device TTS engine

## Troubleshooting

### Announcements Not Working
1. Check if notification access is enabled
2. Verify "Enable Announcements" is toggled ON
3. Ensure device volume is not muted
4. Check if TTS engine is installed (Settings → Language & Input → Text-to-Speech)

### Service Shows Inactive
1. Go to Settings → Notification Access
2. Toggle SoundPay OFF then ON
3. Return to the app and refresh

### No Earnings Showing
1. Ensure notification access is enabled
2. Send a test UPI payment
3. Check debug card (debug build only) for notification count

## Version History

### v1.0 (Initial Release)
- UPI payment announcements
- Earnings tracking (daily, weekly, monthly)
- Support for 6 major UPI apps
- English and Hindi language support
- Local data storage
- Debug mode for development

## License

Copyright © 2025 SoundPay. All rights reserved.

## Support

For issues or feature requests, please check the notification access permissions and ensure you're using a supported UPI app.

---

**Privacy Notice**: SoundPay does not collect, store, or transmit any personal data. All payment information is stored locally on your device and never leaves your phone.

