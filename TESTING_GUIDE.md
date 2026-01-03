# SoundPay - Testing & Troubleshooting Guide

## 🔧 Critical Fixes Applied

### Issue 1: Wrong Notification Extras Keys ✅ FIXED
**Problem**: Using string literals `"android.bigText"` instead of Android constants  
**Fix**: Changed to `android.app.Notification.EXTRA_BIG_TEXT`, `EXTRA_TEXT`, `EXTRA_TITLE`  
**Impact**: Notifications were not being read correctly

### Issue 2: Enhanced Text Extraction ✅ FIXED
**Problem**: Only checking 3 extras fields  
**Fix**: Now checking 6 fields: bigText, text, subText, title, infoText, summaryText  
**Impact**: Better coverage of different UPI notification formats

### Issue 3: Enhanced Payment Keywords ✅ FIXED
**Problem**: Limited keyword detection  
**Fix**: Added more keywords: "transferred", "account credited", "rs.", "inr", "₹"  
**Impact**: Better detection of various payment notification formats

### Issue 4: Combined Text Parsing ✅ FIXED
**Problem**: Some notifications split info across multiple fields  
**Fix**: If single field parsing fails, retry with combined text from all fields  
**Impact**: Higher success rate for complex notifications

### Issue 5: UI Refresh on Resume ✅ FIXED
**Problem**: UI not updating when returning to app  
**Fix**: Added lifecycle observer to refresh on every ON_RESUME event  
**Impact**: UI now updates when you return to the app

### Issue 6: Enhanced Logging ✅ ADDED
**Problem**: Hard to debug what's happening  
**Fix**: Added comprehensive logging at every step  
**Impact**: Can now see exactly what's happening in logcat

### Issue 7: Test Button ✅ ADDED
**Problem**: Hard to test TTS without real payment  
**Fix**: Added test announcement button in debug mode  
**Impact**: Can test TTS directly from the app

---

## 🧪 Testing Instructions

### Step 1: Rebuild and Install
```bash
cd /Users/maheshmishra/AndroidStudioProjects/soundpay1
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Step 2: Enable Notification Access
1. Open SoundPay app
2. You should see status "INACTIVE" (red)
3. Tap "Enable Access" button
4. Find "SoundPay" in the list
5. Toggle it ON
6. Press back to return to app
7. Status should now show "ACTIVE" (green)

### Step 3: Test TTS Announcement (Debug Mode Only)
1. In the app, below the debug card, you'll see "🔊 Test Announcement" button
2. Tap the button
3. You should hear: "Payment received of ₹100.00 from Test User"
4. If you hear the announcement, TTS is working! ✅
5. If not, check:
   - Device volume is not muted
   - TTS engine is installed (Settings → Language & Input → Text-to-Speech)

### Step 4: Test with Real UPI Payment
1. Send a small test payment to yourself via UPI
2. Use PhonePe, Google Pay, Paytm, or BHIM
3. Wait for the notification
4. You should:
   - Hear the announcement
   - See earnings update on the home screen
   - See debug card update with notification count

### Step 5: Check Logcat for Debugging
```bash
adb logcat -s SoundPay:D
```

Look for these log entries:
```
Notification received from: com.phonepe.app
Notification accepted: from PhonePe
BigText: [notification text]
Text: [notification text]
Title: [notification text]
========== PAYMENT PARSING START ==========
Payment keyword found!
Amount extracted: 100.00
Sender name extracted: John Doe
Payment parsed successfully
DB insert success: id=1
Starting announcement service
TTS speak called: Payment received of ₹100.00 from John Doe
```

---

## 🔍 Troubleshooting

### Problem: No announcement heard

**Possible Causes:**
1. ❌ Announcements disabled
   - **Fix**: Check Settings, toggle "Enable Announcements" ON

2. ❌ Volume muted
   - **Fix**: Increase media volume

3. ❌ TTS not initialized
   - **Check logcat**: Look for "TTS initialization failed"
   - **Fix**: Install Google TTS from Play Store

4. ❌ Service not starting
   - **Check logcat**: Look for "Failed to start announcement service"
   - **Fix**: Check foreground service permission in Android settings

### Problem: UI not updating

**Possible Causes:**
1. ❌ Payment not being saved to database
   - **Check logcat**: Look for "DB insert success"
   - **If missing**: Payment was not parsed correctly

2. ❌ Room database not emitting updates
   - **Check**: Are you using Flow correctly?
   - **Fix**: Already using Flow - should update automatically

3. ❌ ViewModel not observing
   - **Check**: Already observing with StateFlow
   - **Fix**: Lifecycle observer added to refresh on resume

### Problem: Notification not detected

**Possible Causes:**
1. ❌ Listener not active
   - **Check**: Status card shows "INACTIVE"
   - **Fix**: Enable notification access

2. ❌ UPI app not supported
   - **Check logcat**: Look for "Notification rejected: not from supported UPI app"
   - **Fix**: Add app to UpiAppConfig.kt

3. ❌ No payment keywords found
   - **Check logcat**: Look for "no payment keywords found"
   - **Check logcat**: Shows actual notification text
   - **Fix**: May need to add more keywords or adjust parser

4. ❌ No amount found
   - **Check logcat**: Look for "no amount found"
   - **Check logcat**: Shows notification text that was parsed
   - **Fix**: May need to adjust amount regex patterns

### Problem: Wrong amount displayed

**Possible Causes:**
1. ❌ Parsing incorrect amount
   - **Check logcat**: Look for "Amount extracted: X"
   - **Fix**: Adjust AMOUNT_PATTERNS in PaymentParser.kt

---

## 📊 Expected Log Flow

### Successful Payment Processing:
```
1. Notification received from: com.phonepe.app (Total: 1)
2. Notification rejected: not from supported UPI app [if not UPI]
   OR
   Notification accepted: from PhonePe
3. Available extras keys: [android.title, android.text, android.bigText, ...]
4. BigText: Payment of ₹100 received from John Doe
5. Text: Payment of ₹100 received
6. Title: PhonePe
7. ========== PAYMENT PARSING START ==========
8. Payment keyword found!
9. Amount extracted: 100.00
10. Sender name extracted: John Doe
11. Payment parsed successfully: amount=100.00, sender=John Doe, app=PhonePe
12. ========== PAYMENT PARSING END (SUCCESS) ==========
13. DB insert success: id=1
14. Starting announcement service
15. PaymentAnnouncementService: onStartCommand
16. TTS initialized successfully
17. TTS speak called: Payment received of ₹100.00 from John Doe
18. TTS speak started
19. TTS speak completed
20. UI recomposition trigger: 1 recent payments, 1 total
21. Earnings updated: today=100.0, week=100.0, month=100.0
```

### Failed Payment Processing Examples:

**Not from UPI app:**
```
Notification received from: com.google.android.gm
Notification rejected: not from supported UPI app
```

**No payment keywords:**
```
Notification accepted: from PhonePe
========== PAYMENT PARSING START ==========
Text lowercase: your otp is 123456
Notification rejected: no payment keywords found
========== PAYMENT PARSING END (NO KEYWORDS) ==========
```

**No amount found:**
```
========== PAYMENT PARSING START ==========
Payment keyword found!
Payment parsing failed: no amount found
========== PAYMENT PARSING END (NO AMOUNT) ==========
```

---

## 🎯 Testing Checklist

- [ ] Rebuild app with latest code
- [ ] Install on device
- [ ] Enable notification access
- [ ] Verify status shows "ACTIVE" (green)
- [ ] Test announcement button works
- [ ] Send real UPI test payment
- [ ] Verify announcement is heard
- [ ] Verify today's earnings updates
- [ ] Lock phone and send payment
- [ ] Verify announcement still works
- [ ] Switch language to Hindi
- [ ] Test announcement in Hindi
- [ ] Check logcat shows all expected logs
- [ ] Toggle announcements OFF
- [ ] Send payment - should NOT announce
- [ ] Toggle announcements ON
- [ ] Send payment - should announce

---

## 🐛 Debug Mode Features

1. **Debug Card** - Shows:
   - Last notification package name
   - Last notification timestamp
   - Total notifications received

2. **Test Announcement Button** - Tests TTS without real payment

3. **Enhanced Logging** - All operations logged to logcat

---

## 📝 Sample Notification Texts to Test

### PhonePe:
```
"Payment of ₹150.00 received from Rahul Kumar"
"You received ₹500 from John"
"₹1,000.00 credited to your account from PhonePe"
```

### Google Pay:
```
"You received ₹200.00 from Sarah"
"Payment successful! ₹300 from GPay user"
```

### Paytm:
```
"Rs.450 credited to Paytm Wallet from Mike"
"Payment received of Rs 600.50"
```

---

## 🔄 After Changes Workflow

1. Make code changes
2. Rebuild: `./gradlew assembleDebug`
3. Install: `adb install -r app/build/outputs/apk/debug/app-debug.apk`
4. Clear logcat: `adb logcat -c`
5. Start logcat: `adb logcat -s SoundPay:D`
6. Test in app
7. Analyze logs

---

## ✅ Success Indicators

✅ Status card shows GREEN "ACTIVE"  
✅ Test announcement button plays audio  
✅ Real payment triggers announcement within 1 second  
✅ Earnings update immediately  
✅ Debug card shows increasing notification count  
✅ Logcat shows complete successful flow  
✅ Works with phone locked  
✅ Works in both English and Hindi  

---

## 🚨 If Still Not Working

1. **Share logcat output** - Run: `adb logcat -s SoundPay:D > soundpay.log`
2. **Share notification text** - Check debug card or logcat for actual notification text
3. **Check Android version** - Some features require Android 7.0+
4. **Check TTS** - Test TTS in other apps (Settings → Accessibility → Text-to-Speech)
5. **Check permissions** - Settings → Apps → SoundPay → Permissions

---

**Last Updated**: After critical fixes for notification extras and UI updates

