plugins {
    id("base")
    id("maven-publish")
}

group = "com.dshatz"


val linuxJar by tasks.registering(Jar::class) {
    // This is the classifier that consumers will use
    archiveClassifier.set("linux-x64")

    from("binaries/linux-x64") {
        into("natives/linux-x64")
    }
}

val macosJar by tasks.registering(Jar::class) {
    archiveClassifier.set("macos-arm64")
    from("binaries/macos-arm64") {
        into("natives/macos-arm64")
    }
}

val windowsJar by tasks.registering(Jar::class) {
    archiveClassifier.set("windows-x64")
    from("binaries/windows-x64") {
        into("natives/windows-x64")
    }
}