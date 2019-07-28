package milkman.plugin.sync.git;


import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import de.danielbechler.diff.ObjectDifferBuilder;
import de.danielbechler.diff.comparison.PrimitiveDefaultValueMode;
import de.danielbechler.diff.identity.IdentityStrategy;
import de.danielbechler.diff.node.DiffNode;
import de.danielbechler.diff.node.Visit;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import milkman.domain.Collection;
import milkman.domain.Environment;
import milkman.domain.RequestContainer;

public class CollectionDiffer {

	/**
	 * this class->subClass mechanic is needed because java-object-diff does not support 
	 * defining identity strategies for generic super-classes but only the concrete types.
	 * TODO: once fixed/introduced in java-object-diff, this needs to be removed
	 */
	static Map<Class, List<Class<?>>> classToSubclassCache = new HashMap<>();
	static List<Class<?>> subtypesOf(Class clazz){
		if (classToSubclassCache.containsKey(clazz))
			return classToSubclassCache.get(clazz);
		
		
		List<Class<?>> typesOf = getSubTypesOf(clazz.getName());
		classToSubclassCache.put(clazz, typesOf);
		
		return typesOf;
	}
	
	
	static List<Class<?>> getSubTypesOf(String superclassName) {
	    try (ScanResult scanResult = new ClassGraph()
	    							 	.enableClassInfo().enableAnnotationInfo()
							 			.ignoreClassVisibility()
							 			.blacklistModules("*")
							 			.scan()) {
	        ClassInfoList classInfoList = scanResult.getSubclasses(superclassName);
	        return classInfoList.loadClasses();
	    }
	}

	
	
	
	@RequiredArgsConstructor
	private final class PropertyBasedIdentityStrategy implements IdentityStrategy {
		private final String propertyName;
		@Override
		@SneakyThrows
		public boolean equals(Object working, Object base) {
			Optional<Object> workingId = extractPropertyValue(working);
			Optional<Object> baseId = extractPropertyValue(base);
			return Objects.equals(baseId, workingId);
		}
		private Optional<Object> extractPropertyValue(Object obj)
				throws IntrospectionException, IllegalAccessException, InvocationTargetException {
			for (PropertyDescriptor descriptor : Introspector.getBeanInfo(obj.getClass()).getPropertyDescriptors()) {
				if (descriptor.getName().equals(propertyName))
					return Optional.ofNullable(descriptor.getReadMethod().invoke(obj));
			}
			
			return Optional.empty();
		}
	}

	@RequiredArgsConstructor
	private final class TypeBasedIdentityStrategy implements IdentityStrategy {
		@Override
		@SneakyThrows
		public boolean equals(Object working, Object base) {
			return Objects.equals(working.getClass(), base.getClass());
		}
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class DataHolder {
		List<Collection> collections;
	}
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class DataHolderEnv {
		List<Environment> environments;
	}
	
	public DiffNode compare(List<Collection> working, List<Collection> base) throws JsonParseException, JsonMappingException, IOException {
		ObjectDifferBuilder diffBuilder = ObjectDifferBuilder.startBuilding()
//				.comparison()
//					.ofType(RestRequestContainer.class).toUseEqualsMethodOfValueProvidedByMethod("getId")
//					.ofType(Collection.class).toUseEqualsMethodOfValueProvidedByMethod("getName")
//				.and()
				.inclusion()
					.exclude().propertyName("onDirtyChange")
							  .propertyName("onInvalidate")
				.and();
		
		
		registerSubtypeIdentityStrategies(diffBuilder, RequestContainer.class, "aspects", new TypeBasedIdentityStrategy());
		
		DiffNode diffNode = diffBuilder
				.identity().ofCollectionItems(DataHolder.class, "collections").via(new PropertyBasedIdentityStrategy("id"))
						   .ofCollectionItems(Collection.class, "requests").via(new PropertyBasedIdentityStrategy("id"))
						   .ofCollectionItems(Collection.class, "folders").via(new PropertyBasedIdentityStrategy("id"))
				.and()
				.comparison()
					.ofPrimitiveTypes().toTreatDefaultValuesAs(PrimitiveDefaultValueMode.ASSIGNED)
				.and()
				.build()
				.compare(new DataHolder(working), new DataHolder(base));
		
		return diffNode;
	}
	
	public DiffNode compareEnvs(List<Environment> working, List<Environment> base) throws JsonParseException, JsonMappingException, IOException {
		ObjectDifferBuilder diffBuilder = ObjectDifferBuilder.startBuilding()
//				.comparison()
//					.ofType(RestRequestContainer.class).toUseEqualsMethodOfValueProvidedByMethod("getId")
//					.ofType(Collection.class).toUseEqualsMethodOfValueProvidedByMethod("getName")
//				.and()
				.inclusion()
//					.exclude().propertyName("onDirtyChange")
//							  .propertyName("onInvalidate")
				.and();
		
		DiffNode diffNode = diffBuilder
				.identity().ofCollectionItems(DataHolderEnv.class, "environments").via(new PropertyBasedIdentityStrategy("id"))
						   .ofCollectionItems(Environment.class, "entries").via(new PropertyBasedIdentityStrategy("id"))
				.and()
				.comparison()
					.ofPrimitiveTypes().toTreatDefaultValuesAs(PrimitiveDefaultValueMode.ASSIGNED)
				.and()
				.build()
				.compare(new DataHolderEnv(working), new DataHolderEnv(base));
		
		return diffNode;
	}
	private void registerSubtypeIdentityStrategies(ObjectDifferBuilder diffBuilder, Class type, String property, IdentityStrategy strat) {
		for (Class<?> subType : subtypesOf(type)) {
			diffBuilder.identity().ofCollectionItems(subType, property).via(strat);
		}
	}

	public void mergeDiffs(List<Collection> mergeSource, List<Collection> mergeTarget, DiffNode diffs) {
		val merger = new MergingDifferenceVisitor<>(new DataHolder(mergeTarget), new DataHolder(mergeSource));
		diffs.visit(merger);
	}
	public void mergeDiffsEnvs(List<Environment> mergeSource, List<Environment> mergeTarget, DiffNode diffs) {
		val merger = new MergingDifferenceVisitor<>(new DataHolderEnv(mergeTarget), new DataHolderEnv(mergeSource));
		diffs.visit(merger);
	}
	
	/**
	 * applies changes of `modified` to `head`
	 */
	private static final class MergingDifferenceVisitor<T> implements DiffNode.Visitor
	{
		private final T head;
		private final T modified;

		public MergingDifferenceVisitor(final T head, final T modified)
		{
			this.head = head;
			this.modified = modified;
		}

		public void node(final DiffNode node, final Visit visit)
		{
			if (node.getState() == DiffNode.State.ADDED)
			{
				node.canonicalSet(head, node.canonicalGet(modified));
			}
			else if (node.getState() == DiffNode.State.REMOVED)
			{
				node.canonicalUnset(head);
				visit.dontGoDeeper();
			}
			else if (node.getState() == DiffNode.State.CHANGED)
			{
				if (node.hasChildren())
				{
					node.visitChildren(this);
					visit.dontGoDeeper();
				}
				else
				{
					node.canonicalSet(head, node.canonicalGet(modified));
				}
			}
		}
}
}
