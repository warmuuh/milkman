package milkman.plugin.sync.git;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.danielbechler.diff.node.DiffNode;
import de.danielbechler.diff.node.Visit;
import milkman.domain.Collection;
import milkman.plugin.sync.git.CollectionDiffer.DataHolder;


import static org.assertj.core.api.Assertions.*;

public class DiffTest {

	

	@ParameterizedTest
	@ValueSource(strings = {
			"change-url", 
			"add-collection", 
			"remove-collection",
			"add-header",
			"change-header" // 2 diffs (removed, added) because no identity defined for header-entries and equals = false 
			})
	public void shouldOnlyFindOneDiff(String dir) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		List<Collection> remoteCollections = mapper.readValue(getClass().getResourceAsStream("/"+dir+"/basecopy.json"),
				new TypeReference<List<Collection>>() {
				});
		List<Collection> workingCollection = mapper.readValue(getClass().getResourceAsStream("/"+dir+"/workingcopy.json"),
				new TypeReference<List<Collection>>() {
				});

		
		CollectionDiffer differ = new CollectionDiffer();
		DiffNode diffNode = differ.compare(workingCollection, remoteCollections);
		String prettyDiff = printDiffs(remoteCollections, workingCollection, diffNode);

		String expected = IOUtils.toString(getClass().getResourceAsStream("/"+dir+"/diffs.txt"));
		assertThat(prettyDiff.replaceAll("\\r\\n|\\r|\\n", " "))
		.withFailMessage("diffs are not as expected: \n" + prettyDiff)
		.isEqualTo(expected.replaceAll("\\r\\n|\\r|\\n", " "));
	}
	
	
	@Test
	public void shouldMergeCorrectlyRenameCollection() throws JsonParseException, JsonMappingException, IOException {
		String colId = UUID.randomUUID().toString();
		
		List<Collection> base = new LinkedList<Collection>();
		base.add(new Collection(colId, "collection1", false, new LinkedList<>(), Collections.emptyList()));
		
		List<Collection> working = new LinkedList<Collection>();
		working.add(new Collection(colId, "collection2", false, new LinkedList<>(), Collections.emptyList()));
		
		CollectionDiffer collectionDiffer = new CollectionDiffer();
		DiffNode diffNode = collectionDiffer.compare(working, base);
		
		collectionDiffer.mergeDiffs(working, base, diffNode);
		
		assertThat(base.size()).isEqualTo(1);
		assertThat(base.get(0).getId()).isEqualTo(colId);
		assertThat(base.get(0).getName()).isEqualTo("collection2");
		
	}
	
	
	@Test
	public void shouldMergeCorrectlyAddCollection() throws JsonParseException, JsonMappingException, IOException {
		String colId = UUID.randomUUID().toString();
		String colId2 = UUID.randomUUID().toString();
		
		List<Collection> base = new LinkedList<Collection>();
		base.add(new Collection(colId, "collection1", false, new LinkedList<>(), Collections.emptyList()));
		
		List<Collection> working = new LinkedList<Collection>();
		working.add(new Collection(colId, "collection1", false, new LinkedList<>(), Collections.emptyList()));
		working.add(new Collection(colId2, "collection2", false, new LinkedList<>(), Collections.emptyList()));
		
		CollectionDiffer collectionDiffer = new CollectionDiffer();
		DiffNode diffNode = collectionDiffer.compare(working, base);
		
		collectionDiffer.mergeDiffs(working, base, diffNode);
		
		assertThat(base.size()).isEqualTo(2);
		assertThat(base.get(0).getId()).isEqualTo(colId);
		assertThat(base.get(0).getName()).isEqualTo("collection1");

		assertThat(base.get(1).getId()).isEqualTo(colId2);
		assertThat(base.get(1).getName()).isEqualTo("collection2");
		
	}
	

	@Test
	public void shouldMergeBooleansToFalse() throws JsonParseException, JsonMappingException, IOException {
		String colId = UUID.randomUUID().toString();
		
		List<Collection> base = new LinkedList<Collection>();
		base.add(new Collection(colId, "collection1", true, new LinkedList<>(), Collections.emptyList()));
		
		List<Collection> working = new LinkedList<Collection>();
		working.add(new Collection(colId, "collection1", false, new LinkedList<>(), Collections.emptyList()));
		
		CollectionDiffer collectionDiffer = new CollectionDiffer();
		DiffNode diffNode = collectionDiffer.compare(working, base);
		
		collectionDiffer.mergeDiffs(working, base, diffNode);
		assertThat(base.get(0).isStarred()).isFalse();
		
		
	}
	
	
	
	private String printDiffs(List<Collection> remoteCollections, List<Collection> workingCollection,
			DiffNode diffNode) {
		StringBuilder b = new StringBuilder();
		
		if (diffNode.hasChanges()) {
			diffNode.visit(new DiffNode.Visitor() {
				public void node(DiffNode node, Visit visit) {
					
					if (!node.hasChanges())
						return;
					if (node.isAdded() || node.isRemoved()) {
						addDataChange(remoteCollections, workingCollection, b, node);
						visit.dontGoDeeper();
					}
					
					if (!node.hasChildren()) {
						addDataChange(remoteCollections, workingCollection, b, node);
					}
				}

				private void addDataChange(List<Collection> remoteCollections, List<Collection> workingCollection,
						StringBuilder b, DiffNode node) {
					final Object baseValue = node.canonicalGet(new DataHolder(remoteCollections));
					final Object workingValue = node.canonicalGet(new DataHolder(workingCollection));

					final String message = node.getPath() + " [" + baseValue + " -> " + workingValue + "]";
					b.append(message);
					b.append("\n");
				}
			});
		} else {
			b.append("No changes");
		}
		return b.toString();
	}
}
