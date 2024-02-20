# Thought Bank

Inspired by the following reddit comment on this [reddit post](https://www.reddit.com/r/AskReddit/comments/85j3ki/reddit_whats_your_million_dollar_app_idea_that/)

```text
ThoughtBank.

Imagine Twitter with no running feed. There's a Deposit function (post) and a Withdraw function (browse).

Deposit is when you have a random thought, idea, joke, etc. You type it in, then hit deposit. It goes into the "bank," disappearing forever (at least from your perspective).

When you hit Withdraw, you get serviced a random thought that another user has deposited. If it's deep, or makes you laugh, etc., you swipe right and it goes into your ThoughtWallet, so you can go back and view it later. Each user has a tracker that shows how many times their thoughts have been saved by other users (thoughtstock value). The more times a thought gets saved, the more likely it is to show up for other users' consideration. Similar to Tinder, as long as you keep swiping, you keep seeing new thoughts. (pun intended?) Ideally there would be an auto-mod system assigns each post a unique code to prevent any one user from getting served the same thought post repeatedly.

Possible additions are some sort of feature to enforce originality in the actual text of each post (which has it's own drawbacks), geo-mapped leaderboard of ppl in your area with the highest thoughtstock value, etc. Total anonymity would also be a nice feature, but that eliminates a lot of the potential gamification aspects and is much less attractive to advertisers.
```

This is a Kotlin Multiplatform project targeting Android, iOS, Web, Desktop, Server.

* `/composeApp` is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - `commonMain` is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    `iosMain` would be the right folder for such calls.

* `/iosApp` contains iOS applications. Even if you’re sharing your UI with Compose Multiplatform, 
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* `/server` is for the Ktor server application.

* `/shared` is for the code that will be shared between all targets in the project.
  The most important subfolder is `commonMain`. If preferred, you can add code to the platform-specific folders here too.


Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html),
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform),
[Kotlin/Wasm](https://kotl.in/wasm/)…

**Note:** Compose/Web is Experimental and may be changed at any time. Use it only for evaluation purposes.
We would appreciate your feedback on Compose/Web and Kotlin/Wasm in the public Slack channel [#compose-web](https://slack-chats.kotlinlang.org/c/compose-web).
If you face any issues, please report them on [GitHub](https://github.com/JetBrains/compose-multiplatform/issues).

You can open the web application by running the `:composeApp:wasmJsBrowserDevelopmentRun` Gradle task.

## Server Setup

The server is deployed to [fly.io](https://fly.io/) via Dockerfile. 

### Initial setup:

1. Sign up for fly.io

2. Install `flyctl` using these [instructions](https://fly.io/docs/hands-on/install-flyctl/)

3. From the `server` directory, run `fly launch` to create a new app for the server

4. Run `flyctl postgres create` to create a Postgres app

5. Associate the server app with the Postgres app by running `fly postgres attach <postgres app name> --app <app name>`

   - A new database and user are created in the Postgres cluster app.

6. Create a new secret `JDBC_DATABASE_URL` under the server app according to the following format:

   `jdbc:postgresql://<host>/<database>?sslmode=disable&user=<user>&password=<password>`,

   replacing the values in `<>` with the actual values obtained from before

### Deploy

1. Build the latest jar using the server `buildFatJar` gradle task
1. From the `server` directory, run `fly deploy`

### Access Database Externally

To access the database from your own machine, run `fly proxy 15432:5432 -a <postgres app name>` to forward the server port to your local system. This will make the database accessible on `localhost:15432`.
