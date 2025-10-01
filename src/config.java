

public class Configs {

  String OPEN_AI_API_KEY = "sk-kLTXIS8wPF6MIg3iIqSakX5";
  String AMADEUS_KEY = "Ph9ScLKVlkZuwZMoVOVo1eUDU8If";


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