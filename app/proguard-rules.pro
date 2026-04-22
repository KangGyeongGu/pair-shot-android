# =============================================================================
# PairShot ProGuard Rules
# =============================================================================

# =============================================================================
# Kotlin
# =============================================================================

# Kotlin metadata — reflection 및 serialization 런타임에 필요
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature
-keepattributes SourceFile, LineNumberTable
-keep class kotlin.Metadata { *; }

# Kotlin companion objects
-keepclassmembers class ** {
    ** Companion;
}

# Kotlin object (싱글턴) 보호
-keepclassmembers class * extends kotlin.coroutines.jvm.internal.BaseContinuationImpl {
    <fields>;
}

# =============================================================================
# Coroutines
# =============================================================================

# 디버그 메타데이터 (DebugMetadata) — stacktrace 복원에 사용
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-keepclassmembernames class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keepclassmembernames class kotlinx.coroutines.CoroutineExceptionHandler { *; }

# =============================================================================
# Hilt
# =============================================================================

# Hilt가 생성하는 컴포넌트/모듈 클래스 보호
-keep class dagger.hilt.** { *; }
-keep class dagger.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.InstallIn class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }

# Hilt ViewModel 팩토리 — KSP 생성 코드 보호
-keepclasseswithmembernames class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}

# Inject 어노테이션이 붙은 생성자/필드/메서드 보호
-keepclassmembers,allowobfuscation class * {
    @javax.inject.Inject <init>(...);
    @javax.inject.Inject <fields>;
}

# =============================================================================
# Room
# =============================================================================

# @Entity 클래스 — 필드명이 DB 컬럼명에 매핑되므로 난독화 금지
-keep @androidx.room.Entity class * { *; }

# @Dao 인터페이스 — KSP 생성 구현체가 참조
-keep @androidx.room.Dao class * { *; }

# Room Database 서브클래스 보호
-keep class * extends androidx.room.RoomDatabase { *; }

# TypeConverter 메서드 보호
-keepclassmembers class * {
    @androidx.room.TypeConverter <methods>;
}

# =============================================================================
# Kotlin Serialization
# =============================================================================

# @Serializable 클래스 — kotlinx.serialization 런타임이 reflection으로 접근
-keepattributes *Annotation*
-keep @kotlinx.serialization.Serializable class * { *; }

# serializer() 동반 객체 메서드 보호
-keepclassmembers @kotlinx.serialization.Serializable class * {
    static ** serializer();
    static ** $serializer;
    ** Companion;
}

# kotlinx.serialization 내부 클래스 보호
-keep class kotlinx.serialization.** { *; }

# =============================================================================
# Navigation Compose (type-safe routes)
# =============================================================================

# type-safe route는 @Serializable 클래스로 정의됨 — 위의 serialization 규칙과 중복 보호
# NavType.SerializableType이 reflection으로 파싱하므로 route data class 보호
-keep class com.pairshot.**.route.** { *; }
-keep class com.pairshot.**.navigation.** { *; }

# =============================================================================
# Glide
# =============================================================================

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
-keep class com.bumptech.glide.GeneratedAppGlideModuleImpl
-dontwarn com.bumptech.glide.**

# =============================================================================
# CameraX
# =============================================================================

# CameraX 내부에서 reflection으로 접근하는 클래스들
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# =============================================================================
# AndroidX / Jetpack 공통
# =============================================================================

# ViewModel — Hilt가 리플렉션으로 생성
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# DataStore — proto 없이 Preferences 사용 시 보호
-keep class androidx.datastore.** { *; }

# Parcelable 구현체 보호 (AGP 9+ 자동 처리되지만 명시적 보호)
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
