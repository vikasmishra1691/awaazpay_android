# Latency Optimization Testing Guide

## 🧪 Pre-Testing Setup

### 1. Clean Build
```bash
./gradlew clean
./gradlew assembleDebug
```

### 2. Install on Test Device
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 3. Grant Permissions
1. Open SoundPay app
2. Grant notification access: Settings → Notification Access → Enable SoundPay
3. Grant notification permission (Android 13+)
4. Keep app open initially

---

## ⚡ Latency Tests (Critical)

### Test 1: First Payment After App Launch
**Expected Latency: <1 second**

**Steps:**
1. Launch app
2. Wait 2 seconds (TTS initialization)
3. Send test UPI payment (Google Pay/PhonePe)
4. Measure time from notification to voice start

**Success Criteria:**
- ✅ Announcement starts within 1 second
- ✅ Voice is clear and complete
- ✅ UI updates with payment within 2 seconds

**How to Measure:**
- Use stopwatch from notification arrival to voice start
- Check logcat timestamps:
  ```
  adb logcat | grep "Payment detected\|TTS"
  ```

---

### Test 2: Rapid Consecutive Payments
**Expected: All announced without delay**

**Steps:**
1. App running in background
2. Send 3 payments within 10 seconds
3. Observe all announcements

**Success Criteria:**
- ✅ All 3 payments announced
- ✅ Each announcement <1s after notification
- ✅ No crashes or ANR
- ✅ No skipped announcements

---

### Test 3: Payment with Screen Locked
**Expected: Announcement works perfectly**

**Steps:**
1. Launch app
2. Lock screen (power button)
3. Send test payment
4. Listen for announcement

**Success Criteria:**
- ✅ Announcement plays through speaker
- ✅ Latency <1 second
- ✅ Screen stays locked (no wake)

---

### Test 4: Payment After App Killed
**Expected: Service survives, announcement works**

**Steps:**
1. Launch app
2. Kill app from recent apps (swipe away)
3. Send test payment
4. Listen for announcement

**Success Criteria:**
- ✅ Announcement still plays
- ✅ Latency <1 second
- ✅ Service auto-restarts if needed

---

## 🔄 Service Lifecycle Tests

### Test 5: Service Persistence
**Expected: Service stays alive**

**Steps:**
1. Launch app
2. Check service status:
   ```bash
   adb shell dumpsys activity services | grep PaymentAnnouncementService
   ```
3. Wait 5 minutes
4. Check again

**Success Criteria:**
- ✅ Service shows `app=ProcessRecord` (running)
- ✅ Service has foreground notification
- ✅ `isServiceRunning = true`

---

### Test 6: TTS Initialization Count
**Expected: TTS initialized only once**

**Steps:**
1. Launch app
2. Send 5 test payments over 1 minute
3. Check logs:
   ```bash
   adb logcat | grep "TTS initializing"
   ```

**Success Criteria:**
- ✅ "TTS initializing" appears ONLY ONCE
- ✅ "TTS initialized" appears ONLY ONCE
- ✅ No repeated initialization

---

### Test 7: Auto-Start on Boot
**Expected: Service starts after reboot**

**Steps:**
1. Enable "Auto Start on Boot" in app settings
2. Reboot device
3. Wait 30 seconds after boot
4. Check service status:
   ```bash
   adb shell dumpsys activity services | grep PaymentAnnouncementService
   ```

**Success Criteria:**
- ✅ Service is running
- ✅ No app launch needed
- ✅ Foreground notification shows

---

## 💾 Database & UI Tests

### Test 8: UI Updates After Payment
**Expected: DB write doesn't block announcement**

**Steps:**
1. Open app (showing home screen)
2. Note current "Today's Earnings"
3. Send test payment of ₹100
4. Observe both announcement and UI

**Success Criteria:**
- ✅ Announcement happens first (<1s)
- ✅ UI updates within 1-2 seconds after
- ✅ "Today's Earnings" increases by ₹100
- ✅ Payment appears in transaction history

---

### Test 9: Multiple Payments DB Integrity
**Expected: All payments saved correctly**

**Steps:**
1. Note initial transaction count
2. Send 5 test payments (₹10, ₹20, ₹30, ₹40, ₹50)
3. Wait 5 seconds
4. Open transaction history

**Success Criteria:**
- ✅ All 5 payments listed
- ✅ Correct amounts
- ✅ Timestamps in order
- ✅ Sender names captured (if available)

---

## 🎤 Audio & TTS Tests

### Test 10: Hindi Language Support
**Expected: Hindi announcements work**

**Steps:**
1. Open app settings
2. Select "हिंदी (Hindi)"
3. Send test payment
4. Listen to announcement

**Success Criteria:**
- ✅ Announcement in Hindi
- ✅ Correct pronunciation
- ✅ Same latency as English

---

### Test 11: Audio Focus Handling
**Expected: Doesn't interrupt calls/music**

**Steps:**
1. Play music on device
2. Send test payment
3. Observe behavior

**Success Criteria:**
- ✅ Music volume ducks briefly
- ✅ Announcement plays clearly
- ✅ Music resumes after announcement
- ✅ No permanent interruption

**During Call Test:**
1. Make a phone call
2. Send test payment (from another device)
3. Observe behavior

**Success Criteria:**
- ✅ Call is NOT interrupted
- ✅ Announcement may be queued or skipped (acceptable)

---

## 🛡️ Stress & Edge Case Tests

### Test 12: Low Memory Conditions
**Expected: Service survives**

**Steps:**
1. Launch SoundPay
2. Open 10 other heavy apps (games, Chrome, etc.)
3. Return to home screen
4. Send test payment

**Success Criteria:**
- ✅ Announcement still works
- ✅ Service not killed
- ✅ TTS still initialized

---

### Test 13: Airplane Mode Recovery
**Expected: Service maintains state**

**Steps:**
1. Launch app
2. Enable Airplane Mode
3. Wait 30 seconds
4. Disable Airplane Mode
5. Send test payment

**Success Criteria:**
- ✅ Announcement works
- ✅ Service still running
- ✅ No re-initialization needed

---

### Test 14: Rapid App Restarts
**Expected: No duplicate services**

**Steps:**
1. Launch app
2. Kill app
3. Launch app again
4. Kill app
5. Launch app again
6. Check service count:
   ```bash
   adb shell dumpsys activity services | grep -c PaymentAnnouncementService
   ```

**Success Criteria:**
- ✅ Only ONE service instance
- ✅ No memory leaks
- ✅ `isServiceRunning` flag correct

---

## 📊 Performance Benchmarks

### Test 15: Latency Benchmark
**Measure exact timings using logcat**

**Steps:**
1. Enable verbose logging (if debug mode)
2. Send test payment
3. Extract timestamps:
   ```bash
   adb logcat -v time | grep "Payment detected\|speak"
   ```

**Expected Timeline:**
- `t=0ms`: Notification arrives
- `t=5-10ms`: Parser extracts data
- `t=10-20ms`: Intent sent to announcement service
- `t=20-50ms`: TTS speak() called
- `t=100-800ms`: Voice starts playing

**Success Criteria:**
- ✅ Total latency < 1000ms
- ✅ Parsing < 20ms
- ✅ Intent dispatch < 30ms
- ✅ TTS speak < 100ms

---

## 🔍 Debugging Tools

### Check Service Status
```bash
# List all services
adb shell dumpsys activity services

# Check specific service
adb shell dumpsys activity services com.example.soundpay1/.service.PaymentAnnouncementService

# Check if foreground
adb shell dumpsys activity services | grep -A 10 PaymentAnnouncementService
```

### Monitor Logcat
```bash
# All SoundPay logs
adb logcat | grep "SoundPay\|PaymentNotification\|PaymentAnnouncement"

# Only errors
adb logcat *:E | grep soundpay

# TTS specific
adb logcat | grep "TTS\|TextToSpeech"
```

### Check Notification Listener
```bash
# Verify listener is enabled
adb shell cmd notification allow_listener com.example.soundpay1/com.example.soundpay1.service.PaymentNotificationListenerService
```

### Monitor Memory
```bash
# Check memory usage
adb shell dumpsys meminfo com.example.soundpay1

# Watch for leaks
adb shell dumpsys meminfo com.example.soundpay1 | grep TOTAL
```

---

## ✅ Final Validation Checklist

Before marking as complete, verify:

### Core Functionality
- [ ] Payment announced when screen is locked
- [ ] Announcement happens <1s after notification
- [ ] UI updates after DB insert
- [ ] App shows foreground notification when listening
- [ ] No crashes when multiple payments arrive quickly
- [ ] No memory leaks (TTS released correctly)

### Architecture Validation
- [ ] NotificationListenerService exists and active
- [ ] Announcements don't happen in NotificationListener
- [ ] Room/Flow/ViewModel pattern intact
- [ ] Works when app is backgrounded or locked
- [ ] No breaking changes to UI or DB schema

### Service Lifecycle
- [ ] Service starts on app launch
- [ ] Service starts on boot (if enabled)
- [ ] Service starts when notification access granted
- [ ] Service stays alive (foreground + START_STICKY)
- [ ] Service survives app kill
- [ ] TTS initialized once and reused
- [ ] Inactivity timeout stops service after 10 minutes

### Performance
- [ ] Latency consistently <1 second
- [ ] TTS initialized only once per service lifecycle
- [ ] DB writes don't block announcements
- [ ] No excessive logging on critical path
- [ ] Audio focus handled properly

### Edge Cases
- [ ] Works with multiple rapid payments
- [ ] Handles low memory gracefully
- [ ] Recovers from TTS initialization failure
- [ ] No ANR (Application Not Responding)
- [ ] No duplicate service instances

---

## 🐛 Known Issues & Mitigations

### Issue: First announcement after boot may take 2-3s
**Cause:** TTS engine initialization
**Mitigation:** Pending queue holds announcements until ready
**Status:** Acceptable (one-time overhead)

### Issue: Service may be killed in extreme low memory
**Cause:** System reclaiming resources
**Mitigation:** START_STICKY auto-restarts
**Status:** Monitored in stress tests

### Issue: TTS voice may vary by device
**Cause:** Different Android versions/OEM TTS engines
**Mitigation:** Use default system TTS
**Status:** Expected behavior

---

## 📝 Test Report Template

```
## Test Report: Latency Optimization
**Date:** YYYY-MM-DD
**Tester:** [Name]
**Device:** [Model, Android Version]
**App Version:** [Version Code]

### Latency Tests
- [ ] Test 1: First Payment - Latency: ___ms
- [ ] Test 2: Rapid Payments - All announced: Yes/No
- [ ] Test 3: Locked Screen - Works: Yes/No
- [ ] Test 4: After App Kill - Works: Yes/No

### Service Tests
- [ ] Test 5: Persistence - Running after 5min: Yes/No
- [ ] Test 6: TTS Init - Count: ___
- [ ] Test 7: Boot - Auto-started: Yes/No

### Database Tests
- [ ] Test 8: UI Update - Timing: ___s
- [ ] Test 9: Multiple Payments - All saved: Yes/No

### Audio Tests
- [ ] Test 10: Hindi - Works: Yes/No
- [ ] Test 11: Audio Focus - Music resumes: Yes/No

### Stress Tests
- [ ] Test 12: Low Memory - Survives: Yes/No
- [ ] Test 13: Airplane Mode - Recovers: Yes/No
- [ ] Test 14: Restarts - Single instance: Yes/No

### Performance Benchmark
- [ ] Test 15: Average Latency: ___ms

### Issues Found
1. [Description]
2. [Description]

### Conclusion
**Status:** PASS / FAIL
**Notes:** [Additional observations]
```

---

## 🚀 Next Steps After Testing

1. **If all tests pass:**
   - Mark implementation as complete ✅
   - Proceed to production build
   - Monitor real-world performance

2. **If issues found:**
   - Document issues with logs
   - Debug using tools above
   - Re-test after fixes

3. **Production Monitoring:**
   - Track announcement latency
   - Monitor service crashes
   - Collect user feedback
   - Optimize further if needed

