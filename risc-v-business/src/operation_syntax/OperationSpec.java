package operation_syntax;

import java.util.Map;

public interface OperationSpec {

	Map<String, Operation> getOperations();
	Operation opOf(String op);
	public boolean isOperation(String op) throws OperationException;
	
}
