# Optimization and security improvements
-repackageclasses ''
-allowaccessmodification
-overloadaggressively

# Keep attributes for better crash reporting but obscure source files
-keepattributes Signature,InnerClasses,EnclosingMethod,*Annotation*
-renamesourcefileattribute SourceFile

# Remove debug logs in release builds for security
# This prevents leaking sensitive information in logs
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
}

# Room specific rules to prevent obfuscation from breaking database queries
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>(...);
}
-keep @androidx.room.Entity class * { *; }
-keep class * extends androidx.room.RoomDatabase
-keep class * implements androidx.room.RoomDatabase$Callback
-keep class androidx.room.RoomDatabase { *; }

# Hilt and Dagger rules
-keep @dagger.hilt.EntryPoint class * { *; }
-keep @dagger.hilt.android.EntryPointAccessors class * { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponentManager
-keep class * implements dagger.hilt.internal.GeneratedComponent

# Keep domain models and Room entities
# Obfuscating these can break Room's reflection-based field mapping
-keep class com.nadhifhayazee.simplereminder.domain.model.** { *; }
-keep class com.nadhifhayazee.simplereminder.data.local.entity.** { *; }

# Coroutines rules
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.android.HandlerContext {
    long pass;
}

# Preserve Compose-related annotations to ensure correct UI behavior
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
    @androidx.compose.runtime.ReadOnlyComposable <methods>;
}
