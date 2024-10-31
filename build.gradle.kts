import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
  kotlin("jvm") version "2.0.20"
  kotlin("kapt") version "2.0.20"
  application
  id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "io.harbor"
version = "1.0.0-SNAPSHOT"

repositories {
  mavenCentral()
  jcenter()
}

val kotlinVersion = "2.0.20"
val vertxVersion = "4.5.10"
val junitJupiterVersion = "5.9.1"
val jooqVersion = "6.3.0"
val postgresqlVersion = "42.2.2"

val mainVerticleName = "io.harbor.calendly.MainVerticle"
val launcherClassName = "io.vertx.core.Launcher"

val watchForChange = "src/**/*"
val doOnChange = "${projectDir}/gradlew classes"
//val launcherClassName = "io.vertx.core.Launcher"

val jsonFile = File("config/app.config.json")


application {
  mainClass.set(launcherClassName)
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
  implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
  implementation("io.vertx:vertx-web-client")
  implementation("io.vertx:vertx-auth-jwt")
  implementation("io.vertx:vertx-service-proxy")
  implementation("io.vertx:vertx-health-check")
  implementation("io.vertx:vertx-web")
  implementation("io.vertx:vertx-service-discovery")
  implementation("io.vertx:vertx-micrometer-metrics")
  implementation("io.vertx:vertx-jdbc-client")
  implementation("io.vertx:vertx-config")
  implementation("io.vertx:vertx-rx-java3")
  implementation("io.vertx:vertx-service-factory")
  implementation("io.vertx:vertx-pg-client")
  implementation("io.vertx:vertx-redis-client")
  implementation("io.vertx:vertx-circuit-breaker")
  implementation("io.vertx:vertx-lang-kotlin-coroutines")
  implementation("io.vertx:vertx-lang-kotlin")
  implementation("io.vertx:vertx-web-graphql")
  implementation("io.vertx:vertx-web-openapi-router")
  implementation("com.google.code.gson:gson:2.8.9")
  implementation("io.vertx:vertx-mail-client")
  implementation("io.netty:netty-resolver-dns-native-macos")

  // Vert.x Template Engine Core
  implementation("io.vertx:vertx-web-templ-handlebars:${vertxVersion}")

  // Handlebars.java (the underlying implementation)
  implementation("com.github.jknack:handlebars:4.3.1")

  implementation("io.github.jklingsporn:vertx-jooq-rx-reactive:$jooqVersion")
  implementation("io.github.jklingsporn:vertx-jooq-generate:$jooqVersion")
  implementation("org.postgresql:postgresql:42.5.4")
  implementation("org.jooq:jooq:3.14.3")
  implementation("org.jooq:jooq-meta:3.14.3")
  implementation("org.jooq:jooq-codegen:3.14.3")
  implementation("com.google.firebase:firebase-admin:9.3.0") {
    exclude("io.netty")
  }
  implementation("commons-io:commons-io:2.11.0")

  kapt("io.vertx:vertx-codegen:$vertxVersion:processor")
  annotationProcessor("io.vertx:vertx-codegen:$vertxVersion:processor")
  annotationProcessor("io.vertx:vertx-service-proxy:$vertxVersion")
  compileOnly("io.vertx:vertx-codegen:$vertxVersion")


  implementation("org.slf4j:slf4j-simple:2.0.6")
  implementation("org.slf4j:slf4j-api:2.0.6")
  implementation("ch.qos.logback:logback-classic:1.4.5")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
  implementation(kotlin("stdlib-jdk8"))
  implementation("com.ongres.scram:client:2.1")
  testImplementation("io.vertx:vertx-junit5")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
  testImplementation(kotlin("test"))
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "17"

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<ShadowJar> {
  archiveClassifier.set("fat")
  manifest {
    attributes(mapOf("Main-Verticle" to mainVerticleName))
  }
  mergeServiceFiles {
    include("META-INF/services/io.vertx.core.spi.VerticleFactory")
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events = setOf(PASSED, SKIPPED, FAILED)
  }
}

tasks.withType<JavaExec> {
  args = listOf(
    "run",
    mainVerticleName,
    "--conf",
    "config/app.config.json"
  )
}
