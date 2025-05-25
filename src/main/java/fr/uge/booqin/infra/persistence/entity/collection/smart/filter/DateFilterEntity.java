package fr.uge.booqin.infra.persistence.entity.collection.smart.filter;

import fr.uge.booqin.domain.model.collection.smart.filter.DateFilterModel;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.Objects;


@Entity
@Table(name = "date_filters")
public class DateFilterEntity extends SmartCollectionFilterEntity {

    private LocalDate date;
    private FilterLogic logic;

    public DateFilterEntity() {
    }

    public DateFilterEntity(LocalDate date, FilterLogic logic) {
        this.date = Objects.requireNonNull(date, "date is required");
        this.logic = Objects.requireNonNull(logic, "logic is required");
    }

    public FilterLogic getLogic() {
        return logic;
    }

    public void setLogic(FilterLogic logic) {
        this.logic = logic;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public DateFilterModel convertTo() {
        return new DateFilterModel(date, logic);
    }
}