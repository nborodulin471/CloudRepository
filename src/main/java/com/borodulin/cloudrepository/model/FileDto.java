package com.borodulin.cloudrepository.model;

public class FileDto {
    public static FileInfo createFile(String fileName, Long size, String owner){
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFilename(fileName);
        fileInfo.setSize(size);
        fileInfo.setOwner(owner);
        return fileInfo;
    }
}
