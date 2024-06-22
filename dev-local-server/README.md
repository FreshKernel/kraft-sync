# 🛠️ Development Local Server

This module has one task, which is to provide a minimal local server that has a local file that
will be used for development only.

Create a file called `file.json` in this module directory, and start the server using:

```./gradlew dev-local-server:run```

> If you're on **Microsoft Windows**, replace `gradlew` with `gradlew.bat`.

If you're using any other way to start the server, change the working directory
to `dev-local-server/devWorkingDirectory` instead of the root project folder.