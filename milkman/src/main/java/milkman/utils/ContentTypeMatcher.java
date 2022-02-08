package milkman.utils;

import okhttp3.MediaType;

import java.util.Locale;

public class ContentTypeMatcher {

    /**
     * lightweight content type matching, only matches by checking type and subtype/suffix
     */
    public static boolean matches(String contentType, String contentTypeToMatch) {
        MediaType mediaType = MediaType.parse(contentType);
        MediaType mediaTypeToMatch = MediaType.parse(contentTypeToMatch);
        return mediaTypeToMatch.type().equalsIgnoreCase(mediaType.type())
                && mediaTypeToMatch.subtype().toLowerCase().contains(mediaType.subtype().toLowerCase());
    }

}
