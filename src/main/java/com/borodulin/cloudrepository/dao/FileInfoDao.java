package com.borodulin.cloudrepository.dao;

import com.borodulin.cloudrepository.model.FileInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileInfoDao extends JpaRepository<FileInfo, Long> {
    List<FileInfo> findByOwner(String owner);
    List<FileInfo> findByOwnerAndFilename(String owner, String file);

}
