package mx.decants.util;

public final class SlugUtil {

    private SlugUtil() {}

    public static String slugify(String texto) {
        return texto.toLowerCase()
            .replaceAll("[áàäâã]", "a").replaceAll("[éèëê]", "e")
            .replaceAll("[íìïî]", "i").replaceAll("[óòöôõ]", "o")
            .replaceAll("[úùüû]", "u").replaceAll("[ñ]", "n")
            .replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }
}