# A-ZU — R8 is OFF in v1 (see app/build.gradle.kts), these rules are kept
# for when you re-enable minify later.
-keep class com.mahin.azu.** { *; }
-keep class coil.** { *; }
-dontwarn coil.**
