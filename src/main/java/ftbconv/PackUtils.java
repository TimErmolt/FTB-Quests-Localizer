package ftbconv;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.*;
import com.google.gson.*;
import org.apache.commons.io.FileUtils;

import static ftbconv.Constants.PackMCMeta.*;

public class PackUtils {
        public static void createResourcePack(File file, String outputName) throws IOException {

            String zipFileName = outputName;
            JsonObject packObject = generatePackMcmeta(DESCRIPTION, PACKFORMAT);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonOutput = gson.toJson(packObject);
            File packMcMeta = new File(FILEPATH);
            FileUtils.write(packMcMeta, jsonOutput.toString(), StandardCharsets.UTF_8);

            try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFileName))) {
                addToZip("assets\\minecraft\\lang\\", file, zipOut);
                addToZip("", packMcMeta, zipOut);

                System.out.println("Zip file created successfully!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private static void addToZip(String path, File file, ZipOutputStream zipOut) throws IOException {
            // Create a FileInputStream for the current file
            try (FileInputStream fis = new FileInputStream(file)) {
                // Create a new ZipEntry
                ZipEntry zipEntry = new ZipEntry(path +file.getName());
                // Put the ZipEntry to the ZipOutputStream
                zipOut.putNextEntry(zipEntry);

                // Write the file contents to the ZipOutputStream
                byte[] bytes = new byte[1024];
                int length;
                while ((length = fis.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }

                // Close the entry
                zipOut.closeEntry();
            }
        }

    public static JsonObject generatePackMcmeta(String description, int packFormat) {
        JsonObject packObject = new JsonObject();
        JsonObject packMeta = new JsonObject();

        packMeta.addProperty("pack_format", packFormat);
        packMeta.addProperty("description", description);
        packObject.add("pack", packMeta);
        return packObject;

    }
}


