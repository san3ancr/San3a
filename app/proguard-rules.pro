# Firebase / Google Play
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Kotlin
-keepclassmembers class kotlin.Metadata { *; }

# App model classes used by Firestore (release build)
-keep class com.san3a.app.data.** { *; }
