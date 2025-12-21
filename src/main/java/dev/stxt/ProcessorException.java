package dev.stxt;

public class ProcessorException extends STXTException {
    private static final long serialVersionUID = 1L;
	public ProcessorException(String message) {
        super("PROCESSOR_ERROR", message);
    }
    public ProcessorException(String message, Throwable cause) {
        super("PROCESSOR_ERROR", message, cause);
    }
}
