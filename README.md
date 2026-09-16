# CODEA

**Is an experimental Android project that runs vs code server via code-server, and WebView**.

```text
 Android
    │
    ▼
 ┌──────────────┐
 │    CODEA     │  Kotlin + Compose
 └──────┬───────┘
        │
        ▼
 ┌──────────────┐
 │  APK Manager │  install / verify / chain
 └──────┬───────┘
        │
        ▼
 ┌──────────────┐
 │    Termux    │  Linux user-space
 └──────┬───────┘
        │
        ▼
 ┌──────────────┐
 │ Shell runner │  commands + status
 └──────┬───────┘
        │
        ▼
 ┌──────────────┐
 │ code-server  │  localhost:8080
 └──────┬───────┘
        │
        ▼
      WebView
```

Improvements are welcome.

A useful rule when changing the project:

```text
Android UI
    ↓
Application orchestration
    ↓
Domain operations
    ↓
Termux / shell
    ↓
Development services
```

Prefer improving a boundary over adding more responsibility to `MainActivity`.

