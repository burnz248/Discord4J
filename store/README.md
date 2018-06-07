# Discord4J Store
The `store` module provides a platform for the efficient caching of Discord gateway data.

[Discord4J Stores](https://github.com/Discord4J/Stores) contains some pre-made implementations. 

## Installation
### Gradle
```groovy
repositories {
  maven { url  "https://jitpack.io" }
}

dependencies {
  implementation "com.discord4j.discord4j:discord4j-store:@VERSION@"
}
```
### Maven
```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>com.discord4j.discord4j</groupId>
    <artifactId>discord4j-store</artifactId>
    <version>@VERSION@</version>
  </dependency>
</dependencies>
```