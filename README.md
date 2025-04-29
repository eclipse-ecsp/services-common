[<img src="./images/logo.png" width="400" height="200"/>](./images/logo.png)

# services-common
[![Maven Build & Sonar Analysis](https://github.com/eclipse-ecsp/services-common/actions/workflows/maven-build.yml/badge.svg)](https://github.com/eclipse-ecsp/services-common/actions/workflows/maven-build.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=eclipse-ecsp_services-common&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=eclipse-ecsp_services-common)
[![License Compliance](https://github.com/eclipse-ecsp/services-common/actions/workflows/licence-compliance.yaml/badge.svg)](https://github.com/eclipse-ecsp/services-common/actions/workflows/licence-compliance.yaml)
[![Latest Release](https://img.shields.io/github/v/release/eclipse-ecsp/services-common?sort=semver)](https://github.com/eclipse-ecsp/services-common/releases)

services-common is a Java library that is used in stream processor microservices. It provides some common functionality.

* SettingsManagerClient for integration with Settingmgmt microservice
* VehicleProfileClient for integration with Vehicle-Profile microservice
* Custom Codec for MongoDB
* JsonMapperUtils for providing JSON Serialization and deserialization with custom filter
* JSON validator to validate JSON with JSON schema

# Table of Contents

* [Getting Started](#getting-started)
* [Usage](#usage)
* [How to contribute](#how-to-contribute)
* [Built with Dependencies](#built-with-dependencies)
* [Code of Conduct](#code-of-conduct)
* [Authors](#authors)
* [Security Contact Information](#security-contact-information)
* [Support](#support)
* [Troubleshooting](#troubleshooting)
* [License](#license)
* [Announcements](#announcements)
* [Acknowledgments](#acknowledgments)

## Getting Started

To build the project in the local working directory after the project has been cloned/forked, run the following from the command line interface.

```mvn clean install```

### Prerequisites

* [Java jdk 17+](https://jdk.java.net/archive/)
* [Maven 3.6](https://maven.apache.org/)

#### dependencies on other modules

* Parent POM: A version of other modules and a third-party library are in services-dependencies.

```xml
 <parent>
    <groupId>org.eclipse.ecsp</groupId>
    <artifactId>services-dependencies</artifactId>
    <version>1.0.XX</version> <!-- release version -->
    <relativePath/> <!-- lookup parent from repository -->
  </parent>
```

* Ignite Cache

```xml
 <dependency>
    <groupId>org.eclipse.ecsp</groupId>
    <artifactId>cache-enabler</artifactId>
 </dependency>
```

* NoSQL DAO

```xml
  <dependency>
    <groupId>org.eclipse.ecsp</groupId>
    <artifactId>nosql-dao</artifactId>
  </dependency>
```

* Utils

```xml
  <dependency>
    <groupId>org.eclipse.ecsp</groupId>
    <artifactId>utils</artifactId>
  </dependency>
```

### Installation

A step-by-step series of examples that describe how to get a local development environment running.

Step 1: build

```shell
$ mvn -s settings.xml clean install -Dmaven.test.skip=<true/false>
```

Step 2 : Release

```shell
$ mvn -s settings.xml -B release:prepare -Darguments="-Dmaven.test.skip=true  -DpushImage" -DreleaseVersion=<RELEASE_VERSION> -DdevelopmentVersion=<SNAPSHOT_VERSION>-SNAPSHOT
$ mvn -s settings.xml release:perform"
```

Step 3 : add services-common dependency to sp microservices

```xml
  <dependency>
    <groupId>org.eclipse.ecsp</groupId>
    <artifactId>services-common</artifactId>
    <version>3.0.XX</version> <!-- release version -->
  </dependency>
```

#### Coding style check configuration
[checkstyle.xml](./checkstyle.xml) is the HARMAN coding standard for writing new code or updating existing code.

Checkstyle plugin [maven-checkstyle-plugin:3.3.1](https://maven.apache.org/plugins/maven-checkstyle-plugin/) is integrated in [pom.xml](./pom.xml) which runs in the `validate` phase and `check` goal of the maven lifecycle and fails the build if there are any checkstyle errors in the project.

### Running the Tests

Execute the Unit Test cases

```shell
mvn test
```

## Usage

Library Usage Document

Add services-common dependency to SP microservices.

```xml
  <dependency>
    <groupId>org.eclipse.ecsp</groupId>
    <artifactId>services-common</artifactId>
    <version>3.0.XX</version> <!-- release version -->
</dependency>
```

#### VehicleProfileClient:
The VehicleProfileClient provides a utility method to interact and fetch information from Vehicle Profile microservices. It helps to maintain and fetch the Vehicle Profile details.

configuration
```properties
http.vp.url=
http.associated.vehicles.url=
http.vehicles.url=
http.disassociate.vehicle.url=
http.associate.vehicle.url=
service.name=servicename
svl.status.jsonpath=$.active
```
code
```java
    @Autowire
    private VehicleProfileClient vehicleProfileClient;
    
    // get vehicle profile by ecu clientId
    vehicleProfileClient.getVehicleProfileAttrWithClientId(vehicleId,vehicleProfileAttributes);
```

#### SettingsManagerClient:
The SettingsManagerClient provides a utility method to interact and fetch information from Settingsmgmt microservices. It helps to maintain and fetch the Vehicle eCall and  bCall stocking rules and configurations.


configuration
```properties
http.sm.url=
```

code
```java
    @Autowire
    private SettingsManagerClient settingsManagerClient;
    
    // Fetch settings management configuration for a vehicle.
    settingsManagerClient.getSettingsManagerConfigurationObject(userId, vehicleId, settingId, configKey);
```

#### RestTemplate configuration:
```
rest.client.read.timeout=3000
rest.client.write.timeout=3000
rest.client.connection.timeout=3000
rest.client.connection.request.timeout=3000
rest.client.okhttp.pool.maxidleconnections=5
rest.client.okhttp.pool.keepaliveduration.ms=30000
rest.client.max.conn.total=20
rest.client.max.conn.per.route=2
rest.client.type=default // default, okhttp
```

#### JsonValidator
The JsonValidator provides utility methods to validate any JSON payload against a JSON schema.

configuration
```properties
json.schemas={key1:file.json, key2:'classpath:/schema/file2.json',key3:'file:/var/local/schema/file3.json',key4:'http://example.com/schema/file3.json'}
```
code
```java

    @Autowire
    private JsonValidator jsonValidator;

    //Validate json against the json schema.
    jsonValidator.validate(schemaKey, jsonToValidate);

```

#### ServiceUtil
The ServiceUtil used by Stream processors to add additional headers during outbound calls, calculate event TTL, and fetch associated user to the vehicle profile.

configuration
```properties
outboud.api.additional.headers=key1:value1, key2:value2
outbound.api.headers.values.lookup={type1:'x-api-key:k2hvag5,content-type:application/json',type3:'x-api-key:pppp,content-type:application/json'}
vehicle.owner.role=VO
```
code
```java
    @Autowire ServiceUtil serviceUtil;

    // create igniteEvent
    IgniteEvent igniteEvent = serviceUtil.createIgniteEvent(version, eventId, vehicleId, eventData);
    
    // Calculate TTL based on provided input.
    // ((numberOfRetry * maxInterval) + bufferInterval + eventTimestamp)
    long ttl = serviceUtil.getEventTtl(long numberOfRetry, long maxInterval, long bufferInterval,
    long eventTimestamp)

    // Fetch user id from vehicle profile authorizedUsers and create UserContext.
    List<UserContext> users = serviceUtil.getUserContextInfo()
```


## Built With Dependencies

* [Spring Boot](https://spring.io/projects/spring-boot/) - The web framework used
* [Maven](https://maven.apache.org/) - Dependency Management

## How to contribute

See [CONTRIBUTING.md](./CONTRIBUTING.md) for details about our contribution
guidelines and the process for submitting pull requests to us.

## Code of Conduct

See [CODE_OF_CONDUCT.md](./CODE_OF_CONDUCT.md) for details about our code of conduct
and the process for submitting pull requests to us.

## Authors

* **Eugene Jiang** - *Initial work*
* **Abhishek Kumar** - *Initial work*
* **Charles Zhu** - *Initial work*
* **Leon Chen** - *Initial work*
* **Shubhangi Shukla** - *Initial work*
* **Padmaja Ainapure** - *Initial work*

For a list contributors to this project, see the [list of [contributors](https://github.com/eclipse-ecsp/services-common/contributors).

## Security Contact Information

See [SECURITY.md](./SECURITY.md) to raise any security related issues

## Support

Contact the project developers via the project's "dev" list - https://accounts.eclipse.org/mailing-list/ecsp-dev

## Troubleshooting

See [CONTRIBUTING.md](./CONTRIBUTING.md) for details about raising issues and submitting pull requests.

## License

This project is licensed under the Apache-2.0 License. See the [LICENSE](./LICENSE) file for details.

## Announcements

All updates to this library are documented in [releases](https://github.com/eclipse-ecsp/services-common/releases)
For the available versions, see the [tags on this repository](https://github.com/eclipse-ecsp/services-common/tags).
