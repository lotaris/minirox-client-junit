# rox-client-junit

> JUnit client for [Mini ROX](https://github.com/lotaris/minirox) written in Java.

# rox-client-junit

> JUnit client for [ROX Center](https://github.com/lotaris/rox-center) written in Java.

## Usage

1. Put the following dependency in your pom.xml

```xml
<dependency>
  <groupId>com.lotaris.minirox.client</groupId>
  <artifactId>minirox-client-junit</artifactId>
  <version>2.1.1</version>
</dependenc>
```

2. Configuration with Maven Surefire

```xml
<plugin>
	<groupId>org.apache.maven.plugins</groupId>
	<artifactId>maven-surefire-plugin</artifactId>
	<version>2.18.1</version>
	<configuration>
		<properties>
			<property>
				<name>listener</name>
				<value>com.lotaris.minirox.client.junit.MiniRoxListener</value>
			</property>
		</properties>
	</configuration>
</plugin>
```
### Requirements

* Java 6+

## Contributing

* [Fork](https://help.github.com/articles/fork-a-repo)
* Create a topic branch - `git checkout -b feature`
* Push to your branch - `git push origin feature`
* Create a [pull request](http://help.github.com/pull-requests/) from your branch

Please add a changelog entry with your name for new features and bug fixes.

## License

**minirox-client-junit** is licensed under the [MIT License](http://opensource.org/licenses/MIT).
See [LICENSE.txt](LICENSE.txt) for the full text.
