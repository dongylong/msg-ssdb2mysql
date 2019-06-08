package com.trans.mapper;

import com.trans.bean.Entity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author dongyl
 * @date 16:22 5/3/18
 * @project msg-ssdb2mysql
 */
@Repository
public interface EntityMapper {
    int updateContent(@Param("list") List<Entity> saveBatch);
    List<Entity>  findContentIsNull();
}
