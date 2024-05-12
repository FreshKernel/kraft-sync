# Minecraft Sync Script ⛏️🔄

A **experimental** lightweight & simple and quick script that help you to sync the content of a minecraft java instance

Let's say you're running a Minecraft server and every time you add, remove, update a mod, resource-pack, shader, each
player have to do the changes manually each time you do it, or let's say you're changing the server address because, the
players have to manually change this

it's designed mostly for server owners but can work for mod-pack developers, the script will run before each time when
launching the game, will make a `GET` request to a file you upload to somewhere (GitHub for example) and it contains
information about the sync info like the mods and some other configurations that allow you to customize the behavior,
you don't need to upload the mods to anywhere it will simply need a public download url for the mod
(For example CurseForge or Modrinth) and it will download the mod from there

You don't have to do that manually, see [Launcher Data Converter](#launcher-data-converter) for more info

Currently, the script will sync the followings:

1. 🛠️ Mods (experimental)
2. 🎨 Resource-packs (not finished yet)
3. ✨ Shader-packs (not finished yet)
4. 🌐 Server List (in-game) (not finished yet)
5. 🧩 Mods configurations (not finished yet): we might sync them for the first time because each player might have their
   own configurations and until now, we have not discovered a way to solve this, unless if you want to force all players
   to have the same mods settings
6. ⌨️ Keybindings (not finished yet): we might sync them for the first time because each player might have their
   own key-binds and until now, we have not discovered a way to solve this, unless if you want to force all players
   to have the same keybindings

The script might introduce **Breaking Changes** as it's still in early state, but we will provide migration instructions
if there is any breaking change in each release

## Table of Contents

- [Features](#features) ✨
- [How it works?](#how-it-works) 🔧
- [Usage️](#usage) 🖥
- [Frequently Asked Questions](#frequently-asked-questions) 💬
- [Launcher Data Converter](#launcher-data-converter) 💾
- [Build from Source️](#build-from-source) 🛠
- [Contributing](#contributing) 🤝
- [Acknowledgments](#acknowledgments) 📜

## Features

List the key features of the script

```markdown

## Features

- 🛠️ Easy to use and customizable.
- 🔍 Different ways to validate the resources (mods etc...) or completely disable it.
- 🖥️ Simple GUI for download indicators and errors (can be disabled).
- 🚫 Handles different errors.
- 🌐 Multiplatform: Works using JVM, which means you don't need different binaries for different desktop platforms.
- 📦 Relatively small bundle size (compared to how the JVM ecosystem works).
- ⚙️ Compatibility with Java 11 and newer versions, thanks to Kotlin/JVM allowing us to use most of the recent language
  features.
- 📜 Completely open-source and documented code.
- 🔌 No need to restart the game after syncing the data; it will work once you open the game.
- 🛡️ No need for a server-side mod to be installed on your Fabric/Forge server.
- 🧩 You define what to be synced and what not to.
- 🔄 You can use the exact same script for syncing server mods (if you have access to running JAR files) and you can
  exclude or include some mods to be downloaded on the client or server.
- 💾 Easily convert all the mods data from other launchers.

```

## How it works?

First of all, are you not going to send this script to all the players? We would appreciate it if you do.
However, to provide an easy way to use for the players without doing anything manual, all you need is to use a
launcher [MultiMC Launcher] or [ATLauncher] which provide more features than the original
[Minecraft Launcher], Features like:

1. Exporting the instance, each instance has its own mods and resources (resource-packs
   and shaders)
2. Configurations and data in a way so no instance will affect another
3. Other features like allow to launch
   Pre-launch
   command which is needed by this script to automate the process, otherwise if you're using the original minecraft
   launcher,
   then the players need to run the script each time they launch the game or only when they want to (when new mods added
   etc...)
4. Many other features, like automatically joining a minecraft server on instance launch

You will need to ship the script with the exported instance, configure the instance to launch the script before each
game launch, and export it. It's important to check the script files; otherwise, it won't be shipped. The process is
simple and easy to use; don't get confused by the required steps.

## Usage

To use the script 🚀

This section has not been started yet, please come back later.

[//]: # (TODO: Complete this section)

## Frequently Asked Questions

This FAQ provides answers to some of the most commonly asked questions. Click on the question to expand and see the
answer.

[//]: # (Questions start)

**How to use this FAQ?**

* Click on any question below to expand and reveal the answer.
* You can use the search functionality of your browser (Ctrl+F or Cmd+F) to find specific keywords within the questions
  and answers.

**List of Questions:**

1. [**Why not sync the mods on the server?**](#q1)
2. [**Does it sync the Minecraft version, Mod loader and Java version?**](#q2)
3. [**Is there a GUI for admins?**](#q3)
4. [**I have an issue/bug or feature request**](#q4)
5. [**What happens to the old script?**](#q5)

**Answers:**

<details>
<summary id="q1">Why not sync the mods on the server?</summary>
There are some mods that is required to be installed on both client and server side
and it will simply sync the mods when you join a server by downloading the mods from the minecraft server
while those mods work great and might require even more efforts than this script, and depending on your use-case,
you might to use them or use this script

some of the common issues in the  `minecraft mods` using sync mods:

1. It will require running http server on port other than the minecraft port (e.g., 25565) which can be used by
   attackers
   to cause performance issues if you haven't implemented Rate Limit (otherwise some users might spam the server)
   and other security mechanism it's might be affect the network and traffic from your minecraft hosting
2. You will constantly need to update the mod for Forge/Fabric or the mod loader you are using, and for a specific
   minecraft version,
   while this script work independently of the minecraft version and the mod loader
3. The users have to update the mods when joining the server, then restarting the game, so let's say a new minecraft
   version has been released, and you were playing with your friends, you will have to wait for the mod that sync the
   mods
   to be updated or update it yourself, then ask your friends to restart the game with the new version and manually
   update
   the sync mod, then you will have to update all the mods on the server, after that when they launch the game
   once they launch the game they will have to update and then restart once again, when using the script, you only have
   to
   update the data as admin, then ask your friends and players to restart the game
4. Limited to minecraft mods only (which you might exactly what you want)
5. Server and client side mods are different: There are completely server side mods
   like [Geyser](https://modrinth.com/mod/geyser)
   or [Spawn Animations](https://modrinth.com/datapack/spawn-animations) and including them in the client side will
   increase
   the size and require the players to download more, for example Geyser is above
   10MB, Another client side mod [Physics Mod](https://modrinth.com/mod/physicsmod) which is more than 130MB but when
   using more and more mods than not only the syncing process will be slower, but it will also require more space on the
   disk and in
   some
   rare cases it could cause issues (if the server side mod will cause an exception when running on client side) or it's
   not
   marked as server side mod
6. Uploading minecraft developer mods and downloading them to the players from external source other than the original
   might be against the LICENSE
7. You might have large client side mods like [Physics Mod](https://modrinth.com/mod/physicsmod) which might not be
   needed
   in the server mod

Also, there is an idea which is a mix of using a mod (server side mod) and a script, but when the mods get larger you
will
have some other issues

</details>

<details>
<summary id="q2">Does it sync the Minecraft version, Mod loader and Java version?</summary>
Right now, the script won't download or sync the java version, because the launcher you are using (MultiMc or ATLauncher)
or even the official minecraft launcher already have java JRE installed, the script will use that installed java instead of
requiring the players to download and install java on their system, launchers like official minecraft launcher and ATLauncher
will automate the process of updating and installing the required java version for a specific minecraft version

As for the minecraft version, unfortunately we currently don't sync it as some launchers
might store such information, and we can't update and cover all the launchers, breaking changes might be introduced, and
the goal of this script is to make it simple and work for most use cases, if there is a new minecraft version, the
player
need to update the minecraft version along with the mod loader manually, launching the game to update the resources and
mods
then they can get back to playing the game

for the mod loader, we might add a feature that will display a warning or require the player to update the mod loader
version
using their launcher or manually download it from the web, or we might automate this process but until now we don't have
plans
</details>

<details>
<summary id="q3">Is there a GUI for admins?</summary>
For now, we don't have a gui, but we might create a simple web interface where you can input some data and you will either
get an url to use in the script or just the raw json data, and you will need to upload it somewhere

We still haven't planned for some things, like should we add authentication and store the data on the server
so each admin can access their multiple servers and update the resources and mods, or we should only provide
add and edit functionalities, for the add you will insert all the data, for the edit you will provide the raw json data
and then edit it in GUI and get the new one

The GUI should allow selecting mods and resources from some providers like Modrinth, CurseForges or by custom url

we might use [Kobweb](https://github.com/varabyte/kobweb) for the GUI which is also in Kotlin which allow us to share
code
and logic between Kotlin/JS and Kotlin/JVM
</details>

<details>
<summary id="q4">I have an issue/bug or feature request</summary>
Please file an issue in the issues tab with detailed information as much as you can, with the error and what are you trying to do
and how to reproduce it, and we will do our best to help you or fix it

if you have a question, use the Discussions tab instead
</details>

<details>
<summary id="q5">What happens to the old script?</summary>
We have an older version of this script called MC Mods Sync which is limited to syncing the mods only, requiring the admin
to upload the mods on GitHub and then downloading them, other than many issues we discussed like storage limitation and LICENSE
issues, it's easier to set up and maintain but less flexible and simpler
</details>

[//]: # (Questions end)

**We hope this FAQ helps!**

If you have any further questions that are not covered here, please feel free to file an issue or create a new
discussion.

## Launcher Data Converter

Because right now we don't have a UI or maybe a web application for selecting, updating, search, configuring
all the mods for example and give you the data to use, as right now you will have to manually deal with json and getting
the info you need like the url of the mod in the json, or the file integrity info, which can be very time-consuming
especially if you have large amount of mods, we provided an alternative for **converting the data from other launchers**

The supported and tested launchers are:

- [ATLauncher]
- [Prism Launcher]

So if you have mods installed in your instance in one of the above launchers, you can convert it the data in automated
way, the script will get information like the hashing, the mod side (if it's server or client side) or the mod url
(required) and all the available information like the mod name

if you have a launcher that have downloading mods directly from the launcher, and it's not supported in this list, feel
free to submit a Feature request in the issues tab

This section is not complete yet and this tool is experimental and can have many breaking changes

[//]: # (TOOD: Complete this section)

## Build from Source

If you want to build from source for the latest version:

1. Install the [Requirements]
2. Run `./gradlew build`
3. Now go to `build/libs` and in there you should fine the jar

The script written in [Kotlin/JVM](https://kotlinlang.org/docs/jvm-get-started.html#create-an-application)

## Contributing

First of all, we would like to thank you for your time and efforts on this project, we appreciate it

But there should be some guidelines that must followed for consistency
and maintainability and more so most developers can understand the code when possible.
See [Contributing] for more details.

## Acknowledgments

- Thanks to the welcoming community
- Thanks for all the volunteers who helped people along the journey
- Thanks to the all developers & contributors and maintainers who put time and efforts for making an easy-to-use
  libraries
- Thanks to [JetBrains](https://www.jetbrains.com/)
  for [Kotlin](https://kotlinlang.org/), [IntelliJ IDEA Community Edition](https://www.jetbrains.com/idea/) in along to
  other projects
- Thanks to [Square Community](https://square.github.io/) for [OkHttp](https://square.github.io/okhttp/)
- Thanks to [JoeEnderman](https://opengameart.org/content/apple-16x-minecraft-style) for the script icon
- The list can be much longer, we would like to thank everyone

[//]: # (Links)

[ATLauncher]: https://atlauncher.com/

[Prism Launcher]: https://prismlauncher.org/

[MultiMC Launcher]: https://multimc.org/

[Minecraft Launcher]: https://www.minecraft.net/download

[Contributing]: ./CONTRIBUTING.md

[Requirements]: ./CONTRIBUTING.md#requirements