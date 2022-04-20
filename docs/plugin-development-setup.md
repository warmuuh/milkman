for creating a new plugin, you can use following pom:



```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	
	<groupId>...</groupId>
	<artifactId>...</artifactId>
	<version>...</version>
	
	
	<dependencies>
		<dependency>
			<groupId>com.github.warmuuh.milkman</groupId>
			<artifactId>milkman</artifactId>
			<version>...</version>
			<scope>provided</scope>
		</dependency>
		<!-- and for refering to plugins of milkman: -->
		<dependency> 
			<groupId>com.github.warmuuh.milkman</groupId> 
			<artifactId>milkman-rest</artifactId> 
			<version>...</version> 
			<scope>provided</scope>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<!-- for packaging all your dependencies into one jar, excluding provided ones -->
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-assembly-plugin</artifactId>
				<version>2.6</version>
				<configuration>
					<descriptorRefs>
						<descriptorRef>jar-with-dependencies</descriptorRef>
					</descriptorRefs>
					<appendAssemblyId>false</appendAssemblyId>
				</configuration>
				<executions>
					<execution>
						<id>assemble-all</id>
						<phase>package</phase>
						<goals>
							<goal>single</goal>
						</goals>
					</execution>
				</executions>
			</plugin>
		</plugins>
	</build>
	
	
	<repositories>
		<repository>
			<id>jitpack.io</id>
			<url>https://jitpack.io</url>
		</repository>
	</repositories>

</project>
```
