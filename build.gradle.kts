plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    id("org.jetbrains.intellij") version "1.17.2"
    id("io.github.sgtsilvio.gradle.proguard") version "0.7.0"
}

group = "com.lhstack"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    maven("https://maven.aliyun.com/repository/public/")
    mavenCentral()
}

intellij {
    version.set("2022.3")
}

dependencies {
    implementation(files("C:/Users/lhstack/.jtools/sdk/sdk.jar"))
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}



tasks.test {
    useJUnitPlatform()
}