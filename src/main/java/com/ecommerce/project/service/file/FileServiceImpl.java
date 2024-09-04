package com.ecommerce.project.service.file;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileServiceImpl implements IFileService {
    @Override
    public String uploadImage(String path, MultipartFile file) throws IOException {
        // De bestandsnaam is nodig om te achterhalen welke extensie de file heeft
        String originalFileName = file.getOriginalFilename();

        // Genereer een random unieke file naam zodat deze geen files in de database gaat overschrijven
        // UUID genereert een random string die zo lang en random is dat het vrijwel onmogelijk is dat het twee keer
        // dezelfde string genereert. Omdat de kans op duplicate namen extreem klein is, is het niet nodig om te
        // checken of de naam al bestaat
        String randomId = UUID.randomUUID().toString();
        // Hier wordt de extensie van de originele filenaam toegevoegd aan de randomId. De extensie is altijd de
        // laatste characters na de laatste "." in een filenaam
        String fileName = randomId.concat(originalFileName.substring(originalFileName.lastIndexOf('.')));
        // File.separator wordt gebruikt omdat separators OS afhankelijk zijn. Deze wil je dus nooit hard coded hebben
        String filePath = path + File.separator + fileName;

        // Check of de directory bestaan. Maak deze aan indien het niet het geval is
        File folder = new File(path);
        if (!folder.exists())
            folder.mkdir();

        // Kopieer het bestand naar de door ons aangegeven file path
        Files.copy(file.getInputStream(), Paths.get(filePath));

        return fileName;
    }
}
