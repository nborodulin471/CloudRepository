package com.borodulin.cloudrepository.service.util;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@Component
public class FileUploadUtil {
    private static final Path ROOT = Paths.get("files-upload");

    public void saveFile(String fileName, MultipartFile multipartFile) throws IOException {
        if (!Files.exists(ROOT)) {
            Files.createDirectories(ROOT);
        }

        Path filePath;
        try (InputStream inputStream = multipartFile.getInputStream()) {
            filePath = ROOT.resolve(fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ioe) {
            throw new IOException("Не смог сохранить файл: " + fileName, ioe);
        }
    }

    public Optional<Resource> uploadFile(String name) {
        try {
            Path file = ROOT.resolve(name);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return Optional.of(resource);
            } else {
                throw new RuntimeException("Не смог прочитать файл");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Ошибка: " + e.getMessage());
        }
    }

    public void deleteFile(String name) {
        try {
            Path file = ROOT.resolve(name);
            FileSystemUtils.deleteRecursively(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
