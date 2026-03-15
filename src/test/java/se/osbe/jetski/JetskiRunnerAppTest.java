package se.osbe.jetski;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JetskiRunnerAppTest {

    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    @Test
    @DisplayName("Ska kunna konvertera giltig YAML till AppConfig")
    void testValidYamlParsing() throws Exception {
        // Arrange: En simulerad inställningsfil
        String yamlContent = """
                scripts:
                  - name: "Test Script"
                    path: "./test.sh"
                    parameters: "-v --force"
                  - name: "Backup"
                    path: "/usr/bin/rsync"
                """;

        // Act: Parsa strängen
        AppConfig config = mapper.readValue(yamlContent, AppConfig.class);

        // Assert: Verifiera att datan mappades korrekt
        assertNotNull(config, "Config-objektet ska inte vara null");
        assertNotNull(config.scripts, "Listan med skript ska inte vara null");
        assertEquals(2, config.scripts.size(), "Det ska finnas två skript i listan");

        // Kontrollera första skriptet
        ScriptConfig firstScript = config.scripts.get(0);
        assertEquals("Test Script", firstScript.name);
        assertEquals("./test.sh", firstScript.path);
        assertEquals("-v --force", firstScript.parameters);

        // Kontrollera andra skriptet (som saknar parametrar i YAML)
        ScriptConfig secondScript = config.scripts.get(1);
        assertEquals("Backup", secondScript.name);
        assertEquals("/usr/bin/rsync", secondScript.path);
        assertNull(secondScript.parameters, "Parametrar ska vara null om de inte anges");
    }

    @Test
    @DisplayName("Ska hantera en helt tom YAML utan att krascha")
    void testEmptyYamlParsing() throws Exception {
        String emptyYaml = "scripts:\n";

        AppConfig config = mapper.readValue(emptyYaml, AppConfig.class);

        assertNotNull(config);
        // Jackson sätter listan till null om den är definierad men saknar innehåll
        assertNull(config.scripts, "Listan ska vara null när inga skript är definierade");
    }

    @Test
    @DisplayName("Dataklasserna kan skapas som tomma objekt")
    void testPOJOCreation() {
        AppConfig appConfig = new AppConfig();
        ScriptConfig scriptConfig = new ScriptConfig();

        assertNull(appConfig.scripts);
        assertNull(scriptConfig.name);
        assertNull(scriptConfig.path);
        assertNull(scriptConfig.parameters);
    }
}