package model;

public record EconomicIndex(
        String indexCode,
        String indexName,
        String countryCode,
        String countryName,
        String period,
        double value,
        String unit,
        String source) {
}
