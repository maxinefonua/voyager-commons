package org.voyager.error;

public class ServiceException extends RuntimeException {
  public ServiceException(String message) {
    super(message);
  }
}
