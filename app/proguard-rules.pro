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
-keepattributes LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

# Keep YouTube API classes
-keep class com.google.android.youtube.player.** { *; }
-keep interface com.google.android.youtube.player.** { *; }

# Keep Google API classes
-keep class com.google.api.services.youtube.** { *; }
-keep class com.google.api.client.** { *; }
-keep class com.google.http.** { *; }

# Keep model classes for YouTube API
-keep class * extends com.google.api.client.json.GenericJson {
    *;
}

# Keep Gson classes for API responses
-keep class * implements com.google.gson.JsonDeserializer
-keep class * implements com.google.gson.JsonSerializer
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep AndroidX and Material Design classes
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-keep class com.google.android.material.** { *; }

# Keep Glide classes for image loading
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# General Android optimizations
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom views
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
