package com.borodulin.cloudrepository.service;

import com.borodulin.cloudrepository.dao.FileInfoDao;
import com.borodulin.cloudrepository.model.FileInfo;
import com.borodulin.cloudrepository.service.util.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileStorageService {
    private final FileUploadUtil fileUploadUtil;
    private final FileInfoDao fileInfoDao;

    @Transactional
    public void uploadFile(FileInfo fileInfo, MultipartFile multipartFile) throws IOException {
        fileUploadUtil.saveFile(fileInfo.getFilename(), multipartFile);
        fileInfoDao.save(fileInfo);
    }

    @Transactional
    public void editFileName(String filename, String newFileName, String owner) {
        FileInfo fileInfo = findFileInfoBy(owner, filename);
        fileInfo.setFilename(newFileName);

        File file = new File("files-upload/" + filename);
        file.renameTo(new File("files-upload/" + newFileName));

        fileInfoDao.save(fileInfo);
    }

    @Transactional
    public void deleteFile(String filename, String owner) {
        FileInfo fileInfo = findFileInfoBy(owner, filename);
        fileUploadUtil.deleteFile(fileInfo.getFilename());
        fileInfoDao.delete(fileInfo);
    }

    public List<FileInfo> getAllFiles(int limit, String owner) {
        return fileInfoDao.findByOwner(owner)
                .stream().limit(limit)
                .toList();
    }

    public Resource getFile(String filename, String owner) {
        String name = findFileInfoBy(owner, filename).getFilename();
        return fileUploadUtil.uploadFile(name)
                .orElseThrow(() -> new IllegalArgumentException("Не найден файл"));
    }

    private FileInfo findFileInfoBy(String owner, String filename) {
        return fileInfoDao.findByOwnerAndFilename(owner, filename).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Не найден файл"));
    }
}
