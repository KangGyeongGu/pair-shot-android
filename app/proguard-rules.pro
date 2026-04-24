-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature
-keepattributes SourceFile, LineNumberTable
-keep class kotlin.Metadata { *; }

-keepclassmembers class ** {
    ** Companion;
}

-keepclassmembers class * extends kotlin.coroutines.jvm.internal.BaseContinuationImpl {
    <fields>;
}

-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-keepclassmembernames class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keepclassmembernames class kotlinx.coroutines.CoroutineExceptionHandler { *; }

-keep class dagger.hilt.** { *; }
-keep class dagger.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.InstallIn class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }

-keepclasseswithmembernames class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}

-keepclassmembers,allowobfuscation class * {
    @javax.inject.Inject <init>(...);
    @javax.inject.Inject <fields>;
}

-keep @androidx.room.Entity class * { *; }

-keep @androidx.room.Dao class * { *; }

-keep class * extends androidx.room.RoomDatabase { *; }

-keepclassmembers class * {
    @androidx.room.TypeConverter <methods>;
}

-keepattributes *Annotation*
-keep @kotlinx.serialization.Serializable class * { *; }

-keepclassmembers @kotlinx.serialization.Serializable class * {
    static ** serializer();
    static ** $serializer;
    ** Companion;
}

-keep class kotlinx.serialization.** { *; }

-keep class com.pairshot.**.route.** { *; }
-keep class com.pairshot.**.navigation.** { *; }

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
-keep class com.bumptech.glide.GeneratedAppGlideModuleImpl
-dontwarn com.bumptech.glide.**

-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

-keep class * extends androidx.lifecycle.ViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

-keep class androidx.datastore.** { *; }

-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

-dontwarn org.slf4j.impl.StaticLoggerBinder

-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_** { *; }
-dontwarn com.google.mlkit.**
