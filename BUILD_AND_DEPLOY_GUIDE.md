# 🚀 Build and Deploy Guide for Play Store

## Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- Android SDK 34 installed
- Java 8 or higher

## Step 1: Setup Your Environment

### Download the Project
Download or clone this modernized project to your local machine with Android Studio installed.

### Configure Android SDK
Make sure you have Android SDK installed. If using Android Studio, it's usually auto-detected.

## Step 2: Configure YouTube API Key

### Get YouTube API Key
1. Go to [Google Cloud Console](https://console.developers.google.com/)
2. Create a new project or select existing one
3. Enable **YouTube Data API v3**
4. Go to Credentials → Create Credentials → API Key
5. **IMPORTANT**: Restrict your API key:
   - Application restrictions: Android apps
   - Add your package name: `com.surya.videos.TrendingVideos`
   - Add your SHA-1 fingerprint

### Add API Key to Project
Edit `local.properties` file in project root:
```properties
YOUTUBE_API_KEY=your_actual_api_key_here
```

## Step 3: Create Release Keystore

### Generate Keystore
Run this command in your project directory:
```bash
keytool -genkey -v -keystore release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias release
```

### Configure Signing in Build
Edit `app/build.gradle` and update the signing configuration:
```gradle
signingConfigs {
    release {
        storeFile file('release-key.jks')
        storePassword 'your_store_password'
        keyAlias 'release'
        keyPassword 'your_key_password'
    }
}

buildTypes {
    release {
        minifyEnabled true
        shrinkResources true
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        signingConfig signingConfigs.release
    }
}
```

## Step 4: Build the App

### Debug Build (for testing)
```bash
./gradlew assembleDebug
```
APK location: `app/build/outputs/apk/debug/app-debug.apk`

### Release Build (for Play Store)
```bash
# Clean build
./gradlew clean

# Generate signed AAB (recommended for Play Store)
./gradlew bundleRelease

# Or generate signed APK
./gradlew assembleRelease
```

**Output locations:**
- AAB: `app/build/outputs/bundle/release/app-release.aab` ⭐
- APK: `app/build/outputs/apk/release/app-release.apk`

## Step 5: Test Your App

### Before Upload
1. Install and test the release APK on real devices
2. Test all features:
   - Video search
   - Video playback
   - Internet connectivity
   - Different screen sizes

### Common Test Cases
- Search for videos
- Play videos (ensure YouTube Player works)
- Test on low-end and high-end devices
- Test in airplane mode (error handling)

## Step 6: Prepare Play Store Assets

### Required Assets
Create these for your Play Store listing:

**App Icon**
- 512x512 PNG
- High-resolution app icon

**Screenshots**
- At least 2 screenshots for each device type
- Phone: 16:9 or 9:16 ratio
- Tablet: 16:10 or 10:16 ratio

**Feature Graphic**
- 1024x500 PNG
- Displayed in Play Store

**App Description**
```
Short Description (80 chars max):
Browse trending YouTube videos with modern Android app

Full Description:
Discover and watch trending YouTube videos with our modern, fast, and intuitive app. 

Features:
• Browse trending videos
• Search YouTube content
• High-quality video playback
• Material Design interface
• Fast and responsive

Perfect for staying up-to-date with the latest viral content and trends.
```

## Step 7: Upload to Play Store

### Google Play Console Setup
1. Go to [Google Play Console](https://play.google.com/console)
2. Create developer account ($25 one-time fee)
3. Create new app

### Upload Process
1. **Upload AAB**: Use the `app-release.aab` file (recommended)
2. **App Details**: Fill in title, description, category
3. **Store Listing**: Add screenshots, icon, description
4. **Content Rating**: Complete questionnaire
5. **Target Audience**: Set age groups
6. **Privacy Policy**: Required if collecting data

### Release Configuration
1. **Internal Testing**: Upload and test first
2. **Closed Testing**: Test with limited users
3. **Open Testing**: Public beta (optional)
4. **Production**: Full release

## Step 8: Version Updates

### For Future Updates
1. Increment version in `app/build.gradle`:
   ```gradle
   versionCode 3  // increment this
   versionName "2.1"  // update this
   ```

2. Build new AAB:
   ```bash
   ./gradlew bundleRelease
   ```

3. Upload to Play Store Console

## 🛡️ Security Checklist

### Before Release
- ✅ API key is not hardcoded in source
- ✅ Keystore is secure and backed up
- ✅ ProGuard is enabled for release
- ✅ Network security config is in place
- ✅ App is tested on real devices

### Play Store Requirements
- ✅ Target SDK 34 (latest)
- ✅ 64-bit support (automatic with current setup)
- ✅ Privacy policy (if collecting data)
- ✅ App content rating completed

## 🚨 Important Notes

### Keystore Security
- **NEVER** commit keystore files to version control
- **BACKUP** your keystore safely - losing it means you can't update your app
- Store passwords securely

### API Key Security
- Never commit `local.properties` to git
- Consider using different API keys for debug/release
- Monitor API usage in Google Cloud Console

### Testing
- Test on actual devices, not just emulators
- Test with poor network conditions
- Test on different Android versions (API 24-34)

---

## ✅ Your App is Ready!

Following these steps will give you a production-ready app that meets all Google Play Store requirements and modern Android standards.

**Need Help?**
- Check the `MODERNIZATION_GUIDE.md` for technical details
- Review Android's [Publishing Guidelines](https://developer.android.com/distribute/best-practices/launch/)
- Visit Google Play Console help documentation