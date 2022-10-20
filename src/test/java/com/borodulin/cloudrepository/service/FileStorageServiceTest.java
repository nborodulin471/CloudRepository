package com.borodulin.cloudrepository.service;

import com.borodulin.cloudrepository.dao.FileInfoDao;
import com.borodulin.cloudrepository.model.FileInfo;
import com.borodulin.cloudrepository.service.util.FileUploadUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class FileStorageServiceTest {
    private static final String FILE_NAME = "filename";
    private static final String OWNER = "user";

    private FileInfo fileInfo;

    @Mock
    private FileInfoDao fileInfoDao;
    @Mock
    private FileUploadUtil fileUploadUtil;

    @InjectMocks
    private FileStorageService sut;

    @BeforeEach
    void setUp() {
        openMocks(this);
        fileInfo = createFileInfo();
    }

    @Test
    void uploadFile() throws IOException {
        MockMultipartFile multipartFile = new MockMultipartFile(FILE_NAME, "test.txt",
                "text/plain", "test".getBytes());
        when(fileInfoDao.save(any())).thenReturn(fileInfo);
        doNothing().when(fileUploadUtil).saveFile(any(), any());

        sut.uploadFile(fileInfo, multipartFile);

        verify(fileUploadUtil).saveFile(fileInfo.getFilename(), multipartFile);
        verify(fileInfoDao).save(fileInfo);
        verifyNoMoreInteractions(fileInfoDao);
    }

    @Test
    void editFileName() {
        String newFileName = "newFileName";
        when(fileInfoDao.findByOwnerAndFilename(any(), any())).thenReturn(List.of(fileInfo));
        when(fileInfoDao.save(any())).thenReturn(fileInfo);

        sut.editFileName(FILE_NAME, newFileName, OWNER);

        assertEquals(newFileName, fileInfo.getFilename());
        verify(fileInfoDao).findByOwnerAndFilename(OWNER, FILE_NAME);
        verify(fileInfoDao).save(fileInfo);
        verifyNoMoreInteractions(fileInfoDao);
        verifyNoInteractions(fileUploadUtil);
    }

    @Test
    void deleteFile() {
        doNothing().when(fileUploadUtil).deleteFile(fileInfo.getFilename());
        doNothing().when(fileInfoDao).delete(fileInfo);
        when(fileInfoDao.findByOwnerAndFilename(any(), any())).thenReturn(List.of(fileInfo));

        sut.deleteFile(FILE_NAME, OWNER);

        verify(fileInfoDao).findByOwnerAndFilename(OWNER, FILE_NAME);
        verify(fileUploadUtil).deleteFile(fileInfo.getFilename());
        verify(fileInfoDao).delete(fileInfo);
        verifyNoMoreInteractions(fileUploadUtil, fileInfoDao);
    }

    @Test
    void getAllFiles() {
        when(fileInfoDao.findByOwner(any())).thenReturn(List.of(fileInfo));

        sut.getAllFiles(10, OWNER);

        verify(fileInfoDao).findByOwner(OWNER);
        verifyNoMoreInteractions(fileInfoDao);
        verifyNoInteractions(fileUploadUtil);
    }

    @Test
    void getFile() {
        when(fileInfoDao.findByOwnerAndFilename(any(), any())).thenReturn(List.of(fileInfo));
        when(fileUploadUtil.uploadFile(any())).thenReturn(Optional.of(getTestFile()));

        sut.getFile(FILE_NAME, OWNER);

        verify(fileInfoDao).findByOwnerAndFilename(OWNER, FILE_NAME);
        verify(fileUploadUtil).uploadFile(FILE_NAME);
        verifyNoMoreInteractions(fileInfoDao, fileUploadUtil);
    }

    private FileInfo createFileInfo() {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFilename(FILE_NAME);
        return fileInfo;
    }

    public static Resource getTestFile() {
        Path file = Paths.get("src/test/resources").resolve("test.txt");
        return new FileSystemResource(file);
    }
}
