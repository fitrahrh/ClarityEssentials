package com.harukiyu.clarityessentials;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class YamlValidationTest {

    @Test
    public void testAllYamlFilesAreValid() throws Exception {
        Path resourcesPath = Paths.get("src", "main", "resources");
        Assertions.assertTrue(Files.exists(resourcesPath), "Resources path should exist");

        Yaml yaml = new Yaml();
        try (Stream<Path> paths = Files.walk(resourcesPath)) {
            List<Path> ymlFiles = paths.filter(p -> p.toString().endsWith(".yml")).toList();
            Assertions.assertFalse(ymlFiles.isEmpty(), "There should be YAML files to test");

            for (Path path : ymlFiles) {
                try (InputStream in = Files.newInputStream(path)) {
                    Object parsed = yaml.load(in);
                    Assertions.assertNotNull(parsed, "YAML file parsed as null: " + path);
                    Assertions.assertInstanceOf(Map.class, parsed, "YAML root must be a map: " + path);
                } catch (Exception e) {
                    Assertions.fail("Failed to parse YAML file " + path + ": " + e.getMessage(), e);
                }
            }
        }
    }
}
