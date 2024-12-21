## Demo

![Debugging JetLagged App](https://github.com/composexy/decomposer/blob/master/assets/decomposer.gif)


## Introduction

Decomposer is a desktop app to help developers investigating jetpack compose internals. The app is built with jetpack compose for desktop. This tool provides two core utilities:
* Viewing the ir structure of your app
* Viewing the composition structure of your app

### Ir structure

As you know, the compose framework uses a kotlin compiler plugin to rewrite your compose code. It might be helpful to understand how the compiler plugin rewrite your compose code. Compose compiler mainly works on the IR stage of kotlin compiler pipeline and there are challenges to view the IR structure in action. First of all, the IR tree is a transient structure in kotlin compiler, it is not serialized to disk after compilation. Secondly, the IR tree is not human readable. So this decomposer tool solves this problem by rendering the IR to a kotlin like format which makes it much easier to read.
* Compare the IR tree before and after compose compiler plugin kicks in with a single click.
* View the IR tree in origin format and kotlin like format with a single click.

### Composition structure

The compose framework stores composition data in an internal data structure called SlotTable. It might be helpful to directly view the SlotTable in a human readable tree structure. The decomposer tool also does that.
* View the whole SlotTable as a tree structure or only a subtree of the SlotTable.
* Filter out empty or leaf tree groups in SlotTable.
* Filter out compose framework created nodes.
* View a subtree of the SlotTable. Currently supporting composition subtree, compose node subtree and recompose scope tree.
* View the structure of state table, snapshot observers, layout nodes, recompose scopes, composable lambdas in the SlotTable.


## Usage

There are three main components of this tool:
* The decomposer desktop app
* Decomposer kotlin compiler plugin and gradle plugin
* Decomposer runtime

### Steps
1. Clone the repository
2. Download android studio and install the [kotlin multi-platform plugin](https://plugins.jetbrains.com/plugin/14936-kotlin-multiplatform).
3. Import the project. After importing, there should be two run configuration imported by android studio. The first one is the decomposer desktop app and the second one is a sample android app to use as a playground.
4. You can also run the desktop app via command line: ```./gradlew :composeApp:run```. Now you can run the sample app on an android device that is connected to the PC via usb cable. This makes sure the desktop app can find the device via adb command.
5. Optionally, you can create an installer for the desktop app via ```./gradlew package```.

### Use it in another app
1. Build the project via ```./gradlew assemble```.
2. Publish the artifacts to maven local: ```./gradlew publishToMavenLocal```.
3. Add mavenLocal to your project's artifacts searching repo.
4. The artifacts are also published to maven central if you do not want to build it locally.

```
gradle/libs.version.toml:

[versions]
decomposer = "[version]"

[libs]
decomposer-runtime = { group = "io.github.composexy-decomposer", name = "runtime-android", version.ref = "decomposer" }

[plugins]
decomposer = { id = "io.github.composexy-decomposer", version.ref = "decomposer" }


root project setting.gradle.kts:

pluginManagement {
    repositories {
        ...
        mavenLocal()
    }
}


root project build.gradle.kts:

plugins {
    ...
    alias(libs.plugins.decomposer) apply false
}


app/build.gradle.kts:

plugins {
    alias(libs.plugins.decomposer)
}

// Only enable in debug build
kotlin {
    compilerOptions {
        val isDebug = project.hasProperty("android")
                && android.buildTypes.find { it.name == "debug" } != null
        if (isDebug) {
            freeCompilerArgs.addAll(
                "-P", "plugin:com.decomposer.compiler:enabled=true",
            )
        } else {
            freeCompilerArgs.addAll(
                "-P", "plugin:com.decomposer.compiler:enabled=false"
            )
        }
    }
}

dependencies {
    implementation(libs.decomposer.runtime)
}


AndroidManifest.xml:

<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    // Decomposer runtime uses websocket to communicate with the desktop app
    <uses-permission android:name="android.permission.INTERNET"/>

    <application
        android:name=".MyApplication"
        android:networkSecurityConfig="@xml/network_security_config"
    </application>
</manifest>


network_security_config.xml:

<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
    // Makes sure clear traffic is permitted on localhost
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">localhost</domain>
    </domain-config>
</network-security-config>


app/MyApplication.kt

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Only enable on debug build
        if (BuildConfig.DEBUG) {
            runtimeInit {
                packagePrefixes = listOf(this@MyApplication.packageName)
            }
        }
    }
}
```

### notes

* Each time you changed some code in your app while debugging, make sure you first uninstall your app from your phone and do a clean reinstall. Otherwise android studio may take some shortcuts which make the app's dex files in an inconsistent state.
* The decomposer relies heavily on kotlin compiler plugin and embedded kotlin compiler internals. And the decomposer runtime uses reflection to retrieve composition data. That means this tool relies heavily on hidden apis of kotlin compiler and compose runtime. When you start the desktop app, you will see the a message about targeting compose runtime version and kotlin version. These versions are what the current decomposer tool is tested against. If your app uses a different kotlin version or compose runtime version. This tool may only partially work or not working at all. For example, your app needs to use at least kotlin 2.1.0 for decomposer to work.
