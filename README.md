# Music

![Icon](app/src/main/res/mipmap-hdpi/ic_launcher_round.png)

Make your own music library with any song on YouTube/YouTube Music.  
No ads, free, and simple.

[<img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png" height="80">](https://apt.izzysoft.de/fdroid/index/apk/com.zionhuang.music)

[![Latest release](https://img.shields.io/github/v/release/z-huang/music?include_prereleases)](https://github.com/z-huang/music/releases)
[![License](https://img.shields.io/github/license/z-huang/music)](https://www.gnu.org/licenses/gpl-3.0)
[![Downloads](https://img.shields.io/github/downloads/z-huang/music/total)](https://github.com/z-huang/music/releases)

> **Note:** The project is currently in an unstable stage. If you encounter bugs, please report by opening an issue.

## Description

_Music_ uses [NewPipe Extractor](https://github.com/TeamNewPipe/NewPipeExtractor) to retrieve information and stream data from YouTube/YouTube Music. It's a music player as well, so you can create your own playlists and organize your songs by the artists you create. The aim of _Music_ is to enable everyone to listen to music at no cost by an easy-to-use, practical and ad-free application.

## Features

### YouTube

- No ads
- Search songs, videos, playlists and channels from YouTube/YouTube Music
- auto load more songs when playing the last 5 songs in queues from YouTube

### Player

- Material design player
- Lockscreen playback
- Media controls in notification
- Skip to next/previous song
- Repeat/shuffle mode
- Edit now-playing queue

### Library

- Play and save songs from YouTube/YouTube Music
- Download music for offline playback
- Edit song name and song artist
- Create playlists in local database

## Screenshots

<p float="left">
  <img src="https://raw.githubusercontent.com/z-huang/music/master/screenshots/main.jpg" width="170" />
  <img src="https://raw.githubusercontent.com/z-huang/music/master/screenshots/artists.jpg" width="170" />
  <img src="https://raw.githubusercontent.com/z-huang/music/master/screenshots/playlists.jpg" width="170" />
  <img src="https://raw.githubusercontent.com/z-huang/music/master/screenshots/explore.jpg" width="170" />
</p>
<p float="left">
  <img src="https://raw.githubusercontent.com/z-huang/music/master/screenshots/player.jpg" width="170" />
  <img src="https://raw.githubusercontent.com/z-huang/music/master/screenshots/notification.jpg" width="170" />
  <img src="https://raw.githubusercontent.com/z-huang/music/master/screenshots/now playing queue.jpg" width="170" />
</p>
<p float="left">
  <img src="https://raw.githubusercontent.com/z-huang/music/master/screenshots/search.jpg" width="170" />
  <img src="https://raw.githubusercontent.com/z-huang/music/master/screenshots/search results.jpg" width="170" />
  <img src="https://raw.githubusercontent.com/z-huang/music/master/screenshots/settings.jpg" width="170" />
</p>

## Roadmap

The overall plan for this project at current stage:
1. Improve user interface and migrate to Material You
2. Modify [NewPipe Extractor](https://github.com/TeamNewPipe/NewPipeExtractor) to support more album and artist information and audio normalization

## Installation

You can install _Music_ using the following methods:

1. Download the APK file from [GitHub Releases](https://github.com/z-huang/music/releases).
2. Add [IzzyOnDroid](https://apt.izzysoft.de/fdroid/index/apk/com.zionhuang.music) to your F-Droid repos following the [instruction](https://apt.izzysoft.de/fdroid/index/info), and you can search for this app and receive updates.
3. Go to [GitHub Action](https://github.com/z-huang/music/actions) and download the APK artifact of any workflow.
4. Clone this repository and build a debug APK.

How to get updates?

1. If you install from [GitHub Releases](https://github.com/z-huang/music/releases), the app already has a built-in updater.
2. If you install from method 2, you can check for updates using the F-Droid application.
3. Or else, visit [GitHub](https://github.com/z-huang/music) and checkout the releases, issues, PRs, or anything new.