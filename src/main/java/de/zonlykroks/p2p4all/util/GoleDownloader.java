package de.zonlykroks.p2p4all.util;

import de.zonlykroks.p2p4all.client.screen.P2PScreen;
import de.zonlykroks.p2p4all.config.P2PConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class GoleDownloader {

    private final OSType type;

    public GoleDownloader() throws Throwable {
        String os = System.getProperty("os.name").toLowerCase();
        OSType osType = null;

        if(os.contains("win")) {
            osType = OSType.WINDOWS;
        }else if(os.contains("mac")) {
            osType = OSType.MAC;
        }else if(os.contains("linux")) {
            osType = OSType.LINUX;
        }

        this.type = osType;

        File configFolder = new File(FabricLoader.getInstance().getConfigDir() + "/p2p4all/gole/");

        if(!configFolder.exists()) {
            boolean couldCreate = configFolder.mkdirs();

            if(!couldCreate) {
                throw new RuntimeException("Could not create config folder, check game permissions!");
            }
        }

        if(Files.list(Path.of(FabricLoader.getInstance().getConfigDir() + "/p2p4all/gole/")).findAny().isPresent()) {
            MinecraftClient.getInstance().setScreen(new P2PScreen());
            return;
        }

        switch (Objects.requireNonNull(osType)) {
            case WINDOWS -> download("https://github.com/shawwwn/Gole/releases/download/v1.2.0/gole-windows-20210217.zip", FabricLoader.getInstance().getConfigDir() + "/p2p4all/gole/gole-win");
            case LINUX -> download("https://github.com/shawwwn/Gole/releases/download/v1.2.0/gole-linux-20210217.zip", FabricLoader.getInstance().getConfigDir() + "/p2p4all/gole/gole-linux");
            case MAC -> download("https://github.com/shawwwn/Gole/releases/download/v1.2.0/gole-darwin-20210217.zip", FabricLoader.getInstance().getConfigDir() + "/p2p4all/gole/gole-darwin");
            default -> throw new RuntimeException("Could not download gole, check internet connection!");
        }

        MinecraftClient.getInstance().setScreen(new P2PScreen());
    }

    private void download(String link, String fileName) throws Throwable
    {
        String zipFileName = fileName + ".zip";
        URL url  = new URL( link );
        HttpURLConnection http = (HttpURLConnection)url.openConnection();
        Map< String, List< String >> header = http.getHeaderFields();
        while( isRedirected( header )) {
            link = header.get( "Location" ).get( 0 );
            url    = new URL( link );
            http   = (HttpURLConnection)url.openConnection();
            header = http.getHeaderFields();
        }
        InputStream input  = http.getInputStream();
        byte[]       buffer = new byte[4096];
        int          n      = -1;
        OutputStream output = new FileOutputStream(zipFileName);
        while ((n = input.read(buffer)) != -1) {
            output.write( buffer, 0, n );
        }
        output.close();

        File extractedFileFolder = new File(fileName);

        if(!extractedFileFolder.exists()) extractedFileFolder.mkdirs();

        byte[] buf = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFileName));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            File newFile = newFile(extractedFileFolder, zipEntry);
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                // fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                // write file content
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buf)) > 0) {
                    fos.write(buf, 0, len);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }

        if(this.type == OSType.WINDOWS) {
            P2PConfig.goleFilePath = extractedFileFolder.getAbsolutePath() + "/gole-windows-amd64.exe";
        }else if(this.type == OSType.LINUX) {
            P2PConfig.goleFilePath = extractedFileFolder.getAbsolutePath() + "/gole-linux-amd64";
        }else if(this.type == OSType.MAC) {
            P2PConfig.goleFilePath = extractedFileFolder.getAbsolutePath() + "/gole-darwin-amd64.exe";
        }

        P2PConfig.write("p2p4all");
    }

    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    private static boolean isRedirected( Map<String, List<String>> header ) {
        for( String hv : header.get( null )) {
            if(   hv.contains( " 301 " )
                    || hv.contains( " 302 " )) return true;
        }
        return false;
    }

    private enum OSType {
        WINDOWS,LINUX,MAC;
    }

}
