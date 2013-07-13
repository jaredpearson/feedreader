package common.ioc;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic IOC container
 * @author jared.pearson
 */
public class Container {
	private List<ComponentAdapter<?>> componentAdapters = new ArrayList<ComponentAdapter<?>>();
	
	/**
	 * Adds an adapter to the container.
	 */
	public void addAdapter(ComponentAdapter<?> adapter) {
		this.componentAdapters.add(adapter);
	}
	
	/**
	 * Adds a component instance
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addComponent(Object component) {
		this.componentAdapters.add(new InstanceAdapter(component));
	}
	
	/**
	 * Gets the first instance that is assignable to the specified type. If no exact type is found, then
	 * a null reference is returned.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getComponent(Class<T> parameterType) {
		ComponentAdapter<?> selected = null;
		
		for(ComponentAdapter<?> adapter : componentAdapters) {
			if(parameterType.isAssignableFrom(adapter.getComponentClass())) {
				selected = adapter;
				break;
			}
		}
		
		if(selected == null) {
			return null;
		}
		return (T)selected.getComponentInstance(this);
	}
}