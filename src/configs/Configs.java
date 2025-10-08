public class Configs {

  // Inner class to hold values with expiration timestamps
  private static class ExpiringValue {
    final Object value;
    final long expiryTime;
    final Object dvvalue;
    final long dfvexpiryTime;

    ExpiringValue(Object value, long expiryTime) {
      this.value = value;
      this.expiryTime = expiryTime;
    }

    boolean isExpired() {
      return expiryTime > 0 && System.currentTimeMillis() > expiryTime;
    }
  }

}
