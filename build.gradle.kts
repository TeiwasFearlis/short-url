import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.5.1"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("java")
    kotlin("jvm") version "1.5.10"
    id("com.google.cloud.tools.jib") version "3.1.4"
    kotlin("plugin.spring") version "1.5.10"
    kotlin("plugin.serialization") version "1.5.31"
}

group = "ru.test"
version = "0.0.2-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}



dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")

    implementation("io.projectreactor:reactor-test")
    implementation("org.springframework:spring-jdbc")

    runtimeOnly("io.r2dbc:r2dbc-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")

    implementation("com.github.ben-manes.caffeine:caffeine")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")


    implementation("org.liquibase:liquibase-core")

    testImplementation("org.springframework.boot:spring-boot-test")
    testImplementation("org.assertj:assertj-db:1.3.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.2")
    testImplementation("io.zonky.test:embedded-database-spring-test:2.1.0")
    testImplementation("io.zonky.test.postgres:embedded-postgres-binaries-bom:12.1.0-1")
    testImplementation("com.opentable.components:otj-pg-embedded:0.13.4")
    testImplementation("io.projectreactor:reactor-test")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")


}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }
    named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
        jvmArgs("-Xmx512M", "-Dspring.profiles.active=development")
    }
}



tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

jib {
    from {
        image = "azul/zulu-openjdk:11"
    }
    to {
        setImage(provider { "docker.io/teiwas/$name:$version" })
    }
    container {
        environment = mapOf(
                "JAVA_TOOL_OPTIONS" to listOf(
                        "-XX:MaxRAMPercentage=60",
                        "-XX:+UseStringDeduplication",
                ).joinToString(" ")
        )
        user = "nobody"
    }
}

