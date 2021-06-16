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

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

#-dontobfuscate
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

-keep class com.mskdev.githubsearchingtool.data.model.** {*;}

#Viewmodel
-keep class * extends androidx.lifecycle.** {*;}

#Parceler Library
-keep interface android.os.Parcelable
-keep @android.os.Parcelable class * { *; }
-keep class **$$Parcelable { *; }
-keep class kotlinx.parcelize.Parcelize {*;}

# OkHttp3
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontnote okhttp3.**

# Retrofit2
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }


# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.firebase.** { *; }
-keep class org.apache.** { *; }
-keepnames class com.fasterxml.jackson.** { *; }
-keepnames class javax.servlet.** { *; }
-keepnames class org.ietf.jgss.** { *; }
-dontwarn org.apache.**
-dontwarn org.w3c.dom.**

#Google Services
-keep class com.google.** { *; }
-keep class com.google.android.** { *; }