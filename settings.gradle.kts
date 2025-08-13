rootProject.name = "sleep"

pluginManagement {
  val dependencyManagementVersion: String by settings
  val flywayVersion: String by settings
  val jooqVersion: String by settings
  val kotlinVersion: String by settings
  val springBootVersion: String by settings

  repositories {
    mavenCentral()
    gradlePluginPortal()
  }

  plugins {
    id("io.spring.dependency-management") version dependencyManagementVersion
    id("org.flywaydb.flyway") version flywayVersion
    id("org.jooq.jooq-codegen-gradle") version jooqVersion
    id("org.springframework.boot") version springBootVersion
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
  }
}

buildscript {
  val flywayVersion: String by settings
  val jooqVersion: String by settings
  val postgresqlVersion: String by settings

  repositories {
    mavenCentral()
  }

  dependencies {
    classpath("org.flywaydb:flyway-database-postgresql:$flywayVersion")
    classpath("org.jooq:jooq-kotlin:$jooqVersion")
    classpath("org.jooq:jooq-meta:$jooqVersion")
    classpath("org.jooq:jooq-postgres-extensions:$jooqVersion")
    classpath("org.postgresql:postgresql:$postgresqlVersion")
  }
}
