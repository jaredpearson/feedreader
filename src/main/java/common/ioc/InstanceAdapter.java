package common.ioc;

/**
 * Adapter that returns an instance
 * @author jared.pearson
 */
class InstanceAdapter<T> implements ComponentAdapter<T> {
	private T instance;
	
	public InstanceAdapter(T instance) {
		this.instance = instance;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<T> getComponentClass() {
		return (Class<T>) instance.getClass();
	}
	
	@Override
	public T getComponentInstance(Container container) {
		return instance;
	}
}