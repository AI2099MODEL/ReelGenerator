# Personal Organizer & Stitch MCP Setup

## Overview
This repository contains the Personal Organizer Android app, designed to help users manage their daily tasks, schedules, and priorities.

## Setup Instructions

### 1. Model Context Protocol (MCP) Setup
To design and iterate on the UI using Google Stitch MCP:
1. Obtain a **Stitch API Token** from your Stitch dashboard.
2. Set the `STITCH_API_TOKEN` environment variable in your shell (e.g. `export STITCH_API_TOKEN=...`).
   `mcp_servers.json` references it as `${STITCH_API_TOKEN}` — the real token is **never** committed.
3. Register the server in your AI editor (e.g. `claude mcp add stitch --transport http --header "X-Goog-Api-Key: $STITCH_API_TOKEN" https://stitch.googleapis.com/mcp`).
4. Restart your AI editor (Cursor, Claude Code, etc.) to ensure the MCP server is loaded correctly.

> **Security:** Do not paste the live Stitch token into `mcp_servers.json`, chat logs, or any committed file. If a token was leaked, revoke it in the Stitch dashboard and regenerate.

### 2. Android App Setup
1. Open the project in Google AI Studio or Android Studio.
2. For live Chat assistant replies, create a `.env` file (copy from `.env.example`) and set `GEMINI_API_KEY=...` to a Gemini API key. The secrets-gradle-plugin injects it as `BuildConfig.GEMINI_API_KEY`. AI Studio also injects it from the Secrets panel at runtime.
3. If `GEMINI_API_KEY` is blank, Chat still works (messages are saved and threads sync) but assistant replies fall back to a local offline acknowledgement instead of calling Gemini.
4. Wait for Gradle sync to complete and run the app.

*(Note: The attempt to clone the `myliferepo` from GitHub did not find a definitive repository, so this codebase represents a fresh initialized Personal Organizer project instead.)*
