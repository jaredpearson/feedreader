package feedreader;

public class Providers {
	private Providers() {
	}
	
	public static <T> Provider<T> forInstance(final T value) {
		return new Provider<T>() {
			@Override
			public T get() {
				return value;
			}
		};
	}
}
