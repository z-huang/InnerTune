# Music

<img src="https://raw.githubusercontent.com/z-huang/music/dev/app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png" height="72">

Make your own music library with any song on YouTube/YouTube Music.  
No ads, free, and simple.

[<img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png" height="80">](https://apt.izzysoft.de/fdroid/index/apk/com.zionhuang.music)

[![Latest release](https://img.shields.io/github/v/release/z-huang/music?include_prereleases)](https://github.com/z-huang/music/releases)
[![License](https://img.shields.io/github/license/z-huang/music)](https://www.gnu.org/licenses/gpl-3.0)
[![Downloads](https://img.shields.io/github/downloads/z-huang/music/total)](https://github.com/z-huang/music/releases)

> **Note 1:** The project is currently in an unstable stage, so there should be many bugs. If you encounter one, please report by opening an issue.

> **Note 2:** The name of this app is temporary. It will be changed in the future.

> **Note 3:** We are currently making a change about the YouTube library. The development is in `feature/innertube` branch. Issues are put on hold until `feature/innertube` gets merged into `dev` branch.
## Description

With this app, you're like getting a free music streaming service. You can listen to music from YouTube/YouTube Music and build your own library. What's more, songs can be downloaded for offline playback. The metadata of songs and artists are fully editable. You can also create playlists to organize your songs. The aim of _Music_ is to enable everyone to listen to music at no cost by an easy-to-use, practical and ad-free application.

The ability to retrieve information and stream data from YouTube/YouTube Music is provided by [NewPipe Extractor](https://github.com/TeamNewPipe/NewPipeExtractor).

## Features

### YouTube

- No ads
- Search songs, albums, videos, playlists and channels from YouTube/YouTube Music
- Auto load more songs when playing the last 5 songs in queues from YouTube

### Library

- Play and save songs from YouTube/YouTube Music
- Download music for offline playback
- Edit song and artist metadata
- Create playlists in local database

### Player

- Material design player
- Lockscreen playback
- Media controls in notification
- Skip to next/previous song
- Repeat/shuffle mode
- Edit now-playing queue

## Screenshots

<p float="left">
  <img src="https://raw.githubusercontent.com/z-huang/music/dev/screenshots/main.jpg" width="170" />
  <img src="https://raw.githubusercontent.com/z-huang/music/dev/screenshots/playlists.jpg" width="170" />
  <img src="https://raw.githubusercontent.com/z-huang/music/dev/screenshots/player.jpg" width="170" />
  <img src="https://raw.githubusercontent.com/z-huang/music/dev/screenshots/now_playing.jpg" width="170" />
</p>
<p float="left">
  <img src="https://raw.githubusercontent.com/z-huang/music/dev/screenshots/search.jpg" width="170" />
  <img src="https://raw.githubusercontent.com/z-huang/music/dev/screenshots/search results.jpg" width="170" />
  <img src="https://raw.githubusercontent.com/z-huang/music/dev/screenshots/settings.jpg" width="170" />
</p>

## Installation

You can install _Music_ using the following methods:

1. Download the APK file from [GitHub Releases](https://github.com/z-huang/music/releases).
2. Add [IzzyOnDroid](https://apt.izzysoft.de/fdroid/index/apk/com.zionhuang.music) to your F-Droid repos following the [instruction](https://apt.izzysoft.de/fdroid/index/info), and you can search for this app and receive updates.
3. To get a dev build, go to [GitHub Action](https://github.com/z-huang/music/actions) and download the APK artifact of any workflow.
4. Clone this repository and build a debug APK.

How to get updates?

1. If you install from [GitHub Releases](https://github.com/z-huang/music/releases), the app already has a built-in updater.
2. If you install from method 2, you can check for updates using the F-Droid application.
3. Or else, visit [GitHub](https://github.com/z-huang/music) and checkout the releases, issues, PRs, or anything new.

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