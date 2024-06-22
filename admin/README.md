# 🔰 Admin

A utility for the [Kraft Sync](../README.md) for the administration who manage the sync info

At the moment, the only functionality the program has is [Mods Info Converter](#mods-info-converter)

This page isn't complete yet, and this tool is experimental and can have many breaking changes

[//]: # (TODO: Complete this page)

## 🔁 Mods Info Converter

This feature allows you to convert the downloaded mods info
to the script data format from a Minecraft Launcher that supports downloading mods **inside the launcher**

### ⭐️ Important for mods uploaded to Curse Forge:

Most Minecraft launchers doesn't store Curse Forge CDN Download URL anymore, they usually store the project and the file
id, the script will send a GET request to [Curse Forge API](https://docs.curseforge.com/) which require an API key and
have a limit for how many times we send a request, we could require a `API_KEY` so you have to register first in their
website and then get it. 

To make the process easier we already included a default `API_KEY`, try to use other providers
like [Modrinth](https://www.modrinth.com/) when possible for downloading mods in your Minecraft launcher as this will
make the process easier, faster with fewer errors as it doesn't require sending any request using the **network**

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
free to submit a feature request in the [issues](https://github.com/ellet0/kraft-sync/issues)