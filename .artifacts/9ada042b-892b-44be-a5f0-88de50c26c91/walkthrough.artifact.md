# Dependency Update & Build Fix Walkthrough

The project has been successfully updated to use modern dependencies, including Android Gradle Plugin 9.3.0 and Kotlin 2.4.10. Several compilation and configuration issues were resolved to achieve a successful build.

## Changes Made

### Build Configuration
- **Repositories**: Added `mavenCentral()` and `jitpack.io` to ensure all 3rd-party libraries can be resolved.
- **JVM Target**: Updated Java and Kotlin compatibility to JVM 21, matching the requirements of AGP 9.3.0.
- **Min SDK**: Bumped `minSdkVersion` to 23 as required by `com.google.android.material:material:1.14.0`.
- **View Binding**: Enabled `viewBinding` in `app/build.gradle` to support modern UI patterns.

### Dependency Management
Updated several libraries in `gradle/libs.versions.toml` to versions available on Maven Central or JitPack:
- `tedpermission`: Migrated to `io.github.ParkSangGwon:tedpermission-normal:3.4.2`.
- `fancybuttons`: Reverted to `1.8.1` (available on Maven Central).
- `filechooser`: Reverted to `1.1.2` (available on Maven Central).
- `library` (ACProgressLite): Migrated to `com.github.Cloudist:ACProgressLite:1.2.1` via JitPack.

### Source Code Fixes
- **Manifest**: Added `tools:replace="android:label"` to `AndroidManifest.xml` to resolve a conflict with the `filechooser` library.
- **TedPermission**: Updated `RSAGenerateKeyFragment.java` to use the 3.x API (`TedPermission.create()` instead of `.with()`).
- **ChooserDialog**: Updated all occurrences in Fragments to use the 1.1.2 API (`new ChooserDialog().with(context)` and `.build()`).
- **Cleanup**: Removed redundant and broken Kotlin adapter/fragment files that were conflicting with existing Java implementations.

## Verification Results

### Automated Tests
- `gradle_assemble_all`: **Passed**
- `gradle_sync`: **Successful**

> [!IMPORTANT]
> The project now requires JDK 21 to build due to the updated JVM target and AGP version.

> [!TIP]
> Redundant Kotlin files in `adapter/` and `ui/ecc_eg/` were removed because they were incomplete and caused redeclaration errors with the functional Java versions.
