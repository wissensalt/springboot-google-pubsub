package com.wissensalt.springbootgooglepubsub;

public class MyRuntimeException extends RuntimeException {

  private static final long serialVersionUID = 1689841773029951888L;

  public MyRuntimeException(Throwable cause) {
    super(cause);
  }
}
