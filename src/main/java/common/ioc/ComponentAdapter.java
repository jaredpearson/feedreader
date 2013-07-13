package common.ioc;

/**
 * The container calls the adapter to provide component instances
 * @author jared.pearson
 */
public interface ComponentAdapter<T> {
	public T getComponentInstance(Container container);
	public Class<T> getComponentClass();
}