package fr.uge.booqin.domain.model.books;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ImageFormatModel {
    @Column(length = 512)
    private String small;

    @Column(length = 512)
    private String medium;

    @Column(length = 512)
    private String large;

    public ImageFormatModel() {}

    public ImageFormatModel(String small, String medium, String large) {
        this.small = small;
        this.medium = medium;
        this.large = large;
    }

    public String getLarge() {
        return large;
    }

    public void setLarge(String large) {
        this.large = large;
    }

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }

    public String getSmall() {
        return small;
    }

    public void setSmall(String small) {
        this.small = small;
    }

}
