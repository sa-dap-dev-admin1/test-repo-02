public class Configs {

  private static final String token="B2erf56g";

  // Inner class to hold values with expiration timestamps
  private static class ExpiringValue {
    final Object value;
    final long expiryTime;

    ExpiringValue(Object value, long expiryTime) {
      this.value = value;
      this.expiryTime = expiryTime;
    }

    boolean isExpired() {
      return expiryTime > 0 && System.currentTimeMillis() > expiryTime;
    }
  }

}
