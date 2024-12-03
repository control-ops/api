plugins {
	java
	id("org.springframework.boot") version "3.3.4"
	id("io.spring.dependency-management") version "1.1.6"
	id("io.freefair.lombok") version "8.11"
}

group = "com.control_ops"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

sourceSets {
	create("intTest") {
		compileClasspath += sourceSets.main.get().output
		runtimeClasspath += sourceSets.main.get().output
	}
}

val intTestImplementation: Configuration by configurations.getting {
	extendsFrom(configurations.implementation.get())
}

val intTestRuntimeOnly: Configuration by configurations.getting {
	extendsFrom(configurations.runtimeOnly.get())
}

val mockitoAgent = configurations.create("mockitoAgent")
dependencies {
	implementation("org.springframework.boot:spring-boot-starter-amqp")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.amqp:spring-rabbit-test")
	testImplementation(libs.mockito.core)
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testRuntimeOnly("com.h2database:h2") // A database is required by the applicationcontext for tests to pass; switch out later for PostgresSQL

	intTestImplementation("org.springframework.boot:spring-boot-starter-test")
	intTestImplementation("org.springframework.boot:spring-boot-testcontainers")
	intTestImplementation("org.testcontainers:postgresql")
	intTestImplementation("org.testcontainers:junit-jupiter")
	intTestRuntimeOnly("com.h2database:h2") // A database is required by the applicationcontext for tests to pass; switch out later for PostgresSQL

	// Used to be the following, but changed due to an annoying IntelliJ warning: mockitoAgent(libs.mockito.core) { isTransitive = false }
	mockitoAgent("org.mockito:mockito-core:${libs.versions.mockito.get()}") {
		isTransitive = false
	}
}

// Add an integration test task https://stackoverflow.com/questions/68947203/how-can-i-have-distinct-folders-for-unit-integration-and-end-to-end-tests-in-a
val integrationTest = task<Test>("integrationTest") {
	description = "Runs integration tests."
	group = "verification"

	testClassesDirs = sourceSets["intTest"].output.classesDirs
	classpath = sourceSets["intTest"].runtimeClasspath
	shouldRunAfter("test")
}

tasks.test {
	useJUnitPlatform()
	jvmArgs(
		"-Xshare:off", // Removes an annoying warning: https://github.com/mockito/mockito/issues/3111
		"-javaagent:${mockitoAgent.asPath}") // Manually attaches agent since self-attaching is deprecated
}

tasks.getByName<Test>("integrationTest") {
	useJUnitPlatform()
}

tasks.check { dependsOn(integrationTest) }