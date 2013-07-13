package common.persist;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

public class EntityManager {
	public interface EntityHandler {
		public void persist(QueryContext context, Object entity) throws SQLException;
		public Object get(QueryContext context, Object id) throws SQLException;
		public List<?> executeNamedQuery(QueryContext context, String query, Object... parameters) throws SQLException;
	}
	
	public interface QueryContext {
		public EntityManager getEntityManager();
		public Connection getConnection() throws SQLException;
		public void releaseConnection(Connection cnn) throws SQLException;
	}
	
	private static class QueryContextImpl implements QueryContext, Closeable {
		private EntityManager em;
		private Connection cnn;
		
		public QueryContextImpl(EntityManager em) {
			this.em = em;
		}
		
		@Override
		public Connection getConnection() throws SQLException {
			if(cnn == null) {
				cnn = em.dataSource.getConnection();
			}
			return cnn;
		}
		
		@Override
		public void releaseConnection(Connection cnn) throws SQLException {
		}
		
		@Override
		public void close() throws IOException {
			try {
				DbUtils.close(cnn);
			} catch (SQLException exc) {
				throw new RuntimeException(exc);
			}
		}
		
		@Override
		public EntityManager getEntityManager() {
			return this.em;
		}
	}
	
	private DataSource dataSource;
	private Map<Class<?>, EntityHandler> handlers;
	
	public EntityManager(DataSource dataSource, Map<Class<?>, EntityHandler> handlers) {
		this.dataSource = dataSource;
		this.handlers = handlers;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> entityClass, Object id) {
		QueryContextImpl queryContext = new QueryContextImpl(this);
		try {
			return (T)ensureHandler(entityClass).get(queryContext, id);
		} catch(SQLException exc) {
			throw propagate(exc);
		} finally {
			try {
				queryContext.close();
			} catch (IOException exc) {
				throw propagate(exc);
			}
		}
	}
	
	public void persist(Object entity) {
		QueryContextImpl queryContext = new QueryContextImpl(this);
		try {
			ensureHandler(entity.getClass()).persist(queryContext, entity);
		} catch(SQLException exc) {
			throw propagate(exc);
		} finally {
			try {
				queryContext.close();
			} catch (IOException exc) {
				throw propagate(exc);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> List<T> executeNamedQuery(Class<T> entityClass, String query, Object... parameters) {
		QueryContextImpl queryContext = new QueryContextImpl(this);
		try {
			return (List<T>)ensureHandler(entityClass).executeNamedQuery(queryContext, query, parameters);
		} catch(SQLException exc) {
			throw propagate(exc);
		} finally {
			try {
				queryContext.close();
			} catch (IOException exc) {
				throw propagate(exc);
			}
		}
	}
	
	private EntityHandler ensureHandler(Class<?> entityClass) {
		if(handlers.containsKey(entityClass)) {
			return handlers.get(entityClass);
		} else {
			throw new RuntimeException("Unknown entity type specified: " + entityClass);
		}
	}
	
	private static RuntimeException propagate(Throwable throwable) {
		if(throwable instanceof RuntimeException) {
			throw (RuntimeException) throwable;
		} else {
			throw new RuntimeException(throwable);
		}
	}
}
