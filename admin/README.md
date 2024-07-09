# 🔰 Admin

A utility for the [Kraft Sync](../README.md) for the administration who manage the sync info, can
extract mod info from launchers or install the script to a Minecraft launcher instance/profile.

This page isn't complete yet, and this tool is experimental and can have many breaking changes

## ✅ Supported Launchers

- [**Prism Launcher**](https://prismlauncher.org/): Supports both [Mods Info Converter](#-mods-info-converter)
  and [Script Installer](#-script-installer).

  Enabling the option `Disable using metadata for mods` in the launcher settings will prevent the application
  from detecting the mods.
- [**ATLauncher**](https://atlauncher.com/): Supports both [Mods Info Converter](#-mods-info-converter)
  and [Script Installer](#-script-installer)
- [**Modrinth App**](https://modrinth.com/app): Supports only [Mods Info Converter](#-mods-info-converter),
  [Script Installer](#-script-installer) support is blocked due to missing support for environment variables,
  See [Feature Request #1238](https://github.com/modrinth/code/issues/1238)
- [**MultiMC**](https://multimc.org/): We're still working on supporting this launcher.

## 🔁 Mods Info Converter

This feature allows you to convert the downloaded mods info
to the script data format from a Minecraft Launcher that supports downloading mods **inside the launcher**

We might add more features like uploading the data directly to where the sync info data exist using a Cloud provider, or
build the Pre-Launch command with the selected launcher, configuring the sync script with different options, and more.

Right now don't have a UI or a web application for selecting, updating, search, configuring
all the mods for example and give you the data to use, as right now you will have to manually deal with json and get
the info you need like the url of the mod in the json, or the file integrity info, which can be very time-consuming
especially if you have a large amount of mods, we provided an alternative for **converting the data from Minecraft
launchers**

So if you have mods installed in your instance in one of the above launchers, you can convert it the data in automated
way, the script will get information like the hashing, the mod side (if it's server or client side) or the mod url
(required) and all the available information like the mod name

if you have a launcher that have downloading mods directly from the launcher, and it's not supported in this list, feel
free to submit a feature request in the [issues](https://github.com/FreshKernel/kraft-sync/issues)

### ⭐️ Important for mods uploaded to Curse Forge:

Most Minecraft launchers don't store Curse Forge CDN Download URL anymore, they usually store the project and the file
id.

The application will send an HTTP GET request
to [Curse Forge API](https://docs.curseforge.com/) which requires an API key
and has a rate limit.
To make the process easier, we already included a default `API_KEY` and the option to override it.

When possible, consider using alternatives such as [Modrinth](https://www.modrinth.com/)
for downloading mods in your
Minecraft launcher as this will make the process easier and faster with fewer errors
as it doesn't require sending any requests using the **network**.

Some launchers store the download URL from the **Modrinth** provider, even if the mod is downloaded from Curse Forge.
If a mod is not available on Modrinth, the launcher may only store the Curse Forge project and file ID. In such cases,
the application retrieves the necessary information by sending a GET request as described earlier.

## 🛠️ Script Installer

You can use this application to install/uninstall the sync script to any instance to the supported Minecraft
launchers, you can start the application, go to `Installer` tab, follow the instructions and once done, you
can launch the game, and you will have the script installed.