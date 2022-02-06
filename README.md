# Autorecorder
This is a Java tool that automates OBS Studio and FFMpeg on Windows to record game sessions and create highlight clips.

# Setup
1. Install [OBS Studio](https://obsproject.com/download) and configure it however you like. Autorecorder will launch OBS Studio and start a new recording when it detects that a game has launched.
2. Clone this repo and open with IntelliJ to resolve dependencies.
3. Run Autorecorder.
4. Edit your Autorecorder settings to tell it where to find OBS, and where your recordings and clips will be stored. You can do this through Autorecorder itself or by editing the JSON file it generates in %LOCALAPPDATA%.
5. Leave Autorecorder running whenever you want it to record your game sessions.