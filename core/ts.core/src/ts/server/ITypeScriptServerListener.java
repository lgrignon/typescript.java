package ts.server;

public interface ITypeScriptServerListener {

	/**
	 * Method called when the given tsserver starts.
	 * 
	 * @param server
	 */
	void onStart(ITypeScriptServiceClient client);

	/**
	 * Method called when the given tsserver stops.
	 * 
	 * @param server
	 */
	void onStop(ITypeScriptServiceClient client);
}
