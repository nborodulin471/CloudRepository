package com.borodulin.cloudrepository.controller;

import com.borodulin.cloudrepository.model.*;
import com.borodulin.cloudrepository.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CloudController {
    private final FileStorageService fileStorageService;

    @PostMapping("/file")
    public void uploadFile(@RequestParam String filename, @RequestParam MultipartFile file, Authentication authentication) throws IOException {
        FileInfo fileInfo = FileDto.createFile(filename, file.getSize(), authentication.getName());
        fileStorageService.uploadFile(fileInfo, file);
    }

    @GetMapping("/list")
    public List<ResFileInfo> getAllFiles(@RequestParam int limit, Authentication authentication) {
        return fileStorageService.getAllFiles(limit, authentication.getName())
                .stream().map(file -> new ResFileInfo(file.getFilename(), file.getSize()))
                .toList();
    }

    @GetMapping("/file")
    public Resource downloadFile(@RequestParam String filename, Authentication authentication) {
        return fileStorageService.getFile(filename, authentication.getName());
    }

    @PutMapping("/file")
    public void putFile(@RequestParam String filename, @RequestBody ReqEditFileName newFileName, Authentication authentication) {
        fileStorageService.editFileName(filename, newFileName.getName(), authentication.getName());
    }

    @DeleteMapping("/file")
    public void deleteFile(@RequestParam String filename, Authentication authentication) {
        fileStorageService.deleteFile(filename, authentication.getName());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ErrorCloudStorage handleExceptions(IllegalArgumentException e) {
        log.error(e.getLocalizedMessage());
        return ErrorCloudStorage.builder()
                .id(UUID.randomUUID().toString())
                .message(e.getLocalizedMessage())
                .build();
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(AuthenticationException.class)
    public ErrorCloudStorage handleExceptions(AuthenticationException e) {
        log.error(e.getLocalizedMessage());
        return ErrorCloudStorage.builder()
                .id(UUID.randomUUID().toString())
                .message(e.getLocalizedMessage())
                .build();
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorCloudStorage handleExceptions(Exception e) {
        log.error(e.getLocalizedMessage());
        return ErrorCloudStorage.builder()
                .id(UUID.randomUUID().toString())
                .message(e.getLocalizedMessage())
                .build();
    }
}
