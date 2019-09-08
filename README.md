# JElf
JElf is an ELF parsing library in java.

[![Build Status](https://travis-ci.org/fornwall/jelf.svg?branch=master)](https://travis-ci.org/fornwall/jelf)

## Adding JElf to your build

JElf's Maven group ID is `net.fornwall` and its artifact ID is `jelf`.

To add a dependency on JElf using Maven, use the following:

```xml
<dependency>
  <groupId>net.fornwall</groupId>
  <artifactId>jelf</artifactId>
  <version>0.4.2</version>
</dependency>
```

To add a dependency using Gradle:

```gradle
dependencies {
  implementation 'net.fornwall:jelf:0.4.2'
}
```

## ELF Resources
- [Wikipedia entry on the ELF format](https://en.wikipedia.org/wiki/Executable_and_Linkable_Format)
- [elf(5) man page](http://man7.org/linux/man-pages/man5/elf.5.html)
- [Anatomy of Linux dynamic libraries](https://www.ibm.com/developerworks/library/l-dynamic-libraries/)

## Authors
Created and maintained by Fredrik Fornwall ([@fornwall](https://github.com/fornwall)).
