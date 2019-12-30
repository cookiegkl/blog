package com.mtons.mblog.modules.repository;

import com.mtons.mblog.modules.entity.BlogUpload;
import com.mtons.mblog.modules.entity.View;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author Alex
 * @since 2019-06-20
 */
@Repository
public interface BlogUploadRepository extends JpaRepository<BlogUpload, Integer>, JpaSpecificationExecutor<BlogUpload> {

}
