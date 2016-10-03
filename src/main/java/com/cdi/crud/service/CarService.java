/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cdi.crud.service;

import com.cdi.crud.model.Car;
import com.cdi.crud.infra.CrudService;
import com.cdi.crud.infra.exception.CustomException;
import com.cdi.crud.infra.model.Filter;
import com.cdi.crud.infra.security.Admin;
import org.hibernate.Criteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.util.List;

/**
 * @author rmpestano
 *         <p/>
 *         Car Business logic
 */
@Stateless
public class CarService extends CrudService<Car> {


    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Car> listByModel(String model) {
        return crud().ilike("model", model, MatchMode.ANYWHERE).list();

    }

    @Override
    public Criteria configPagination(Filter<Car> filter) {
        if (filter.hasParam("id")) {
            crud().eq("id",
                    Integer.parseInt(filter.getParam("id").toString()));
        }

        // see index.xhtml 'model' column facet name filter
        if (filter.getEntity() != null) {
            if(filter.getEntity().getModel() != null){
                crud().ilike("model", filter.getEntity().getModel(), MatchMode.ANYWHERE);
            }
            if(filter.getEntity().getName() != null){
                crud().ilike("name", filter.getEntity().getName(), MatchMode.ANYWHERE);
            }
        }

        // see index.xhtml 'price' column facet name filter
        if (filter.getEntity() != null && filter.getEntity().getPrice() != null) {
            crud().eq("price", filter.getEntity().getPrice());
        }

        if (filter.hasParam("minPrice") && filter.hasParam("maxPrice")) {
            crud().between("price", (Double) filter.getParam("minPrice"), (Double) filter.getParam("maxPrice"));
        } else if (filter.hasParam("minPrice")) {
            crud().ge("minPrice", (Double) filter.getParam("minPrice"));
        } else if (filter.hasParam("maxPrice")) {
            crud().ge("maxPrice", (Double) filter.getParam("maxPrice"));
        }
        return crud().getCriteria();
    }

    public List<Car> getModels(String query) {
        return crud().criteria().ilike("model", query, MatchMode.ANYWHERE)
                .projection(Projections.property("model")).list();
    }


    @Override
    public void beforeInsert(Car car) {
        if (!car.hasModel()) {
            throw new CustomException("Car model cannot be empty");
        }
        if (!car.hasName()) {
            throw new CustomException("Car name cannot be empty");
        }

        if (crud().eq("name", car.getName()).ne("id", car.getId()).count() > 0) {
            throw new CustomException("Car name must be unique");
        }
    }

    @Override
    public void beforeUpdate(Car entity) {
        this.beforeInsert(entity);
    }


    @Override
    @Admin
    public void remove(Car car) {
        super.remove(car);
    }
}
