package bett.gustavo.rinhaBackend2025Model.model;

public enum SituationPayment {

    QUEUE("On queue"),
    DEFAULT("default processor"),
    FALLBACK("fallback processor");

    private String description;

    SituationPayment(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
