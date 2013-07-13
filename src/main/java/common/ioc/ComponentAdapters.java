package common.ioc;

public class ComponentAdapters {
	private ComponentAdapters() {
	}
	
	public static <T> ComponentAdapter<T> forInstance(T value) {
		return new InstanceAdapter<T>(value);
	}
	
	public static <T> ComponentAdapter<T> asSingleton(final ComponentAdapter<T> adapter) {
		return new ComponentAdapter<T>() {
			private T singleton;
			private boolean retrieved = false;
			
			@Override
			public Class<T> getComponentClass() {
				return adapter.getComponentClass();
			}
			
			@Override
			public T getComponentInstance(Container container) {
				if(!retrieved) {
					retrieved = true;
					singleton = adapter.getComponentInstance(container);
				}
				return singleton;
			}
		};
	}
}
