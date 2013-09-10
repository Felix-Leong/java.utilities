package de.ebf.utils.dao;

import java.io.Serializable;
import java.util.List;

public interface GenericDAOI<T extends Serializable> {

   public T findById(Long id);

   public List<T> findAll();

   public T saveOrUpdate(T entity);

   public void delete(T entity);

   public void deleteById(Long id);

   public void delete(List<T> entities);
}
