================================================================================
MelodyMart - Local Audio Track Storage
================================================================================

This directory contains local MP3 audio files for MelodyMart track playback.

File & URL Mapping Architecture:
--------------------------------
Physical file:
backend/src/main/resources/static/assets/audio/song1.mp3

Database:
TRACK.AudioFileURL = /assets/audio/song1.mp3

Browser:
http://localhost:8080/assets/audio/song1.mp3

Instructions:
- Place valid MP3 files in this folder (e.g. song1.mp3, 01_sanda_eliya.mp3).
- In Admin Track Management, set Audio File URL to /assets/audio/<filename>.mp3
- Album details and playlist views will render HTML5 audio players for playback.
