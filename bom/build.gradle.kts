plugins {
    kotlin("jvm")
    id("com.vanniktech.maven.publish")
}

dependencies {
    constraints {
        api(project(":permissions"))
        api(project(":photopicker"))
        api(project(":storage"))
        api("com.squareup.okio:okio:3.0.0")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
