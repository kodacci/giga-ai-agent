package pro.ra_tech.giga_ai_agent.core.util;

import lombok.SneakyThrows;
import lombok.val;

import java.nio.charset.StandardCharsets;

public class TestUtils {

    @SneakyThrows
    public static byte[] readResourcesFileBytes(String name) {
        try (val stream = TestUtils.class.getClassLoader().getResourceAsStream(name)) {
            if (stream == null) {
                return new byte[] {};
            }

            return stream.readAllBytes();
        }
    }

    @SneakyThrows
    public static String readResourceFileAsString(String name) {
        return new String(readResourcesFileBytes(name), StandardCharsets.UTF_8);
    }
}
