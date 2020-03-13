package net.fornwall.jelf;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ReadmeTest {

    @Test
    void versionInReadmeUpToDate() throws IOException {
        String currentVersion = System.getProperty("jelf.version");
        String readmeContent = new String(Files.readAllBytes(Paths.get("./README.md")));
        Assertions.assertTrue(readmeContent.contains("<version>" + currentVersion + "</version>"));
        Assertions.assertTrue(readmeContent.contains("implementation 'net.fornwall:jelf:" + currentVersion));
    }

}
