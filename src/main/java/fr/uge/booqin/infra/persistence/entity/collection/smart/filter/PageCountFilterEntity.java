package fr.uge.booqin.infra.persistence.entity.collection.smart.filter;

import fr.uge.booqin.domain.model.collection.smart.filter.PageCountFilterModel;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.Objects;


@Entity
@Table(name = "page_count_filters")
public class PageCountFilterEntity extends SmartCollectionFilterEntity {
    private int number;
    private FilterLogic logic;

    public PageCountFilterEntity() {
    }

    public PageCountFilterEntity(int number, FilterLogic logic) {
        this.number = number;
        this.logic = Objects.requireNonNull(logic, "logic is required");
    }

    public FilterLogic getLogic() {
        return logic;
    }

    public void setLogic(FilterLogic logic) {
        this.logic = logic;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public PageCountFilterModel convertTo() {
        return new PageCountFilterModel(number, logic);
    }
}