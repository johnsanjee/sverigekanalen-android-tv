# Sverigekanalen Android TV App

## Features

- **HLS Stream Playback**: 
  - Streams live video using **ExoPlayer**, supporting adaptive bitrate and smooth playback on varying network conditions.
  
- **Cloudflare Worker Integration**: 
  - Dynamically fetches the HLS stream URL from a **Cloudflare Worker**, allowing easy updates without app changes.
  
- **URL Caching**: 
  - Caches the HLS URL locally using **SharedPreferences**, reducing API calls and improving playback speed.
  
- **Automatic Retry**: 
  - Automatically retries fetching the URL or during playback issues to ensure uninterrupted viewing.
  
- **ExoPlayer Controls**: 
  - Provides **media controls** (play, pause, seek, volume) optimized for Android TV navigation with a remote control.
  
- **Android TV Optimization**: 
  - Tailored for **Android TV** with a clean interface and TV-optimized components for a seamless viewing experience.
  
## Setup

1. Clone this repository.
2. Open in **Android Studio**.
3. Update the Cloudflare Worker URL in the `MainActivity.kt` if necessary.
4. Run on an **Android TV** device or emulator.

## License

This project is licensed under the MIT License.
