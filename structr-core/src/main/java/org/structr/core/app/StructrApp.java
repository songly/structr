package org.structr.core.app;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.structr.common.SecurityContext;
import org.structr.common.error.FrameworkException;
import org.structr.core.Command;
import org.structr.core.GraphObject;
import org.structr.core.Services;
import org.structr.core.entity.AbstractNode;
import org.structr.core.entity.Relation;
import org.structr.core.graph.CreateNodeCommand;
import org.structr.core.graph.CreateRelationshipCommand;
import org.structr.core.graph.DeleteNodeCommand;
import org.structr.core.graph.DeleteRelationshipCommand;
import org.structr.core.graph.NodeAttribute;
import org.structr.core.graph.NodeInterface;
import org.structr.core.graph.RelationshipInterface;
import org.structr.core.graph.TransactionCommand;
import org.structr.core.graph.search.SearchCommand;
import org.structr.core.graph.search.SearchNodeCommand;
import org.structr.core.graph.search.SearchRelationshipCommand;
import org.structr.core.property.PropertyMap;

/**
 * Stateful facade for accessing the Structr core layer.
 * 
 * @author Christian Morgner
 */
public class StructrApp implements App {

	private static final Logger logger = Logger.getLogger(StructrApp.class.getName());
	
	private Map<Class<? extends Command>, Command> commandCache = new LinkedHashMap<>();
	private SecurityContext securityContext                     = null;
	
	private StructrApp(final SecurityContext securityContext) {
		
		this.securityContext = securityContext;

		if (!Services.isInitialized()) {

			final Map<String, String> context = new LinkedHashMap<>();
			final String basePath             = System.getProperty("structr.home", "~/.structr");

			logger.log(Level.INFO, "Initializing Structr with base path {0}..", basePath);

			context.put(Services.CONFIGURED_SERVICES, "ModuleService NodeService");
			context.put(Services.TMP_PATH,          "/tmp/");
			context.put(Services.BASE_PATH,         basePath);
			context.put(Services.DATABASE_PATH,     basePath + "/db");
			context.put(Services.FILES_PATH,        basePath + "/files");
			context.put(Services.LOG_DATABASE_PATH, basePath + "/logDb.dat");

			Services.initialize(context);
			
			// wait for initialization
			while (!Services.isInitialized()) {
				
				try { Thread.sleep(100); } catch (Throwable t) {}
			}
			
			// register shutdown hook
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

				@Override
				public void run() {
					
					Services.shutdown();
				}
				
			}));
			
			logger.log(Level.INFO, "Initialization done.");
		}
	
	}
	
	// ----- public methods -----
	@Override
	public <T extends NodeInterface> T create(final Class<T> type, final String name) throws FrameworkException {
		return create(type, new NodeAttribute(AbstractNode.name, name));
	}
	
	@Override
	public <T extends NodeInterface> T create(final Class<T> type, final PropertyMap source) throws FrameworkException {

		final CreateNodeCommand<T> command = command(CreateNodeCommand.class);
		final PropertyMap properties       = new PropertyMap(source);
		
		// add type information when creating a node
		properties.put(AbstractNode.type, type.getSimpleName());
		
		return command.execute(properties);
	}
	
	@Override
	public <T extends NodeInterface> T create(final Class<T> type, final NodeAttribute<?>... attributes) throws FrameworkException {
		
		final List<NodeAttribute<?>> attrs = new LinkedList<>(Arrays.asList(attributes));
		final CreateNodeCommand<T> command = command(CreateNodeCommand.class);
		
		// add type information when creating a node
		attrs.add(new NodeAttribute(AbstractNode.type, type.getSimpleName()));
		
		return command.execute(attrs);
	}

	@Override
	public void delete(final NodeInterface node) throws FrameworkException {
		command(DeleteNodeCommand.class).execute(node);
	}
	
	@Override
	public <T extends Relation> T create(final NodeInterface fromNode, final NodeInterface toNode, final Class<T> relType) throws FrameworkException {
		return command(CreateRelationshipCommand.class).execute(fromNode, toNode, relType);
	}

	@Override
	public <T extends Relation> T create(final NodeInterface fromNode, final NodeInterface toNode, final Class<T> relType, final PropertyMap properties) throws FrameworkException {
		return command(CreateRelationshipCommand.class).execute(fromNode, toNode, relType, properties);
	}

	@Override
	public void delete(final RelationshipInterface relationship) throws FrameworkException {
		command(DeleteRelationshipCommand.class).execute(relationship);
	}

	@Override
	public NodeInterface get(final String uuid) throws FrameworkException {

		final Class<? extends SearchCommand> searchType = SearchNodeCommand.class;

		final Query<NodeInterface> query = new Query<>(securityContext, searchType);
		return query.uuid(uuid).getFirst();
	}

	@Override
	public <T extends GraphObject> T get(final Class<T> type, final String uuid) throws FrameworkException {

		final Class<? extends SearchCommand> searchType = SearchNodeCommand.class;

		final Query<T> query = new Query<>(securityContext, searchType);
		return query.type(type).getFirst();
	}
	
	@Override
	public <T extends GraphObject> List<T> get(final Class<T> type) throws FrameworkException {

		final Class<? extends SearchCommand> searchType = SearchNodeCommand.class;
		
		final Query<T> query = new Query<>(securityContext, searchType);
		return query.type(type).getAsList();
	}
	
	@Override
	public <T extends NodeInterface> Query<T> nodeQuery() {
		return new Query<>(securityContext, SearchNodeCommand.class);
	}
	
	@Override
	public <T extends RelationshipInterface> Query<T> relationshipQuery() {
		return new Query<>(securityContext, SearchRelationshipCommand.class);
	}
	
	@Override
	public void beginTx() {
		command(TransactionCommand.class).beginTx();
	}
	
	@Override
	public void commitTx() throws FrameworkException {
		command(TransactionCommand.class).commitTx(true);
	}
	
	@Override
	public void shutdown() {
		Services.shutdown();
	}
	
	// ----- public static methods ----
	/**
	 * Constructs a new stateful App instance, initialized with the given
	 * security context.
	 * 
	 * @param basePath
	 * @param securityContext
	 * @return 
	 */
	public static App getInstance() {
		return new StructrApp(SecurityContext.getSuperUserInstance());
	}
	
	// ----- public static methods ----
	/**
	 * Constructs a new stateful App instance, initialized with the given
	 * security context.
	 * 
	 * @param basePath
	 * @param securityContext
	 * @return 
	 */
	public static App getInstance(final SecurityContext securityContext) {
		return new StructrApp(securityContext);
	}
	
	// ----- private methods -----
	private <T extends Command> T command(Class<T> commandType) {
		
		Command command = commandCache.get(commandType);
		if (command == null) {
			
			command = Services.command(securityContext, commandType);
			commandCache.put(commandType, command);
		}
		
		return (T)command;
	}
}