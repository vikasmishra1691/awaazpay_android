# Onboarding Flow Testing Guide

## Prerequisites
1. Sync Gradle to download DataStore dependency
2. Clean and rebuild the project
3. Install the app on a test device or emulator

## Testing Scenarios

### Scenario 1: First Launch (Fresh Install)
**Expected Behavior**: Onboarding should be shown

**Steps**:
1. Uninstall the app if previously installed
2. Install the app
3. Launch the app

**What to Verify**:
- ✅ Onboarding screen appears immediately
- ✅ Dark gradient background is visible
- ✅ Page indicator shows "1/3" (first dot active)
- ✅ Screen 1 shows: Welcome message, notifications icon (cyan), features list
- ✅ "Skip" and "Continue" buttons are visible

### Scenario 2: Navigate Through Onboarding
**Steps**:
1. From Screen 1, tap "Continue"
2. From Screen 2, tap "Continue"
3. From Screen 3, tap "Get Started"

**What to Verify**:
- ✅ Smooth slide animation between screens (left to right)
- ✅ Page indicator updates (1/3 → 2/3 → 3/3)
- ✅ Screen 2 shows: Lock icon (green), permissions explanation
- ✅ Screen 3 shows: Heart icon (pink), privacy message
- ✅ Screen 3 has "Get Started" instead of "Continue"
- ✅ No "Skip" button on Screen 3
- ✅ After "Get Started", home screen appears

### Scenario 3: Skip Onboarding
**Steps**:
1. Fresh install
2. From Screen 1 or 2, tap "Skip"

**What to Verify**:
- ✅ Immediately navigates to home screen
- ✅ Onboarding is marked as completed
- ✅ Home screen loads normally

### Scenario 4: Subsequent Launches
**Expected Behavior**: Onboarding should NOT be shown again

**Steps**:
1. Complete onboarding (either skip or finish)
2. Close the app completely
3. Re-launch the app

**What to Verify**:
- ✅ Home screen appears immediately
- ✅ No onboarding shown
- ✅ App works normally

### Scenario 5: Privacy Information Display
**Steps**:
1. Complete onboarding
2. Scroll down on home screen to Privacy section
3. Read the "Your Privacy Matters" card

**What to Verify**:
- ✅ Card shows "100% Offline" badge
- ✅ Shows 4 main privacy points about payment data
- ✅ Shows divider and "About Feedback" section
- ✅ Shows 4 feedback-related points:
  - Feedback is optional
  - Secure storage explanation
  - What data is included
  - How it helps
- ✅ No technical terms like "Firebase" or "database"

## UI/UX Quality Checks

### Visual Design
- ✅ Gradient background (dark blue: #1A1A2E → #16213E)
- ✅ Icons in colored circles (cyan, green, pink)
- ✅ Clean white text on dark background (good contrast)
- ✅ Smooth animations (no jank or lag)
- ✅ Page indicators animate smoothly
- ✅ Cards have rounded corners and subtle transparency

### Text Content
- ✅ No technical jargon
- ✅ Trust-building tone (not scary/legal)
- ✅ Emojis used effectively
- ✅ Clear, concise messaging
- ✅ Proper grammar and spelling

### Touch Targets
- ✅ Buttons are at least 56dp tall
- ✅ Easy to tap "Skip", "Continue", and "Get Started"
- ✅ No accidental taps

### Performance
- ✅ Onboarding loads quickly
- ✅ Animations are smooth (60 FPS)
- ✅ No ANR (Application Not Responding)
- ✅ Transitions feel responsive

## Developer Testing Tools

### Reset Onboarding (For Testing)
Add this code temporarily in MainActivity for testing:

```kotlin
// In onCreate() or in a debug button
if (BuildConfig.DEBUG) {
    OnboardingDebugHelper.resetOnboarding(this)
    // Restart app to see onboarding again
}
```

Or use adb to clear app data:
```bash
adb shell pm clear com.example.awaazpay
```

### Check Onboarding Status
```kotlin
lifecycleScope.launch {
    val manager = OnboardingManager(context)
    manager.isOnboardingCompleted.collect { isCompleted ->
        Logger.d("Onboarding completed: $isCompleted")
    }
}
```

### View DataStore File
```bash
adb shell
run-as com.example.awaazpay
cd files/datastore
ls -la
# Should see: onboarding_prefs.preferences_pb
```

## Edge Cases to Test

### 1. Process Death During Onboarding
**Steps**:
1. Launch app (onboarding appears)
2. Navigate to Screen 2
3. Kill app from recent apps
4. Re-launch app

**Expected**: Onboarding restarts from Screen 1 (state not saved mid-flow)

### 2. Rotate Device During Onboarding
**Steps**:
1. Start onboarding
2. Rotate device to landscape
3. Rotate back to portrait

**Expected**: 
- Screen content adjusts to orientation
- Current page is preserved
- No crashes

### 3. Background/Foreground During Onboarding
**Steps**:
1. Start onboarding
2. Press home button
3. Return to app

**Expected**: Onboarding remains on same screen

### 4. Permission Dialogs
**Steps**:
1. Complete onboarding
2. System shows notification permission dialog (Android 13+)

**Expected**: Permission dialog doesn't interfere with completion

## Common Issues & Solutions

### Issue: Onboarding Shows Every Time
**Cause**: DataStore not persisting
**Solution**: 
- Check DataStore dependency is added
- Verify Gradle sync completed
- Check for exceptions in logcat

### Issue: DataStore Import Errors
**Cause**: Gradle not synced
**Solution**: 
1. Sync Gradle files
2. Clean and rebuild project
3. Restart IDE if needed

### Issue: Black Screen After "Get Started"
**Cause**: Navigation issue
**Solution**: Check MainActivity HomeScreen composable is rendering

### Issue: Animations Stuttering
**Cause**: Main thread overload
**Solution**: 
- Profile with Android Profiler
- Check for blocking operations
- Verify onboarding logic is not blocking UI

## Acceptance Criteria

All of the following must pass:

- [ ] Fresh install shows onboarding
- [ ] Can complete all 3 screens
- [ ] Can skip from screens 1-2
- [ ] "Get Started" navigates to home
- [ ] Subsequent launches skip onboarding
- [ ] Privacy card shows feedback disclosure
- [ ] No technical terms in UI
- [ ] Smooth animations throughout
- [ ] No crashes or ANRs
- [ ] Works on Android 7.0+ (API 24+)
- [ ] Works on both portrait and landscape
- [ ] Text is readable (good contrast)
- [ ] Trust-building tone throughout

## Performance Benchmarks

Target metrics:
- **Initial Load**: < 300ms (from launch to first screen visible)
- **Screen Transition**: < 300ms (slide animation duration)
- **Completion**: < 50ms (DataStore write operation)
- **Memory**: < 50MB additional (during onboarding)

## Regression Testing

After implementing onboarding, verify:
- [ ] Home screen still works normally
- [ ] Payment announcements still work
- [ ] Transaction history still loads
- [ ] Settings still function
- [ ] Feedback submission still works
- [ ] No performance degradation on home screen

## Sign-Off Checklist

Before marking as complete:
- [ ] All acceptance criteria passed
- [ ] Tested on at least 2 devices/emulators
- [ ] Tested on different Android versions (API 24, 28, 31, 33+)
- [ ] No memory leaks detected
- [ ] Code reviewed
- [ ] Documentation updated
- [ ] Screenshots/video captured for release notes

