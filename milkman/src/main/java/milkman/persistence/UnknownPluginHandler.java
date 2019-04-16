package milkman.persistence;

import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.ReferenceType;

import lombok.extern.slf4j.Slf4j;
import milkman.domain.RequestAspect;
import milkman.domain.RequestContainer;
import milkman.domain.RequestAspect.UnknownRequestAspect;
import milkman.domain.RequestContainer.UnknownRequestContainer;

@Slf4j
public final class UnknownPluginHandler extends DeserializationProblemHandler {
	@Override
	public JavaType handleUnknownTypeId(DeserializationContext ctxt, JavaType baseType, String subTypeId,
			TypeIdResolver idResolver, String failureMsg) throws IOException {
		if (baseType.hasRawClass(RequestAspect.class)) {
			log.error("Unknown AspectType found: " + subTypeId + ".");
			return ReferenceType.construct(UnknownRequestAspect.class);
		}
		if (baseType.hasRawClass(RequestContainer.class)) {
			log.error("Unknown RequestContainer found: " + subTypeId + ".");
			return ReferenceType.construct(UnknownRequestContainer.class);
		}
		return null;
	}
}