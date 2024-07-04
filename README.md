# Kraft Sync Script ⛏️🔄

![GitHub Release](https://img.shields.io/github/v/release/ellet0/kraft-sync)
![GitHub Pre-Release](https://img.shields.io/github/v/release/ellet0/kraft-sync?include_prereleases&label=pre-release)
![License](https://img.shields.io/github/license/ellet0/kraft-sync)
![Build and test](https://github.com/ellet0/kraft-sync/actions/workflows/build.yml/badge.svg?branch=main)
![GitHub repo size](https://img.shields.io/github/repo-size/ellet0/kraft-sync)
![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/ellet0/kraft-sync)
![JAR Size](https://img.shields.io/badge/JAR_Size-2.46_MB-blue)
![Obfuscated JAR Size](https://img.shields.io/badge/Minimized_JAR_Size-1.59_MB-blue)
![GitHub Downloads (all assets, all releases)](https://img.shields.io/github/downloads/ellet0/kraft-sync/total)
![GitHub Repo stars](https://img.shields.io/github/stars/ellet0/kraft-sync)

AN **experimental** lightweight & simple and quick script that helps you to sync the content of a **Minecraft Java
instance**.

The script will sync the following:

1. 🛠️ Mods (experimental)
2. 🎨 Resource packs (highly experimental)
3. ✨ Shader-packs (not implemented yet)
4. 🌐 Server List (in-game) (not implemented yet)
5. 🧩 Mods configurations (not implemented yet): These may be synced initially, since each player can have their own
   configurations.
   We have not yet found a solution, except by enforcing the same mod settings for all players.
6. ⌨️ Keybindings (not implemented yet): These may be synced initially since each player can have their own
   keybindings.
   We have not yet found a solution for this, except by enforcing the same keybindings for all players

If you're a player interested in joining a server, **make sure you trust the server administration** on this
matter—unless you're confident you know what you're doing.

![Screenshot](https://github.com/ellet0/kraft-sync/assets/73608287/fc645eee-62a7-4f43-89ef-98677524446e)

The script might introduce **Breaking Changes** as it's still in the early stages. We will provide migration
instructions if there is any breaking change in each release. Some features might not be available yet, or unstable.

**Kraft Sync is not affiliated
with [Mojang AB](https://mojang.com/), [Microsoft Corporation](https://www.microsoft.com/), or any of their
subsidiaries.**

## 📚 Table of Contents

- 📖 [About](#-about)
- ✨ [Features](#-features)
- 🔧 [How it works?](#-how-it-works)
- 🖥 [Usage️](#-usage)
- 💬 [Frequently Asked Questions](#-frequently-asked-questions)
- 🔰 [Admin Utility](#-admin-utility)
- 🛠 [Build from Source️](#-build-from-source)
- 🤝 [Contributing](#-contributing)
- 📜 [Acknowledgments](#-acknowledgments)

## 📖 About

Let's say you're running a Minecraft server and every time you add, remove, or update a mod, resource pack, or shader,
each player has to make the changes manually each time you do it or let's say you're changing the server address, the
players have to update it in the game manually.

It's designed for server owners, yet it can also be effective for mod-pack developers and other use cases.
The script will run before each time when launching the game will make a `GET` request to a file you upload somewhere
(GitHub for example) and it contains information about the sync information like the **Mods** and other
configurations that allow you to customize the behavior, you don't need to upload the mods anywhere it will need
a public download URL for the mod (e.g, **Modrinth**) and it will download the mod directly from there.

You don't have to do that manually, see [Mods Info Converter] 💾 for more
info.

> Kraft Sync
> <img src="common/src/main/resources/apple.png" height="30px" align="center"/> <br>
> The meaning of this name:
> - `K`: will depend on how you look at it, it could be **Keep**, which means keep a Minecraft instance synced
> - `Craft` or `Kraft`: Indicating this is specific for Minecraft<br>
> - `Sync`: Indicating it's for syncing the content/assets/data

## ✨ Features

List the key features of the script

```markdown

## Features

- 🛠️ Easy to use and customizable.
- 🔍 Different ways to validate the assets (e.g., Mods, Resource-packs) or disable the file integrity validation.
- 🖥️ Simple GUI for download indicators and errors for both dark and light with different themes (can be disabled).
- 🚫 Handles different errors.
- 🌐 Multiplatform: Works using JVM, which means you don't need different binaries for different desktop platforms.
- 📦 Relatively small bundle size.
- ⚙️ Compatibility with Java 11 and newer versions
  features.
- 📜 Completely open-source and documented code.
- 🔌 No need to restart the game after syncing the data; it will work once you open the game.
- 🛡️ No need for a server-side mod to be installed on your Fabric/Forge server.
- 🧩 You define what to be synced and what not to.
- 🔄 You can use the exact same script for syncing server mods (if you have access to running JAR files) and you can
  exclude or include some mods to be downloaded on the client or server.
- 💾 Easily convert the info of the mods from other launchers.
- ⚡ Support for quick play feature for automatically joining a Minecraft server on launch (limited to supported
  launchers)

```

## 🔧 How it works?

To provide an easy way to use for the players without doing anything manually, all you need is to use a
Minecraft launcher that supports running a command before launching the game

You can use [MultiMC Launcher] or [Prism Launcher] which provide more features than the original
[Minecraft Launcher], features like:

1. Exporting the instance, each instance has its assets (e.g., Mods, Resource-packs, Shader-packs)
2. Configurations and data in a way so no instance will affect another
3. Other features like allow to launch
   `Pre-launch` command is needed by this script to automate the process, otherwise if you're using the
   official [Minecraft Launcher], then the players need to run the script each time they launch the game or only when
   they want to (when new mods are added etc...) or you could create a `bat` (Windows) or `sh` (Linux, macOS) script
   that launches this script first then the launcher, This way the players are forced to use your mods and resource
   packs, etc... even if they don't want to use the mods, still [Minecraft Launcher] doesn't provide a way to separate
   the mods and settings for different instances and servers, to get the best experience, use one of the supported
   launchers or a launcher that supports running a command before launching the game
4. Many other features, like automatically joining a Minecraft server when launching the game

You will need to ship the script with the exported instance and configure the instance to launch the script before each
game launch and export it.
It's important to check the script files; otherwise, it won't be shipped.

The process is straightforward; don't get confused by the required steps.

## 🖥 Usage

[//]: # (TODO: Add supported launchers section)

[//]: # (TODO: Share the common steps between the manual and automated way)
Use the manual or automated method to get started 🚀:

<details>
<summary>The automated way to use the script</summary>

1. Download the JAR file from the [Releases] for both the script and the [Admin Utility](#-admin-utility)
2. Download and install your favorite Minecraft launcher that has built-in support for downloading mods inside the
   launcher, and hooks/custom commands feature.
3. **Create a new instance**, choose the version, and mod loader, and download the mods you want to use.

   Consider using [Modrinth] **as an asset provider** to make the process easier and faster with a smaller possibility
   of
   errors for the utility as it will be an offline task
4. Use the [Admin Utility] to convert the mod info from the launcher to the script format, and upload the new file
   you get from the admin application to somewhere public, like [GitHub](https://github.com/)
5. To prepare the instance that will be used by the players, **create a new instance**, choose the same version
   and mod loader as in the previous instance while refraining from downloading the mods.
   This instance will be configured to use the script.

   The previous one will be used by [Admin Utility] to convert the mods' info to the script format.
6. Use the [Admin Utility] to install the script into the new instance, navigate
   to [Admin Script Installer](./admin/README.md#-script-installer) for details.
7. Now launch the instance to run the game, if this is the first time, it will ask you for the URL
   from **Step 4**.
   Enter it and then wait for the sync process to finish, the game will launch with the new synced
   content.
8. To make this process easier for all the players, export the instance from **Step 5** that's configured
   to use the script, make sure to include the `kraft-sync.jar` file and the `kraft-sync-data` folder
   and exclude the content that will be synced like the `mods` as the script will download them once the player import
   the instance and launch the instance, you can include them though, the script will sync them if they're outdated.

</details>

<details>
<summary>The manual way to use the script</summary>

1. Download the JAR file from the [Releases] for both the script and the [Admin Utility](#-admin-utility)
2. Download and install your favorite Minecraft launcher that has built-in support for downloading mods inside the
   launcher.

   Consider using [Modrinth] **as asset provider** to make the process easier and faster with a smaller possibility of
   errors for the utility as it will be an offline task
3. **Create a new instance**, choose the version, mod loader, and download the mods you want to use
4. Use the [Admin Utility] to convert the mod info from the launcher to the script format, and upload the new file
   you get from the admin application to somewhere public, like [GitHub](https://github.com/)
5. To prepare the instance that the players will use, **create a new instance**, choose the same version
   and mod loader as the previous instance while refraining from downloading the mods.
   This instance will be configured to use the script.

   The previous one will be used by [Admin Utility] to convert the mods' info to the script format
6. Move the downloaded script JAR file to the instance directory where it has the `mods` and other folders
7. Go to the settings of the newly created instance, try to find the section of the custom commands,
   or the command to run before launching the game.
   For example, for [MultiMC Launcher], it's
   in [here](https://github.com/MultiMC/Launcher/wiki/Instance-settings#custom-commands), the same applies
   the launchers based on it like [Prism Launcher]
8. Once you find the `Pre-launch command` or something similar, enter the following:
   ```
   $INST_JAVA -jar $INST_MC_DIR/kraft-sync.jar
   ```
   and replace `kraft-sync` with the JAR file name from **Step 6** if necessary.
   You might need to replace `$INST_JAVA` and `$INST_MC_DIR` environment variables with something else depending on the
   launcher, [MultiMC Launcher] and the launchers that are based on it use those variables,
   and other launchers like [ATLauncher] use the same for compatibility
9. Now launch the instance to run the game, if this is the first time, it will ask you for the URL
   from **Step 4**.
   Enter it and then wait for the sync process to finish, the game will launch with the new synced
   content
10. To make this process easier for all the players, export the instance from **Step 5** that's configured
    to use the script, make sure to include the used JAR file and the `kraft-sync-data` folder
    and exclude the content that will be synced like the `mods` as the script will download them once the player import
    the instance and launch the instance, you can include them though, the script will sync them if they're outdated.

</details>

This section will be updated in the future for improvements.

[//]: # (TODO: Update or rework this section)

## 💬 Frequently Asked Questions

This FAQ provides answers to some of the most commonly asked questions. Click on the question to expand and see the
answer.

[//]: # (Questions start)

**How to use this FAQ?**

* Click on any question below to expand and reveal the answer.
* You can use the search functionality of your browser (Ctrl+F or Cmd+F) to find specific keywords within the questions
  and answers.

**List of Questions:**

1. [**Why not use sync mods?**](#q1)
2. [**Does it sync the Minecraft version, Mod loader, and Java version?**](#q2)
3. [**Is there a GUI for admins?**](#q3)
4. [**I have an issue/bug or feature request**](#q4)
5. [**What happens to the old script?**](#q5)
6. [**Why is the JAR file size not smaller?**](#q6)
7. [**Why not Mod packs?**](#q7)
8. [**Will there be Bedrock support?**](#q8)

**Answers:**

<details>
<summary id="q1">Why not use sync mods?</summary>
There are some mods that are required to be installed on both the client and server-side
and it will simply sync the mods when you join a server by downloading the mods to a Minecraft server.

While those mods work great, depending on your use case,
you might use one of those mods or use this script.

Some common issues encountered when using Minecraft mods that synchronize the mods include:

1. It will require running the HTTP server on a port other than the Minecraft port (e.g., 25565) which can be used by
   attackers to cause performance issues if you haven't implemented Rate Limit (otherwise some users might spam the
   server) and another security mechanism that might affect the network and traffic from your Minecraft hosting
2. You will constantly need to update the mod for Forge/Fabric or the mod loader you are using, and for a specific
   Minecraft version, while this script works independently of the Minecraft version and the mod loader
3. The users have to update the mods when joining the server, then restarting the game, so let's say a new Minecraft
   version has been released, and you were playing with your friends, you will have to wait for the mod that syncs the
   mods to be updated or update it yourself, then ask your friends to restart the game with the new version and manually
   update the sync mod, then you will have to update all the mods on the server, after when they launch the game
   once they launch the game they will have to update and then restart once again, when using the script, you only have
   to update the data as admin, then ask your friends and players to restart the game
4. Can only sync Minecraft mods (which might be exactly what you want)
5. Server and client-side mods are different: There are completely server-side mods
   like [Geyser](https://modrinth.com/mod/geyser) or [Spawn Animations](https://modrinth.com/datapack/spawn-animations)
   and including them on the client side will increase the size and require the players to download more, for example,
   Geyser is above 10 MB, Another client-side mod [Physics Mod](https://modrinth.com/mod/physicsmod) which is more than
   130 MB, when using more and more mods not only the syncing process will be slower, it will also require more space on
   the disk and in some rare cases it could cause issues (if the server-side mod causes an exception when running on
   the client side) or it's not marked as a server-side mod
6. Uploading Minecraft developer mods and downloading them to the players from external sources other than the original
   might be violating the LICENSE
7. You might have large client-side mods like [Physics Mod](https://modrinth.com/mod/physicsmod) which might not be
   needed in the server mod
8. Only work with official Minecraft servers, you might use some other solution to play with your friends
   like [Essential](https://essential.gg/) or you want to sync the content without using sync mods for some other
   reason

Also, there is an idea that is a mix of using a mod (server-side mod) and a script, yet this method could introduce
issues later.

</details>

<details>
<summary id="q2">Does it sync the Minecraft version, Mod loader, and Java version?</summary>
Right now, the script won't download or sync the Java version, because the custom launcher you are using
or even the official Minecraft launcher already has Java JRE installed,
the script will use that installed Java instead of
requiring the players to download and install Java on their system,
launchers like official Minecraft and some other custom launchers
will automate the process of updating and installing the required Java version for a specific Minecraft version

As for the Minecraft version, unfortunately, we currently don't sync it as some launchers
might store such information, and we can't update and cover all the launchers, breaking changes might be introduced, and
the goal of this script is to make it simple and work for most use cases, if there is a new Minecraft version, the
player
needs to update the Minecraft version along with the mod loader manually, launching the game to update the assets (e.g.
Mods, Resource-packs) then they can get back to playing the game

We might add a feature that will display a warning or require the player to update the mod loader
version using their launcher or manually downloading it from the web.

Or we might automate this process, until now we don't have plans.
</details>

<details>
<summary id="q3">Is there a GUI for admins?</summary>
For now, we don't have a GUI for downloading and configuring the mods and customizing the data,
We might create a simple web interface where you can input some data and you will either
get a URL to use in the script or just the raw JSON data, and you will need to upload it somewhere

We still haven't planned for some things, like whether we should add authentication and store the data on the server
so each admin can access their multiple servers and update the assets (e.g. Mods, Resource-packs), or we should only
provide add and edit functionalities, for the add you will insert all the data, for the edit you will provide the raw
JSON data and then edit it in GUI and get the new one

The GUI should allow selecting assets (e.g., Mods, Resource-packs) from some providers like Modrinth, CurseForges, or by
custom URL.

</details>

<details>
<summary id="q4">I have an issue/bug or feature request</summary>

File an issue in the [issues](https://github.com/ellet0/kraft-sync/issues) with detailed information as much as you can,
with the error and what are you trying to do and how to reproduce it.
We will do our best to help fix it.

if you have a question, use the Discussions tab instead
</details>

<details>
<summary id="q5">What happens to the old script?</summary>

We have another script called [MC Mods Sync](https://github.com/ellet0/mc-mods-sync)
which is limited to syncing the mods only, requiring the admin
to upload the mods on GitHub and then download them,
other than many issues we discussed like storage limitation and LICENSE
issues, it's easier to set up and maintain yet less flexible and simpler

</details>

<details>
<summary id="q6">Can the JAR file be smaller?</summary>
Although the script has only one task, the bundle size is not too small. You might wonder why this is the case if
Java is already installed on the system/launcher.

There are some technical reasons:

1. **Written in [Kotlin]**: Kotlin is fully compatible with Java, You don't need Kotlin installed on your machine
   to run the script as Kotlin will include everything it needs in the JAR file directly allowing it to
   use the existing Java JRE on your system/launcher, it's also
   use [Kotlin Standard Library](https://kotlinlang.org/api/latest/jvm/stdlib/), the JAR file for Kotlin/JVM
   applications around 1.7 MB without dependencies, it does provide many features and advantages compared to this
   size
2. **Compatible with older versions of Java**: Thanks to [Kotlin], it's possible to use recent language features
   like [Sealed Classes](https://docs.oracle.com/en/java/javase/17/language/sealed-classes-and-interfaces.html)
   Which is available in Java 17 and above, with Kotlin it's possible to use it even in Java 8, it provides modern
   syntax and makes developing JVM apps easier with many built-in features with more recent syntax, it also
   makes it easier to compile the script to native platforms
   using [Kotlin/Native](https://kotlinlang.org/docs/native-overview.html)
3. **Cross Platform**: Java made it easy to write cross-platform apps, while it's also possible with Dart and other
   languages, it requires different operating systems (Linux, macOS, and Windows) to compile the script and build the
   binaries, which means you have to include 3 different versions of the script in your instance
   or provide separate ones for each operating system, Windows players have to download the Windows version of the
   instance in order to work, some of the solutions require installing the language on the machine, and the goal of
   the script is to make it as easy as possible to run without installing anything extra or too many manual
   configurations
   which is one reason why this script uses JVM as most Minecraft Java Launchers already have it installed, which
   make it easier to write a cross-platform script from a single code base, the script
   will use [Java Swing](https://docs.oracle.com/javase/tutorial/uiswing/) for GUI support
   without making the bundle size larger, and we will use imports from Java when possible
4. **Features**: The script provides some features like dark and light mode support with different themes
   which makes the bundle size a little bit larger

We will try to find a good balance between the features and the bundle size.
We use a tool called [Proguard](https://www.guardsquare.com/proguard) to shrink and minimize
the JAR file.

</details>

<details>
<summary id="q7">Why not Mod packs?</summary>

It depends on your use case, there are some reasons that prevent some players from using Mod packs
to use Minecraft mods with players

1. To allow your friends to use your mod pack to get the updates, you have to publish it to a provider
   that is supported by the launcher like [Modrinth] or [Curse Forge].

   It will be listed publicly, a review is usually required, and you might only want to play it with your friends
2. Limited to the supported launchers, technically you can run this script without Minecraft.
   It can work on any launcher or even the
   official [Minecraft Launcher], we recommend using any launcher that supports Instances and Pre-launch command
   feature, which most Minecraft launchers do
3. Server and Client Flexibility, Mod-packs are usually for syncing the client side, while it's possible to get the
   updates of a Minecraft mod pack into a server, or most server hosting providers already support a wide range of
   mod-packs with one click, this script allows you to split the used mods between the server and the client or use them
   on
   both sides, and it can work on any server even on localhost
4. Allow you to use a mix of mods from multiple providers, the script only needs the URL to the mod, and it can be from
   anywhere, it doesn't store anything specific about any mod provider, only the links to the mod providers which will
   be used to detect the mod provider, if it's unknown or unverified, then it will warn the user before continuing for
   the first time, the script doesn't store any data remotely by itself, which gives you more control over where to
   store it
5. License Compliance by avoiding re-uploading the mods somewhere other than the source, the script will
   download the mods as if you're downloading them from the website
6. It can only support mods; The term "Mod packs" typically refers to collections of mods, which can include resource
   packs as well.
   However, they might not always meet specific needs or preferences.
7. More features like
   the [Quick Play](https://www.minecraft.net/en-us/article/quick-play-coming-java-and-bedrock-edition) support feature,
   see [Features](#-features) for more info

</details>

<details>
<summary id="q8">Will there be Bedrock support?</summary>

There are some reasons preventing us from providing support to
[Minecraft Bedrock](https://play.google.com/store/apps/details?id=com.mojang.minecraftpe&hl=en_US&pli=1) Support

1. Built with different code and underline data, which requires us to do breaking changes, and make more steps to use
   the script with different data for resource-packs and behavior-packs, add-ons, and shaders for Bedrock and for Java
   which require different information to be stored or maybe both in the same URL or maybe provide two versions of the
   scripts
2. Lack of asset provider support for the Bedrock edition
3. The bedrock edition already has a way to sync the resource packs and behavior packs to all the players; it's much
   easier, all you have to enter the IP address join as a player, and click continue
4. Requires refactoring almost all the logic and abstracting it to different editions of Minecraft
5. It will require Java JRE installed on the machine since the script now will use JVM as Minecraft Java already needs
   it installed on the launcher/system, the script will use the same Java installation to run, if players are only
   interested in playing a Minecraft Bedrock edition, installing Java on the system might not be preferred,
   which requires us to bundle the JRE and provide native platform binaries or
   use [Kotlin/Native](https://kotlinlang.org/docs/native-overview.html) with KMP to still use Kotlin/JVM for another
   module/source set
6. Sandbox limitations, which is a good thing and has many features.
   However, it doesn't allow reading or editing the content, nor an API or something else to edit with permission.
7. Bedrock Edition does not support multiple instances, each with its own data and configurations
8. The modifications are limited to the Bedrock edition, which reduces the necessity for using this script

There may be additional reasons, as discussed above, why using this script is less needed for the Bedrock edition;
the bedrock edition already has something similar

</details>

[//]: # (Questions end)

**We hope this FAQ helps!**

If you have any further questions that are not covered here, feel free to file an issue or create a new
discussion.

## 🔰 Admin Utility

We provide a utility program 🛠️ for the Admin which helps when dealing with the data or converting them from other
launchers, for more details, navigate to the [Admin Utility] page.

## 🛠 Build from Source

If you want to build from source for the latest version:

1. Install the [Requirements](./CONTRIBUTING.md#-requirements)
2. Run `./gradlew assemble` or `./gradlew.bat assemble` on **Microsoft Windows**
3. Navigate to [dist](./dist) folder where you should find the Jar

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)

## 🤝 Contributing

We greatly appreciate your time and effort.

To keep the project consistent and maintainable, we have a few guidelines that we ask all contributors to follow.
These guidelines help ensure that everyone can understand and work with the code easier.

See [Contributing] for more details.

## 📜 Acknowledgments

We are incredibly grateful to many individuals and organizations who have played a
role in the project.
This includes the community, dedicated volunteers, talented developers and
contributors, and the creators of the open-source tools we rely on.

Thanks to:

- The welcoming community, the volunteers who helped along the journey, developers, contributors
  and contributors who put time and effort into everything including making all the libraries, tools, and the
  information we rely on
- [JetBrains](https://www.jetbrains.com/) for developing and maintaining [Kotlin],
  [IntelliJ IDEA Community Edition](https://www.jetbrains.com/idea/) in along with other projects
- [Square Community](https://square.github.io/) for developing and
  maintaining [OkHttp](https://square.github.io/okhttp/)
- [GuardSquare](https://github.com/Guardsquare) for developing and
  maintaining [Proguard](https://github.com/guardsquare/proguard/)

[//]: # (Links)

[Kotlin]: https://kotlinlang.org/

[Releases]: https://github.com/ellet0/kraft-sync/releases

[Prism Launcher]: https://prismlauncher.org/

[MultiMC Launcher]: https://multimc.org/

[Minecraft Launcher]: https://www.minecraft.net/download

[ATLauncher]: https://atlauncher.com/

[Modrinth]: https://www.modrinth.com/

[Curse Forge]: https://www.curseforge.com/

[Admin Utility]: ./admin/README.md

[Mods Info Converter]: ./admin/README.md#-mods-info-converter

[Contributing]: ./CONTRIBUTING.md