package com.harukiyu.clarityessentials;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

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

    @Test
    @SuppressWarnings("unchecked")
    public void testOpenLinkChatDeserialization() throws Exception {
        Path resourcesPath = Paths.get("src", "main", "resources", "languages");
        Yaml yaml = new Yaml();
        net.kyori.adventure.text.minimessage.MiniMessage mm = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage();

        try (Stream<Path> paths = Files.walk(resourcesPath)) {
            List<Path> ymlFiles = paths.filter(p -> p.toString().endsWith(".yml")).toList();
            Assertions.assertEquals(9, ymlFiles.size(), "Should test all 9 language files");

            for (Path path : ymlFiles) {
                try (InputStream in = Files.newInputStream(path)) {
                    Map<String, Object> map = yaml.load(in);
                    Map<String, Object> menu = (Map<String, Object>) map.get("menu");
                    Assertions.assertNotNull(menu, "menu section missing in " + path);
                    Assertions.assertTrue(menu.containsKey("open_link_chat"), "open_link_chat missing in " + path);

                    String raw = (String) menu.get("open_link_chat");
                    // Ensure symbol is removed
                    raw = raw.replace("🌐 ", "").replace("🌐", "");

                    // Replace url directly
                    String replaced = raw.replace("{url}", "https://store.clarity-id.net")
                                         .replace("<url>", "https://store.clarity-id.net");

                    net.kyori.adventure.text.Component comp = mm.deserialize(replaced, 
                        net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.parsed("prefix", "")
                    );

                    String plain = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(comp);
                    Assertions.assertFalse(plain.contains("<click"), "Raw tag <click leaked in " + path.getFileName() + ": " + plain);
                    Assertions.assertFalse(plain.contains("</click>"), "Raw tag </click> leaked in " + path.getFileName() + ": " + plain);
                    Assertions.assertFalse(plain.contains("🌐"), "Symbol 🌐 still in " + path.getFileName() + ": " + plain);
                    Assertions.assertTrue(plain.contains("CLARITY NETWORK STORE"), "Title missing in " + path.getFileName() + ": " + plain);
                    Assertions.assertTrue(plain.contains("https://store.clarity-id.net"), "URL missing in " + path.getFileName() + ": " + plain);

                    // Check that a ClickEvent is present and has the correct URL
                    boolean[] hasClick = new boolean[]{false};
                    findClickEvents(comp, hasClick, "https://store.clarity-id.net");
                    Assertions.assertTrue(hasClick[0], "No ClickEvent found in " + path.getFileName());
                }
            }
        }
    }

    private void findClickEvents(net.kyori.adventure.text.Component c, boolean[] hasClick, String expectedUrl) {
        if (c == null) return;
        Object click = c.clickEvent();
        if (click != null) {
            Assertions.assertTrue(click.toString().toLowerCase().contains("open_url"));
            Assertions.assertTrue(click.toString().contains(expectedUrl));
            hasClick[0] = true;
        }
        for (net.kyori.adventure.text.Component child : c.children()) {
            findClickEvents(child, hasClick, expectedUrl);
        }
    }
}
