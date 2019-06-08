package com.trans.repository;

import com.trans.bean.Entity;
import org.springframework.data.repository.CrudRepository;

/**
 * @author dongyl
 */
public interface EntityRepository extends CrudRepository<Entity, Long> {
    Entity findAllById(long id);

}
