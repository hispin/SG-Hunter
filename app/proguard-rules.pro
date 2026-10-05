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
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ---------------------------------------------------------------------------
# R8 is enabled for release to strip unused library code - this removes the
# deprecated edge-to-edge calls (Window.setStatusBarColor/setNavigationBarColor)
# inside unused androidx.activity / Material classes that Google Play reports.
# ---------------------------------------------------------------------------

# keep stack traces readable in Crashlytics
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature,InnerClasses,EnclosingMethod,*Annotation*

# app code is kept as is (not shrunk or renamed): models are (de)serialized by Gson
# with reflection, WebFragment exposes a @JavascriptInterface, and classes are referenced
# from the manifest / layouts. Only the libraries are shrunk.
-keep class com.sensoguard.hunter.** { *; }

# email alarms (javax.mail / activation use reflection and service files)
-keep class javax.mail.** { *; }
-keep class javax.activation.** { *; }
-keep class com.sun.mail.** { *; }
-keep class myjava.awt.datatransfer.** { *; }
-dontwarn java.awt.**
-dontwarn javax.security.**
-dontwarn java.beans.**

# Azure notification hubs (old SDK without consumer rules) - only the classic NotificationHub API
# is used; the newer notificationhubs sub-package needs Volley, which the app does not include
-keep class com.microsoft.windowsazure.messaging.* { *; }
-dontwarn com.android.volley.**

# language list library
-keep class com.delight.** { *; }

# ViewPager finds its fixed children (the TabLayout inside vPager) by reading the
# @ViewPager.DecorView annotation at runtime - without this the tab bar disappears
-keepattributes RuntimeVisibleAnnotations
-keep @interface androidx.viewpager.widget.ViewPager$DecorView
-keep @androidx.viewpager.widget.ViewPager$DecorView class * { *; }
