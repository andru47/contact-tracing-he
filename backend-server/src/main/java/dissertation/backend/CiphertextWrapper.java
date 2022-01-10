package dissertation.backend;

public class CiphertextWrapper {
  private char[] computedCiphertext1, computedCiphertext2;

  public void setComputedCiphertext1(char[] computedCiphertext1) {
    this.computedCiphertext1 = computedCiphertext1;
  }

  public void setComputedCiphertext2(char[] computedCiphertext2) {
    this.computedCiphertext2 = computedCiphertext2;
  }

  public char[] getComputedCiphertext1() {
    return computedCiphertext1;
  }

  public char[] getComputedCiphertext2() {
    return computedCiphertext2;
  }
}
