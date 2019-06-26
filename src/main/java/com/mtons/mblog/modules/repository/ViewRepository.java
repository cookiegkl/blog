package com.mtons.mblog.modules.repository;

import com.mtons.mblog.modules.entity.View;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author Alex
 * @since 2019-06-20
 */
public interface ViewRepository extends JpaRepository<View, Long>, JpaSpecificationExecutor<View> {

}
