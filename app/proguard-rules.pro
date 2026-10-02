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

# Stack trace crash tetap punya nomor baris di build release/perf.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Moshi (KotlinJsonAdapterFactory = reflection) + Retrofit: butuh generic signature & annotation.
-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod
-keep class kotlin.Metadata { *; }
-keep class com.dayynime.wibuplay.data.model.** { *; }
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Entity Room.
-keep class com.dayynime.wibuplay.data.local.** { *; }

# Lapisan API (ApiResponseWrapper dkk) harus tetap utuh, kalau tidak Moshi gagal parse
# dan semua layar (Beranda, Jelajah, Jadwal, Cuplix) menampilkan "Gagal memuat".
-keep class com.dayynime.wibuplay.data.api.** { *; }
-keepclassmembers @com.squareup.moshi.JsonClass class * { *; }
