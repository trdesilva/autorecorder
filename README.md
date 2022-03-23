# Autorecorder
This is a Java application that automates OBS Studio and FFMpeg on Windows to record game sessions and create highlight clips. It can also upload clips to YouTube.

# Setup
1. Install [OBS Studio](https://obsproject.com/download) and configure it however you like. Autorecorder will launch OBS Studio and start a new recording when it detects that a game has launched.
2. Install some kind of Java Runtime Environment that supports Java 11. This project was developed on [OpenJDK 11 for x64 Windows](https://adoptium.net/?variant=openjdk11), so that should work.
3. Clone this repo and open with IntelliJ to resolve dependencies, or [download the automatic updater](https://github.com/trdesilva/autorecorder/releases/download/v1.4.0/updater.jar).
4. Run the updater JAR to generate the launcher script in %LOCALAPPDATA%\autorecorder. (If you cloned the repo, you can skip this step and the next one because you can build Autorecorder yourself.)
5. Run the launcher script. The launcher will download and run the latest Autorecorder release. You can make Windows run the launcher on startup if you want to update and launch Autorecorder when you log in to Windows.
6. Edit your Autorecorder settings to tell it where to find OBS, and where your recordings and clips will be stored. You can do this through Autorecorder itself or by editing the JSON file it generates in %LOCALAPPDATA%.
7. Put a Google OAuth client secret JSON file at %LOCALAPPDATA%\autorecorder\client_secret.json. If you're forking the project, you'll need to get your own client secret and make sure it can request tokens for the `youtube.upload` scope in the YouTube Data API.
8. Leave Autorecorder running whenever you want it to record your game sessions.

# FAQ

## Why should I use Autorecorder?
Autorecorder makes it easy to record, upload, and share your gameplay highlights by being focused on usability. It's built on existing robust and highly-available software, so it's reliable and takes relatively little effort to maintain. Also, its UI was built to be convenient rather than pretty, so it's simple to use.

## Why should I use Autorecorder over one of those other apps?
There have been a number of applications for automatically recording gameplay and sharing highlight clips, but they're often unreliable because they try to do too much themselves. They usually try to do their own video hosting without a monetization model that works well enough, which means that users lose all their clips when the app inevitably folds. Autorecorder leverages OBS Studio for recording, FFMpeg for clip editing, and YouTube for video hosting, all of which are mature and well-tested technologies; this greatly reduces the potential for bugs. Also, since all user data is stored locally or on YouTube, there's no way for user data to be lost as a result of Autorecorder's development being stopped. Finally, because Autorecorder is open-source, users can freely view and edit the code that they're running if they don't trust it or want to change something. Autorecorder is much lower risk than the similar commercial apps, and largely as capable.

## What do I have to agree to in order to use it?
You can find Autorecorder's terms of use (including its privacy policy) [here](https://trdesilva.github.io/autorecorder/license). Autorecorder is published under the GNU Public License.
