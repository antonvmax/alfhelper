package ru.alfastrah.site.avto.ws.contact.signed.model.loyalty;

public class LoyaltyData {
    private Long points;
    private LoyaltyStatus status;
    private String percentage;
    private Long clientId;

    public LoyaltyData() {
    }

    public LoyaltyData(Long points, LoyaltyStatus status, String percentage, Long clientId) {
        this.points = points;
        this.status = status;
        this.percentage = percentage;
        this.clientId = clientId;
    }

    public Long getPoints() {
        return points;
    }

    public void setPoints(Long points) {
        this.points = points;
    }

    public LoyaltyStatus getStatus() {
        if (status == null) {
            return LoyaltyStatus.UNKNOWN;
        }
        return status;
    }

    public void setStatus(LoyaltyStatus status) {
        this.status = status;
    }

    public String getPercentage() {
        return percentage;
    }

    public void setPercentage(String percentage) {
        this.percentage = percentage;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getClientId() {
        return this.clientId;
    }

    @Override
    public String toString() {
        return "LoyaltyData{" +
                "points='" + points + '\'' +
                ", status='" + status + '\'' +
                ", percentage='" + percentage + '\'' +
                '}';
    }

    public enum LoyaltyStatus {
        GOLD(3, "GOLD"),
        REGISTERED(2, "Зарегистрированный пользователь"),
        SILVER(1, "SILVER"),
        UNKNOWN(-1, "");
        private final int id;
        private final String name;

        LoyaltyStatus(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public static LoyaltyStatus findByName(String name) {
            for (LoyaltyStatus status : values()) {
                if (status.name.equals(name)) {
                    return status;
                }
            }
            return UNKNOWN;
        }
    }
}
