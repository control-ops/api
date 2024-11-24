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
val mockitoAgent = configurations.create("mockitoAgent")
dependencies {
	implementation("org.springframework.boot:spring-boot-starter-amqp")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.amqp:spring-rabbit-test")
	testImplementation(libs.mockito.core)

	// Used to be the following, but changed due to an annoying IntelliJ warning: mockitoAgent(libs.mockito.core) { isTransitive = false }
	mockitoAgent("org.mockito:mockito-core:${libs.versions.mockito.get()}") {
		isTransitive = false
	}

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testRuntimeOnly("com.h2database:h2") // A database is required by the applicationcontext for tests to pass; switch out later for PostgresSQL
}

tasks.withType<Test> {
	useJUnitPlatform()
	jvmArgs(
		"-Xshare:off", // Removes an annoying warning: https://github.com/mockito/mockito/issues/3111
		"-javaagent:${mockitoAgent.asPath}") // Manually attaches agent since self-attaching is deprecated
}