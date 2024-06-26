# 🛠️ Development Local Server

This module has one task, which is to provide a minimal local server to host files
that will be used during development.

1. Create the file with any name (e.g., `sync-info.json`) in `dev-local-server/devWorkingDirectory` folder
2. Run using IntelliJ IDEA shared run configurations or change the working directory
   to `dev-local-server/devWorkingDirectory` instead of the root project folder
   if you're running the server in a different way.
3. You should now be able to send GET request to `http://localhost:8080/sync-info.json`, replace `sync-info.json`
   with the file path.