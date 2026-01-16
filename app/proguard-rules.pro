# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Preserve line number information for debugging stack traces in production
-keepattributes SourceFile,LineNumberTable

# Hide the original source file name
-renamesourcefileattribute SourceFile

# ========================================
# Production-Ready ProGuard Configuration
# ========================================

# Keep error logging for crash diagnosis (Logger.e calls)
-keep class com.example.awaazpay.util.Logger {
    public static void e(java.lang.String, java.lang.Throwable);
    public static void e(java.lang.String);
}

# Keep Room Database classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-dontwarn androidx.room.paging.**

# Keep Firebase classes
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
}
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep Payment entity for Firebase/Room
-keep class com.example.awaazpay.data.Payment { *; }
-keep class com.example.awaazpay.parser.ParsedPayment { *; }

# Keep service classes (required for Android system)
-keep class com.example.awaazpay.service.** { *; }
-keep class com.example.awaazpay.receiver.** { *; }

# Keep ViewModel classes
-keep class com.example.awaazpay.viewmodel.** { *; }

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Optimize and shrink aggressively
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# Remove debug logs (Logger.d, Logger.i, Logger.w are already guarded by BuildConfig.DEBUG_MODE)
# R8 will automatically remove the code blocks when DEBUG_MODE is false

# Keep essential attributes
-keepattributes Signature, Exception, *Annotation*, InnerClasses, EnclosingMethod

