# InnerTune

<img src="https://raw.githubusercontent.com/z-huang/music/dev/app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png" height="72">

Make your own music library with any song from YouTube Music.  
No ads, free, and simple.

[<img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png" height="80">](https://apt.izzysoft.de/fdroid/index/apk/com.zionhuang.music)

[![Latest release](https://img.shields.io/github/v/release/z-huang/music?include_prereleases)](https://github.com/z-huang/music/releases)
[![License](https://img.shields.io/github/license/z-huang/music)](https://www.gnu.org/licenses/gpl-3.0)
[![Downloads](https://img.shields.io/github/downloads/z-huang/music/total)](https://github.com/z-huang/music/releases)

> **Note**
>
> **1.** The project is currently in an unstable stage, so there should be many bugs. If you encounter one, please report by opening an issue.
>
> **2.** The icon of this app is temporary. It will be changed in the future.

With this app, you're like getting a free music streaming service. You can listen to music from YouTube Music and build your own library. What's more, songs can be downloaded for offline playback. You can also create playlists to organize your songs. The aim of _InnerTune_ is to enable everyone to listen to music at no cost by an easy-to-use, practical and ad-free application.

> **Warning**
> 
>If you're in region that YouTube Music is not supported, you won't be able to use this app ***unless*** you have proxy or VPN to connect to a YTM supported region.

## Features

### YouTube

- Play songs without ads
- Browse almost any YouTube Music page
- Search songs, albums, videos and playlists from YouTube Music
- Open YouTube Music links

### Library

- Save songs, albums and playlists in local database
- Download music for offline playback
- Edit song title
- Add links to your favorite YouTube Music playlists

### Player

- Material design player
- Lockscreen playback
- Media controls in notification
- Skip to next/previous song
- Repeat/shuffle mode
- Edit now-playing queue

### Other

- Custom themes
- Dark theme
- Localization
- Proxy
- Backup & restore

## Screenshots

<p float="left">
  <img src="https://raw.githubusercontent.com/z-huang/music/dev/fastlane/metadata/android/en-US/images/phoneScreenshots/01.jpg" width="170" />
  <img src="https://raw.githubusercontent.com/z-huang/music/dev/fastlane/metadata/android/en-US/images/phoneScreenshots/02.jpg" width="170" />
  <img src="https://raw.githubusercontent.com/z-huang/music/dev/fastlane/metadata/android/en-US/images/phoneScreenshots/03.jpg" width="170" />
  <img src="https://raw.githubusercontent.com/z-huang/music/dev/fastlane/metadata/android/en-US/images/phoneScreenshots/04.jpg" width="170" />
</p>
<p float="left">
  <img src="https://raw.githubusercontent.com/z-huang/music/dev/fastlane/metadata/android/en-US/images/phoneScreenshots/05.jpg" width="170" />
  <img src="https://raw.githubusercontent.com/z-huang/music/dev/fastlane/metadata/android/en-US/images/phoneScreenshots/07.jpg" width="170" />
  <img src="https://raw.githubusercontent.com/z-huang/music/dev/fastlane/metadata/android/en-US/images/phoneScreenshots/08.jpg" width="170" />
  <img src="https://raw.githubusercontent.com/z-huang/music/dev/fastlane/metadata/android/en-US/images/phoneScreenshots/09.jpg" width="170" />
</p>

## Installation

You can install _InnerTune_ using the following methods:

1. Download the APK file from [GitHub Releases](https://github.com/z-huang/music/releases).
2. Add [IzzyOnDroid](https://apt.izzysoft.de/fdroid/index/apk/com.zionhuang.music) to your F-Droid repos following the [instruction](https://apt.izzysoft.de/fdroid/index/info), and you can search for this app and receive updates.
3. If you want to have bug fixes or enjoy new features quickly, install debug version from [GitHub Action](https://github.com/z-huang/music/actions) at your own risk and remember to backup your data more frequently.
4. Clone this repository and build a debug APK.

How to get updates?

1. F-Droid application.
2. [GitHub](https://github.com/z-huang/music)

## Contribution

### Contributing Translations

#### App

1. Have a fork of this project.
2. If you have Android Studio, right click on the `app/src/main/res/values` folder, select "New"->"Values Resource File". Input `strings.xml` as file name. Select "Locale", click ">>", choose your language and region, and click "OK".
3. If not, create a folder named `values-<language code>-r<region code>` under `app/src/main/res`. Copy `app/src/main/res/values/strings.xml` to the created folder.
4. Replace each English string with the equivalent translation. Note that lines with `translatable="false"` should be ignored.
5. (Recommended) Build the app to see if something is wrong.
6. Make a pull request with your changes. If you do step 5, the process of accepting your PR will be faster.

#### Fastlane (App Description and Changelogs)

Follow the [fastlane instruction](https://gitlab.com/-/snippets/1895688) to add your language and create a pull request.
