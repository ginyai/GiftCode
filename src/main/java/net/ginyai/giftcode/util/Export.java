package net.ginyai.giftcode.util;

import net.ginyai.giftcode.GiftCodePlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collection;

public class Export {
    private static Path exportDir;

    public static Path export(Collection<String> codes, String fileName) throws IOException {
        if(exportDir == null){
            exportDir = GiftCodePlugin.getInstance().getConfigDir().resolve("exports");
        }
        if(!exportDir.toFile().exists()){
            exportDir.toFile().mkdirs();
        }
        String time = LocalDateTime.now().toString();
        fileName = (fileName+"_"+time).replaceAll("[\\\\/:*?\"<>|]","_");
        File exportFile = new File(exportDir.toFile(),fileName+".txt");
        int i = 0;
        while (exportFile.exists()){
            exportFile = new File(exportDir.toFile(),fileName+i+++".txt");
        }
        exportFile.createNewFile();
        Writer writer = new FileWriter(exportFile);
        for(String code:codes){
            writer.write(code);
            // \r\n ?
            writer.write('\n');
        }
        writer.close();
        return exportFile.toPath();
    }

}
