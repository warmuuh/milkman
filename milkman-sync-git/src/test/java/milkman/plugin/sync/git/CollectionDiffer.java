package milkman.plugin.sync.git;

import static org.assertj.core.api.Assertions.assertThat;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.danielbechler.diff.ObjectDifferBuilder;
import de.danielbechler.diff.identity.IdentityStrategy;
import de.danielbechler.diff.node.DiffNode;
import de.danielbechler.diff.node.Visit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import milkman.domain.Collection;

public class CollectionDiffer {

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

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class DataHolder {
		List<Collection> collections;
	}
	
	public DiffNode compare(List<Collection> working, List<Collection> base) throws JsonParseException, JsonMappingException, IOException {
		DiffNode diffNode = ObjectDifferBuilder.startBuilding()
//				.comparison()
//					.ofType(RestRequestContainer.class).toUseEqualsMethodOfValueProvidedByMethod("getId")
//					.ofType(Collection.class).toUseEqualsMethodOfValueProvidedByMethod("getName")
//				.and()
				.inclusion()
					.exclude().propertyName("onDirtyChange")
							  .propertyName("onInvalidate")
				.and()
				.identity().ofCollectionItems(Collection.class, "requests").via(new PropertyBasedIdentityStrategy("id"))
						   .ofCollectionItems(DataHolder.class, "collections").via(new PropertyBasedIdentityStrategy("id"))
				.and()
				.build()
				.compare(new DataHolder(working), new DataHolder(base));
		
		return diffNode;
	}
	
	public void mergeDiffs(List<Collection> mergeSource, List<Collection> mergeTarget, DiffNode diffs) {
		val merger = new MergingDifferenceVisitor<>(new DataHolder(mergeTarget), new DataHolder(mergeSource));
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
