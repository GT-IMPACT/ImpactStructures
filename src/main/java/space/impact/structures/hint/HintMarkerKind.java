package space.impact.structures.hint;

/** Service markers do not use colored block metadata. */
public enum HintMarkerKind {
    GENERIC("impactapi.marker.generic"), AIR("impactapi.marker.air"),
    NOT_AIR("impactapi.marker.not_air"), ERROR("impactapi.marker.error");

    private final String translationKey;

    HintMarkerKind(String translationKey) { this.translationKey = translationKey; }

    public String getTranslationKey() { return translationKey; }
}
