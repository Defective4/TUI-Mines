<img alt="logo" height="128" src="img/logo.png" width="128"/>

[Releases]() | **[Installation]()** | [Share your theme/replay]()

# ![info](img/info.png) Description
TUI Sweeper is a game created for the Java Jam hosted at [Java Community](https://discord.com/invite/X3NmMgzFKF) Discord server.  
It's a Minesweeper clone designed to run entirely in terminal.  
The game also has its own terminal emulator that can be used as an alternative.

# ![installation](img/install.png) Installation
|    Contents    |
|:--------------:|
|  Requirements  |
|    Windows     |
|     Linux      |
|     Other      |

## Requirements
On all platforms the Java Runtime Environment is required to run the TUI Sweeper.  
The lowest required version of JRE is **1.8**.  
The game was tested on all JRE versions from `1.8` to `17` inclusive.  
The game was **not** tested on a headless JRE

## Windows
To play TUI Sweeper on Windows it is recommended to download the executable file (.exe) from the [Releases page]().  
Alternatively you can download the universal JAR file.

## Linux
### (Recommended) Install from Debian repository
You can download the game from my Debian repository.
To do so:

1. Add the repository to your system
```shell

```

2. Install TUI Sweeper using APT
```shell
sudo apt-get install tui-sweeper
```

### Manual installation
To install TUI Sweeper without adding any repositories you can download the Debian package (or universal JAR if you don't want to install/don't have root privileges) from the [Releases page]().

## Other
TUI Sweeper should be able to run on any platform that can run Java applications.  
It was confirmed to run in Termux on Android.

# ![share](img/share.png) Share your theme or replay
### [User assets repository](https://github.com/Defective4/TUI-Sweeper-Repo)
You can share your themes and replays in a separate repository (see link above).  
Instructions on how to share are also included.  
All shared assets are available for viewing and downloading using **in-game browser**.

# ![cogs](img/cogs.png) Technical information
TUI Sweeper uses its own file format for storing replays.  
It is documented [here](Replay%20format.md)