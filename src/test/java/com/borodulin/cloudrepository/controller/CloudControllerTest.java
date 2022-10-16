package com.borodulin.cloudrepository.controller;

import com.borodulin.cloudrepository.model.FileInfo;
import com.borodulin.cloudrepository.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CloudControllerTest {
    private static final String FILE_NAME = "filename";
    private static final String OWNER = "spring";
    private static final FileInfo FILE_INFO = createFileInfo();

    @MockBean
    private FileStorageService fileStorageService;

    @Autowired
    private MockMvc mockMvc;

    private static FileInfo createFileInfo() {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFilename(FILE_NAME);
        fileInfo.setSize(4L);
        fileInfo.setOwner(OWNER);
        return fileInfo;
    }

    @Test
    @WithMockUser(value = "spring")
    void getFile() throws Exception {
        Path file = Paths.get("src/test/resources").resolve("test.txt");
        Resource resource = new UrlResource(file.toUri());
        when(fileStorageService.getFile(any(), any())).thenReturn(resource);

        MvcResult result = mockMvc.perform(get("/file?filename=" + FILE_NAME))
                .andExpect(status().isOk())
                .andReturn();

        assertFalse(result.getResponse().getContentAsString().isEmpty());
        verify(fileStorageService).getFile(FILE_NAME, OWNER);
        verifyNoMoreInteractions(fileStorageService);
    }

    @Test
    @WithMockUser(value = "spring")
    void getList() throws Exception {
        String excepted = "[{" +
                "\"filename\":\"filename\"," +
                "\"size\":4" +
                "}]";
        when(fileStorageService.getAllFiles(anyInt(), anyString())).thenReturn(List.of(FILE_INFO));

        MvcResult result = mockMvc.perform(get("/list?limit=10"))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(excepted, result.getResponse().getContentAsString());
        verify(fileStorageService).getAllFiles(10, OWNER);
        verifyNoMoreInteractions(fileStorageService);
    }

    @Test
    @WithMockUser(value = "spring")
    void uploadFile() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(FILE_NAME, "test.txt",
                "text/plain", "test".getBytes());
        doNothing().when(fileStorageService).uploadFile(any(), any());

        mockMvc.perform(multipart(HttpMethod.POST, "/file?filename=" + FILE_NAME)
                        .file("file", multipartFile.getBytes()))
                .andExpect(status().isOk());

        verify(fileStorageService).uploadFile(any(FileInfo.class), any());
        verifyNoMoreInteractions(fileStorageService);
    }

    @Test
    @WithMockUser(value = "spring")
    void putFile() throws Exception {
        String newFileName = "newFileName";
        doNothing().when(fileStorageService).editFileName(any(), any(), any());

        mockMvc.perform(put("/file?filename=" + FILE_NAME)
                        .contentType(APPLICATION_JSON)
                        .content("{" +
                                "  \"name\": \"newFileName\"" +
                                "}"))
                .andExpect(status().isOk());

        verify(fileStorageService).editFileName(FILE_NAME, newFileName, OWNER);
        verifyNoMoreInteractions(fileStorageService);
    }

    @Test
    @WithMockUser(value = "spring")
    void deleteFile() throws Exception {
        doNothing().when(fileStorageService).deleteFile(any(), any());

        mockMvc.perform(delete("/file?filename=" + FILE_NAME))
                .andExpect(status().isOk());

        verify(fileStorageService).deleteFile(FILE_NAME, OWNER);
        verifyNoMoreInteractions(fileStorageService);
    }

    @Test
    @WithMockUser(value = "spring")
    void handleExceptions() throws Exception {
        when(fileStorageService.getFile(any(), any())).thenThrow(IllegalArgumentException.class);

        MvcResult result = mockMvc.perform(get("/file?filename=" + FILE_NAME))
                .andExpect(status().is4xxClientError())
                .andReturn();

        assertFalse(result.getResponse().getContentAsString().isEmpty());
        verify(fileStorageService).getFile(FILE_NAME, OWNER);
        verifyNoMoreInteractions(fileStorageService);
    }
}