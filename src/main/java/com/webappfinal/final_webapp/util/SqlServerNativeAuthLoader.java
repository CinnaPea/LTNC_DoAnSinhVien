package com.webappfinal.final_webapp.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SqlServerNativeAuthLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(SqlServerNativeAuthLoader.class);
    private static final String[] WINDOWS_AUTH_DLLS = {
        "mssql-jdbc_auth-13.4.0.x64.dll",
        "mssql-jdbc_auth-12.10.2.x64.dll"
    };
    private static volatile boolean loaded;

    private SqlServerNativeAuthLoader() {
    }

    public static void loadIfAvailable() {
        if (loaded || !isWindows64Bit()) {
            return;
        }

        for (String dllName : WINDOWS_AUTH_DLLS) {
            try (InputStream dllStream = SqlServerNativeAuthLoader.class.getResourceAsStream("/native/" + dllName)) {
                if (dllStream == null) {
                    continue;
                }

                Path nativeDir = Files.createTempDirectory("sqljdbc-native-auth");
                nativeDir.toFile().deleteOnExit();
                Path extractedDll = nativeDir.resolve(dllName);
                extractedDll.toFile().deleteOnExit();
                Files.copy(dllStream, extractedDll, StandardCopyOption.REPLACE_EXISTING);
                System.load(extractedDll.toAbsolutePath().toString());
                loaded = true;
                LOGGER.info("Loaded SQL Server native authentication DLL from bundled resources: {}", dllName);
                return;
            } catch (UnsatisfiedLinkError | IOException ex) {
                LOGGER.warn("Failed to load SQL Server native authentication DLL {}.", dllName, ex);
            }
        }

        LOGGER.warn("SQL Server native auth DLL was not found on the classpath.");
    }

    private static boolean isWindows64Bit() {
        String osName = System.getProperty("os.name", "").toLowerCase();
        String osArch = System.getProperty("os.arch", "").toLowerCase();
        return osName.contains("win") && osArch.contains("64");
    }
}
