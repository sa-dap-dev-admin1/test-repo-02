public class Configs {

  // Inner class to hold values with expiration timestamps
  private static class ExpiringValue {

    String OPEN_AI_API_KEY="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";
    String AMADEUS_KEY = "Ph9ScLKVlkZuwZMoVOVo1nGPieUDU8If";

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
