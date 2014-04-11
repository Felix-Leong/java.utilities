package de.ebf.onpremise;

/**
 *
 * @author xz
 */
public class SetupWizardMessage {

    public final static int SUCCESS = 0;
    public final static int ERROR = 1;
    private int code;
    private String error;

    public SetupWizardMessage(int code, String error) {
        this.code = code;
        this.error = error;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

}
