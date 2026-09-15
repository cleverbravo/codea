# CODEA

> **Your phone has a shell. Give it a reason to run.**

CODEA is an experimental Android project that turns a phone into a tiny development workstation by connecting **Jetpack Compose, Kotlin, Termux, shell automation, code-server, and WebView**.

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

## 🧠 Why CODEA?

There is a particular kind of developer who looks at a powerful Android phone and thinks:

> *“This is a computer. What if I treated it like one?”*

CODEA is an answer to that question.

Instead of building another mobile UI, the project experiments with using Android as the **orchestrator** and Termux as the **developer toolbox**. The application prepares the environment, executes commands, starts a local development service, and brings that service back into a native Android experience.

The goal is not to pretend that a phone is a desktop.

The goal is to discover **how much of a desktop can fit in your pocket.**

---

## ✨ What it does today

### 📦 Bootstrap the environment

CODEA contains an `ApkManager` domain with an installation-chain design. The chain can check whether a package is installed, verify that an APK exists, and pass an `InstallSession` from one stage to the next.

The project deliberately uses Kotlin's `Result<InstallSession>` so every stage has a clear success/failure boundary.

### 🐚 Run a developer bootstrap sequence

The current startup flow executes commands equivalent to:

```bash
pkg update -y
pkg upgrade -y
pkg install tur-repo -y
pkg install code-server -y
code-server --auth none &
sleep 1
```

The UI reports progress through a persistent status dialog rather than silently doing work in the background.

### 🌐 Open the development environment

Once the local server is started, CODEA loads:

```text
http://127.0.0.1:8080
```

inside an Android `WebView` with JavaScript and DOM storage enabled.

The WebView also contains a small JavaScript/native bridge and recovery handling for loading and HTTP errors.

---

## 🧩 Architecture

```text
src/app/src/main/java/com/codea/
│
├── MainActivity.kt
├── JSEventsBridge.kt
│
├── domain/
│   ├── ApkManager/
│   │   ├── ApkManager.kt
│   │   ├── InstallChain.kt
│   │   ├── InstallSession.kt
│   │   ├── CheckIfInstalled.kt
│   │   ├── VerifyFileExists.kt
│   │   ├── VerifyPGP.kt
│   │   └── DeleteApkFile.kt
│   │
│   └── TerminalManager/
│       ├── BashCommandExecutor.kt
│       └── CommandState.kt
│
└── ui/theme/
    ├── Color.kt
    ├── Theme.kt
    └── Type.kt
```

The architectural idea is simple:

**UI coordinates. Domain objects do the work. Termux provides the environment.**

That separation is especially useful because installation, shell execution, Android lifecycle, and WebView behavior all fail in very different ways.

---

## 🛠 Stack

| Area | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Android | SDK 34 |
| Async | Kotlin Coroutines |
| Terminal | Termux shared library |
| Development server | code-server |
| Browser layer | Android WebView |
| Build | Gradle + Kotlin DSL |
| Tests | JUnit + AndroidX + Espresso |

The current module uses `compileSdk 34`, `minSdk 34`, and `targetSdk 34`. fileciteturn4file0

---

## 🚀 Run it

### Clone

```bash
git clone https://github.com/cleverbravo/codea.git
cd codea
```

### Build

Open the project in Android Studio or run:

```bash
./gradlew assembleDebug
```

Install the resulting APK on an Android 14/API 34+ device.

> A physical device is recommended because CODEA interacts with the Android/Termux environment.

### First launch

The first launch is intentionally more dramatic than a normal Android app:

```text
┌───────────────────────────┐
│       Please wait...      │
├───────────────────────────┤
│ Installing / checking...  │
│ Running: pkg update -y    │
│ Running: pkg upgrade -y   │
│ Installing code-server... │
│ Starting local server...  │
└───────────────────────────┘
             │
             ▼
      localhost:8080
             │
             ▼
          CODEA 🚀
```

Depending on the Android/Termux setup, additional permission or configuration steps may be necessary before command execution works.

---

## 🔬 The interesting bits

CODEA is really several experiments hiding inside one application:

**Android ↔ Linux**  
Use Android as the native host while delegating developer tooling to Termux.

**App ↔ Shell**  
Turn shell commands into structured application operations with success/failure results.

**Native ↔ Web**  
Use WebView to embed a browser-based development interface inside a Compose application.

**Bootstrap ↔ Lifecycle**  
Deal with the awkward reality that installing packages and starting servers takes longer than an Android frame.

**Failure ↔ Recovery**  
Treat errors and a server that is “not ready yet” as normal states instead of assuming localhost will always be available instantly.

---

## 🗺 Roadmap

- [ ] Robust package/version detection
- [ ] Complete APK hash/integrity verification
- [ ] Reliable temporary APK cleanup
- [ ] Better Termux permission detection
- [ ] Persistent background server/service management
- [ ] Start / stop / restart controls
- [ ] Terminal and command-log viewer
- [ ] Workspace/project management
- [ ] Configurable code-server options
- [ ] Better Android lifecycle handling
- [ ] More installation-chain tests
- [ ] Offline and recovery-first UX

Some installation stages are currently placeholders. In particular, the repository contains explicit unfinished work for verification and APK cleanup. fileciteturn3file6 fileciteturn3file9

---

## ⚠️ Experimental means experimental

CODEA is **not a production IDE yet**.

Expect platform-specific behavior, Termux integration quirks, Android lifecycle surprises, incomplete installation stages, and rough edges around background execution.

That is part of the project.

> **The experiment is the product.**

If something breaks, it is probably pointing at the next interesting engineering problem.

---

## 🤝 Contributing

If you want to experiment with CODEA, improvements are welcome.

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

---

## 👤 Author

**Clever Bravo**

Android · Kotlin · .NET · C++ · Linux · automation · developer tooling

- GitHub: https://github.com/cleverbravo
- Repository: https://github.com/cleverbravo/codea

---

## 📜 License

No explicit open-source license is currently declared in the repository. If you plan to redistribute or publish a derivative, check the repository licensing status or contact the author first.

---

<div align="center">

### CODEA

**Pocket computer. Real shell. Tiny workstation.**

`Kotlin` · `Compose` · `Termux` · `code-server` · `WebView`

</div>
