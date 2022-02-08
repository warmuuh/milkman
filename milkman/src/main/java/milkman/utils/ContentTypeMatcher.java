package milkman.utils;

import okhttp3.MediaType;

import java.util.Optional;

public class ContentTypeMatcher {

    /**
     * lightweight content type matching, only matches by checking type and subtype/suffix
     */
    public static boolean matches(String contentType, String contentTypeToMatch) {
        Optional<MediaType> mediaType = Optional.ofNullable(MediaType.parse(contentType));
        Optional<MediaType> mediaTypeToMatch =  Optional.ofNullable(MediaType.parse(contentTypeToMatch));

        return mediaType.flatMap(mt ->
                mediaTypeToMatch.map(mttm ->
                        matches(mt, mttm)
                )).orElse(false);

//        return mediaTypeToMatch.type().equalsIgnoreCase(mediaType.type())
//                && mediaTypeToMatch.subtype().toLowerCase().contains(mediaType.subtype().toLowerCase());
    }

    private static boolean matches(MediaType mt, MediaType mttm) {
        return (mt.type().equals("*") || mttm.type().equalsIgnoreCase(mt.type()))
                && mttm.subtype().toLowerCase().contains(mt.subtype().toLowerCase());
    }

}
