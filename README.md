# JElf
Java library for parsing [Executable and Linkable Format (ELF)](https://en.wikipedia.org/wiki/Executable_and_Linkable_Format) files.

[![Build Status](https://github.com/fornwall/jelf/workflows/Java%20CI/badge.svg)](https://github.com/fornwall/jelf/actions?query=workflow%3A%22Java+CI%22)
[![MIT licensed](http://img.shields.io/:license-MIT-blue.svg)](LICENSE.txt)
[![Package on Maven Central](https://img.shields.io/maven-central/v/net.fornwall/jelf)](https://search.maven.org/artifact/net.fornwall/jelf/)
[![javadoc](https://www.javadoc.io/badge/net.fornwall/jelf.svg)](https://www.javadoc.io/doc/net.fornwall/jelf)

## Adding JElf to your build

JElf's Maven group ID is `net.fornwall` and its artifact ID is `jelf`.

To add a dependency on JElf using Maven, use the following:

```xml
<dependency>
    <groupId>net.fornwall</groupId>
    <artifactId>jelf</artifactId>
    <version>0.4.5</version>
</dependency>
```

To add a dependency using Gradle:

```gradle
dependencies {
    implementation 'net.fornwall:jelf:0.4.3'
}
```

## ELF Resources
- [Wikipedia entry on the ELF format](https://en.wikipedia.org/wiki/Executable_and_Linkable_Format)
- [elf(5) man page](http://man7.org/linux/man-pages/man5/elf.5.html)
- [Anatomy of Linux dynamic libraries](https://www.ibm.com/developerworks/library/l-dynamic-libraries/)
