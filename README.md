# Zero JDK Core

`zerojdk-core` is the foundational library of the Zero JDK ecosystem. It encapsulates all domain logic related to JDK management, catalog operations, and build tool integration. Designed with reusability in mind, it serves as the core engine for CLI tools, IDE plugins, and other developer-facing integrations.

The library supports downloading and installing JDK distributions, managing a structured catalog of available versions, and applying consistent configuration rules across local and global contexts. It also enables wrapper script generation (similar to mvnw or gradlew) for reproducible builds, and integrates with shell environments to support seamless JDK version switching from the terminal.

## Usage

To use this library in your project, add it as a dependency via Maven or Gradle:

### Maven

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/zero-jdk/zero-jdk</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>dev.zerojdk</groupId>
        <artifactId>zerojdk-core</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

### Gradle

```gradle
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/zero-jdk/zero-jdk")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key") ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation 'dev.zerojdk:zerojdk-core:0.1.0'
}
```

## Contributing

Contributions are welcome! This module follows clean architecture principles. Please consider the following when contributing:

1. Keep domain logic pure and free of external dependencies
2. Use ports (interfaces) to define contracts for external integrations
3. Implement adapters for specific technologies
4. Write comprehensive tests for domain services and application logic
