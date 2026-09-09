# Google Play Release & R8 Rules for Worldwide Distribution

# Preserve SourceFile and line numbers for Google Play vitals & crash reporting
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Room Database
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public void <init>();
}
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# App Data Models & Room Entities
-keep class com.example.data.model.** { *; }
-keep class com.example.data.dao.** { *; }
-keep class com.example.ui.** { *; }

# Moshi & JSON Parsing
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <fields>;
}
-keep class com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**

# Retrofit & OkHttp Networking
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.** {
    volatile <fields>;
}

# Biometric & Camera
-keep class androidx.biometric.** { *; }
-keep class androidx.camera.** { *; }

# ZXing QR Code
-keep class com.google.zxing.** { *; }

# Google Mobile Ads (AdMob) & Consent SDK
-keep public class com.google.android.gms.ads.** {
   public *;
}
-keep public class com.google.ads.** {
   public *;
}
-keep class com.google.android.gms.internal.ads.** { *; }
-dontwarn com.google.android.gms.ads.**
-keep class com.google.android.gms.** { *; }
-keep class com.google.android.ump.** { *; }

# Google Generative AI (Gemini SDK)
-keep class com.google.ai.client.generativeai.** { *; }
-dontwarn com.google.ai.client.generativeai.**

