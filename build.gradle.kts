import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jooq.meta.jaxb.Logging

plugins {
  id("io.spring.dependency-management")
  id("org.flywaydb.flyway")
  id("org.jooq.jooq-codegen-gradle")
  id("org.springframework.boot")
  idea
  kotlin("jvm")
  kotlin("plugin.spring")
}

val jdbcDriver: String by project
val jdbcUrl: String by project
val jdbcUser: String by project
val jdbcPassword: String by project

val flywayVersion: String by project
val javaVersion: String by project
val jooqVersion: String by project
val kotlinVersion: String by project
val postgresqlVersion: String by project
val springBootVersion: String by project
val springmockkVersion: String by project

extra["flyway.version"] = flywayVersion
extra["jooq.version"] = jooqVersion
extra["kotlin.version"] = kotlinVersion
extra["postgresql.version"] = postgresqlVersion

repositories {
  mavenCentral()
}

dependencies {
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("org.flywaydb:flyway-core")
  implementation("org.flywaydb:flyway-database-postgresql")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jooq:jooq-kotlin")
  implementation("org.jooq:jooq-postgres-extensions:$jooqVersion")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-jooq")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-web")
  runtimeOnly("org.postgresql:postgresql")
  testImplementation("com.ninja-squad:springmockk:$springmockkVersion")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

flyway {
  driver = jdbcDriver
  url = jdbcUrl
  user = jdbcUser
  password = jdbcPassword
}

jooq {
  configuration {
    logging = Logging.WARN
    jdbc {
      driver = jdbcDriver
      url = jdbcUrl
      user = jdbcUser
      password = jdbcPassword
    }
    generator {
      name = "org.jooq.codegen.KotlinGenerator"
      database {
        name = "org.jooq.meta.postgres.PostgresDatabase"
        inputSchema = "public"
        includes = ".*"
        excludes = "flyway_schema_history.*"
        isOutputSchemaToDefault = true
      }
      generate {
        isDefaultCatalog = false
        isDefaultSchema = false
        isGlobalObjectReferences = false
        isKotlinNotNullRecordAttributes = true
      }
      target {
        packageName = "com.noom.interview.fullstack.sleep.jooq"
      }
    }
  }
}

tasks.jooqCodegen {
  dependsOn(tasks.flywayMigrate)
  inputs.files(fileTree("src/main/resources/db/migration"))
}

kotlin {
  compilerOptions.freeCompilerArgs.addAll("-Xjsr305=strict")
  compilerOptions.jvmTarget = JvmTarget.fromTarget(javaVersion)
  jvmToolchain(javaVersion.toInt())
}

tasks.compileKotlin {
  dependsOn(tasks.jooqCodegen)
}

java {
  sourceCompatibility = JavaVersion.toVersion(javaVersion)
  targetCompatibility = JavaVersion.toVersion(javaVersion)
  toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)
}

tasks.withType<Test> {
  environment(
    "SPRING_DATASOURCE_URL" to jdbcUrl,
    "SPRING_DATASOURCE_USERNAME" to jdbcUser,
    "SPRING_DATASOURCE_PASSWORD" to jdbcPassword
  )
  systemProperties(
    "user.country" to "US",
    "user.language" to "en",
    "user.timezone" to "UTC"
  )
  testLogging {
    events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
    exceptionFormat = TestExceptionFormat.FULL
    showExceptions = true
  }
  useJUnitPlatform()
}

idea {
  module.sourceDirs.add(file("build/generated-sources/jooq"))
}
