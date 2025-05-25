package fr.uge.booqin.domain.model.cart;

public interface PriceEstimator {
    double estimatePrice(int books);
    static PriceEstimator useDefault() {
        return bookCount -> bookCount * 6.0;
    }
}
