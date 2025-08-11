# TrendingVideos App - New Features Setup Guide

This guide will help you set up the new features added to the TrendingVideos app: Firebase OTP Authentication, Notifications, Favorites, and Video Resume functionality.

## New Features Added

1. **Firebase OTP Authentication** - Phone number-based authentication
2. **Push Notifications** - Real-time notifications for new videos
3. **Favorites System** - Save and manage favorite videos
4. **Video Resume Feature** - Continue watching from where you left off
5. **Category-based Filtering** - Filter videos by categories

## Prerequisites

- Android Studio Arctic Fox or later
- Google Firebase account
- YouTube Data API v3 key
- Google AdMob account (optional)

## Setup Instructions

### 1. Firebase Setup

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or select existing project
3. Add Android app to your Firebase project:
   - Package name: `com.surya.videos.TrendingVideos`
   - App nickname: "TrendingVideos"
   - Debug signing certificate SHA-1 (optional for now)
4. Download the `google-services.json` file
5. Replace the placeholder `google-services.json` in the `app/` directory with your actual file
6. Enable Authentication in Firebase Console:
   - Go to Authentication > Sign-in method
   - Enable Phone Number authentication
7. Enable Cloud Firestore:
   - Go to Firestore Database
   - Create database in test mode
8. Enable Cloud Messaging:
   - Go to Project Settings > Cloud Messaging
   - Note down the Server Key for sending notifications

### 2. YouTube API Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing project
3. Enable YouTube Data API v3
4. Create credentials (API Key)
5. Add the API key to your `local.properties` file:
   ```
   YOUTUBE_API_KEY=your_youtube_api_key_here
   ```

### 3. Build Configuration

The app is already configured with all necessary dependencies. Make sure to:

1. Sync your project with Gradle files
2. Clean and rebuild the project
3. Ensure all dependencies are downloaded

### 4. Notification Setup

The app automatically handles notification permissions for Android 13+ (API 33+). For older versions, notifications will work without explicit permission requests.

### 5. Database Setup

The Room database is automatically created when the app runs for the first time. No manual setup required.

## Features Usage

### Authentication
- Users will be prompted to enter their phone number
- OTP will be sent via SMS
- After verification, users can access the app

### Favorites
- Tap the heart icon on any video to add/remove from favorites
- Access favorites from the main menu
- Filter favorites by category using the chip group

### Video Resume
- Video progress is automatically saved while watching
- Resume from where you left off when reopening a video
- Progress is stored locally on the device

### Notifications
- Users receive notifications for new videos in their preferred categories
- Notifications can be tapped to open the specific video or category
- Notification preferences can be managed in device settings

## Security Considerations

1. **API Keys**: Never commit API keys to version control
2. **Firebase Rules**: Configure proper Firestore security rules
3. **YouTube Terms**: Ensure compliance with YouTube API terms of service
4. **User Data**: Handle user data according to privacy regulations

## YouTube API Compliance

The app follows YouTube API terms of service:
- Uses official YouTube Data API v3
- Displays video metadata properly
- Includes proper attribution
- Respects rate limits
- No content downloading or modification

## Troubleshooting

### Common Issues

1. **Firebase Authentication Fails**
   - Check if phone number format is correct
   - Ensure Firebase project is properly configured
   - Verify `google-services.json` is in the correct location

2. **Notifications Not Working**
   - Check notification permissions in device settings
   - Verify FCM token is being generated
   - Ensure Firebase Cloud Messaging is enabled

3. **Database Errors**
   - Clear app data and reinstall
   - Check if device has sufficient storage
   - Verify Room database migrations

4. **YouTube API Errors**
   - Check API key validity
   - Verify API quota limits
   - Ensure proper API key restrictions

### Debug Mode

To enable debug logging, add this to your `local.properties`:
```
DEBUG_MODE=true
```

## Support

For issues or questions:
1. Check the troubleshooting section above
2. Review Firebase and YouTube API documentation
3. Ensure all setup steps are completed correctly

## License

This project follows the same license as the original TrendingVideos app. Please ensure compliance with YouTube API terms of service and Firebase usage policies.
