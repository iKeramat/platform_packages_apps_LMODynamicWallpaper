# LMODroid Dynamic Wallpaper

LMODroid Dynamic Wallpaper is an Android live wallpaper service that dynamically changes the wallpaper based on the time of day.

## Installation
### Using Gradle/Studio
1. Clone the repository:
   ```sh
   git clone https://github.com/iKeramat/platform_packages_apps_LMODynamicWallpaper.git
   cd platform_packages_apps_LMODynamicWallpaper
   ```
2. Build the APK using Gradle:
   ```sh
   ./gradlew assembleDebug
   ```
3. Install the APK on your Android device:
   ```sh
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

### Using AOSP Soong
1. Setup build env
   ```sh
   . build/envsetup.sh
   ```
2. Clone the repository:
   ```sh
   git clone https://github.com/iKeramat/platform_packages_apps_LMODynamicWallpaper.git packages/apps/LMODynamicWallpaper
   ```
3. Lunch your device tree
   ```sh
   lunch device-codename
   ```
4. Build project
   ```sh
   m LMODynamicWallpaper
   ```
5. Install the APK on your Android device:
   ```sh
   adb install -r $OUT/system/product/app/LMODynamicWallpaper/LMODynamicWallpaper.apk
   ```

### AOSP Integration
To set this live wallpaper as the default in an AOSP build:
1. Modify the overlay:
   ```xml
    <!-- Component name of the default wallpaper. This will be ImageWallpaper if not
         specified -->
     <string name="default_wallpaper_component" translatable="false">com.libremobileos.dynamicwallpaper/com.libremobileos.dynamicwallpaper.GLWallpaperService</string>   
    ```
2. Build LMODynamicWallpaper in Products makefile:
   ```xml
    # Dynamic Wallpaper
    PRODUCT_PACKAGES += \
        LMODynamicWallpaper
    ```
3. Build and flash the AOSP ROM.

## License
This project is licensed under the Apache License 2.0
