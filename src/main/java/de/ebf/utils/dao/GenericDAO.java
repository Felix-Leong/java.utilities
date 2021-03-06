package de.ebf.utils.dao;

import de.ebf.utils.GenericsUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public abstract class GenericDAO<T extends Serializable> extends GenericsUtils<T> implements GenericDAOI<T> {

   @Autowired
   protected SessionFactory sessionFactory;

   @SuppressWarnings("unchecked")
   @Override
   public T findById(Long id) {
      Session session = sessionFactory.getCurrentSession();
      return (T) session.get(getGenericSuperClass(GenericDAO.class), id);
   }

   @Override
   public List<T> findAll() {
      Session session = sessionFactory.getCurrentSession();
      Criteria criteria = session.createCriteria(getGenericSuperClass(GenericDAO.class));
      criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
      List<T> results = criteria.list();
      if (results == null) {
         return new ArrayList<>();
      }
      return results;
   }

   @Override
   public T saveOrUpdate(T entity) {
      Session session = sessionFactory.getCurrentSession();
      session.saveOrUpdate(entity);
      return entity;
   }

   @Override
   public void delete(T entity) {
      Session session = sessionFactory.getCurrentSession();
      session.delete(entity);
   }

   @Override
   public void deleteById(Long id) {
      delete(findById(id));
   }

   @Override
   public void delete(List<T> entities) {
      Session session = sessionFactory.getCurrentSession();
      for (T entity : entities) {
         session.delete(entity);
      }
   }
}
