package io.github.darkona.logged.utils;

import jakarta.annotation.Nullable;
import org.springframework.stereotype.Component;

import java.text.BreakIterator;
import java.util.Arrays;
import java.util.Locale;

/**
 * Utilidades de transformación y formateo de texto usadas por la librería de logging.
 *
 * <p>Diseñadas para ser baratas en CPU y GC en los caminos calientes (hot path) de logging:
 * evitan asignaciones innecesarias, usan rutas rápidas para tipos comunes y reservan
 * operaciones más costosas (por ejemplo, manejo de grafemas) para métodos específicos
 * que no se invocan por defecto.</p>
 */
@SuppressWarnings("unused")
@Component
public class Transformer {

    private Transformer() {}

    /**
     * Devuelve una representación segura en texto del objeto recibido.
     *
     * - Rutas rápidas sin try/catch para {@link CharSequence}, wrappers numéricos,
     *   {@link Boolean} y {@link Character}.
     * - Soporte para arreglos: usa {@code Arrays.toString/deepToString} según corresponda.
     * - Fallback seguro con {@code toString()} dentro de try/catch (evita romper el logging si el toString lanza).
     * - No trunca: la truncación se realiza en los llamadores (p. ej., LoggedEngine) para no duplicar costo.
     *
     * @param o objeto a representar
     * @return texto representando al objeto; "null" si el objeto es nulo
     */
    public static String objectString(Object o) {
        if (o == null) return "null";
        if (o instanceof CharSequence cs) return cs.toString();
        if (o instanceof Number || o instanceof Boolean || o instanceof Character) return String.valueOf(o);
        // Arrays: render contents instead of identity hash
        Class<?> c = o.getClass();
        if (c.isArray()) {
            if (o instanceof Object[] arr) return Arrays.deepToString(arr);
            if (o instanceof int[] a) return Arrays.toString(a);
            if (o instanceof long[] a) return Arrays.toString(a);
            if (o instanceof double[] a) return Arrays.toString(a);
            if (o instanceof float[] a) return Arrays.toString(a);
            if (o instanceof boolean[] a) return Arrays.toString(a);
            if (o instanceof byte[] a) return Arrays.toString(a);
            if (o instanceof short[] a) return Arrays.toString(a);
            if (o instanceof char[] a) return Arrays.toString(a);
        }
        try {
            return o.toString();
        } catch (Throwable t) {
            return "toString Error: " + c.getSimpleName();
        }
    }

    /**
     * Repite la cadena indicada {@code amount} veces.
     *
     * @param s      patrón a repetir (no nulo)
     * @param amount cantidad de repeticiones (si es ≤ 0 retorna "")
     * @return la cadena repetida
     */
    public static String fill(String s, int amount) {
        return s.repeat(Math.max(0, amount));
    }

    /**
     * Enmascara una cadena preservando opcionalmente los primeros {@code unmasked} caracteres.
     * No crea {@code char[]} cuando enmascara todo; usa {@code repeat} para minimizar asignaciones.
     *
     * @param string   entrada (puede ser nula → retorna null)
     * @param unmasked cantidad de caracteres iniciales sin enmascarar (nulo o &lt;0 → 0)
     * @param maskChar carácter de máscara (nulo → '*')
     * @return cadena enmascarada o null si {@code string} es null
     */
    public static String mask(String string, @Nullable Integer unmasked, @Nullable Character maskChar) {
        if (string == null) return null;
        int keep = (unmasked == null || unmasked < 0) ? 0 : unmasked;
        char m = (maskChar == null) ? '*' : maskChar;
        int len = string.length();
        if (keep <= 0) return String.valueOf(m).repeat(len);
        if (keep >= len) return string;
        StringBuilder sb = new StringBuilder(len);
        sb.append(string, 0, keep);
        sb.append(String.valueOf(m).repeat(len - keep));
        return sb.toString();
    }

    /**
     * Sobrecarga sin boxing para {@link #mask(String, Integer, Character)}.
     */
    public static String mask(String string, int unmasked, char maskChar) {
        return mask(string, Integer.valueOf(unmasked), Character.valueOf(maskChar));
    }

    /**
     * Enmascara un arreglo de caracteres preservando opcionalmente los primeros {@code unmasked}.
     * Preasigna capacidad y evita ramas por carácter cuando es posible.
     *
     * @param bytes    caracteres de entrada (puede ser null → retorna null)
     * @param unmasked cantidad de caracteres iniciales sin enmascarar (nulo o &lt;0 → 0)
     * @param maskChar carácter de máscara (nulo → '*')
     * @return cadena enmascarada, o copia de {@code bytes} si {@code unmasked ≥ length}
     */
    public static String mask(char[] bytes, @Nullable Integer unmasked, @Nullable Character maskChar) {
        if (bytes == null) return null;
        int keep = (unmasked == null || unmasked < 0) ? 0 : unmasked;
        char m = (maskChar == null) ? '*' : maskChar;
        int len = bytes.length;
        if (keep >= len) return new String(bytes);
        StringBuilder sb = new StringBuilder(len);
        if (keep > 0) sb.append(bytes, 0, keep);
        sb.append(String.valueOf(m).repeat(Math.max(0, len - keep)));
        return sb.toString();
    }

    /**
     * Sobrecarga sin boxing para {@link #mask(char[], Integer, Character)}.
     */
    public static String mask(char[] bytes, int unmasked, char maskChar) {
        return mask(bytes, Integer.valueOf(unmasked), Character.valueOf(maskChar));
    }

    /**
     * Sufijo ordinal en inglés para un día del mes (st, nd, rd, th).
     *
     * @param day día del mes
     * @return sufijo ordinal correspondiente
     */
    public static String daySuffix(int day) {
        if (day >= 11 && day <= 13) return "th";
        return switch (day % 10) {
            case 1 -> "st";
            case 2 -> "nd";
            case 3 -> "rd";
            default -> "th";
        };
    }

    /**
     * Subcadena entre índices {@code begin} (incluido) y {@code end} (excluido).
     * Retorna "" si la entrada es nula o vacía; retorna la original si los índices no son válidos.
     */
    public static String getSubstring(String str, int begin, int end) {
        if (str == null || str.isEmpty()) return "";
        return (end <= str.length() && begin < end && begin >= 0) ? str.substring(begin, end) : str;
    }

    /**
     * Subcadena desde {@code begin} hasta la primera ocurrencia de {@code delimiter} (excluido).
     * Si no existe el delimitador, retorna {@code str.trim()}.
     */
    public static String getSubstringUntil(String str, int begin, String delimiter) {
        if (str == null || str.isEmpty()) return "";
        int from = Math.max(0, begin);
        int idx = str.indexOf(delimiter, from);
        if (idx == -1) return str.trim();
        return (from < idx) ? str.substring(from, idx) : "";
    }

    /**
     * Capitaliza el primer carácter (versión ASCII/inglés; no locale-aware).
     * Mantiene el resto de la cadena sin copiar más de lo necesario.
     */
    public static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        char first = Character.toUpperCase(s.charAt(0));
        if (s.length() == 1) return String.valueOf(first);
        StringBuilder sb = new StringBuilder(s.length());
        sb.append(first).append(s, 1, s.length());
        return sb.toString();
    }

    /** Carácter de puntos suspensivos usado por {@link #truncate(String, int)}. */
    private static final String ELLIPSIS = "…";

    /**
     * Trunca la cadena a lo más {@code max} caracteres y agrega un {@link #ELLIPSIS} al final
     * si hubo truncamiento (de modo que la longitud resultante sea &gt; {@code max}).
     *
     * @param s   entrada (puede ser null → retorna null)
     * @param max longitud máxima previa al agregado del sufijo
     * @return cadena truncada con sufijo o la original si no requiere truncar
     */
    public static String truncate(String s, int max) {
        if (s == null) return null;
        if (max <= 0) return "";
        if (s.length() <= max) return s;
        // Keep up to max chars, then append ellipsis so total length > max
        return s.substring(0, max) + ELLIPSIS;
    }

    /**
     * Trunca por grupos de grafemas (caracteres percibidos por el usuario) usando {@link BreakIterator}.
     * Costoso; úsalo solo cuando realmente necesites no partir emojis/combining marks.
     *
     * @param s           entrada (null → "")
     * @param maxClusters máximo de grafemas
     * @return subcadena limitada a {@code maxClusters} grafemas
     * @throws IllegalArgumentException si {@code maxClusters} &lt; 0
     */
    public static String truncateGraphemes(String s, int maxClusters) {
        if (s == null) return "";
        if (maxClusters < 0) throw new IllegalArgumentException("maxClusters cannot be a negative number");
        if (s.length() <= maxClusters) return s;
        BreakIterator bi = BreakIterator.getCharacterInstance(Locale.ROOT);
        bi.setText(s);
        int end = bi.first();
        for (int i = 0; i < maxClusters; i++) {
            int next = bi.next();
            if (next == BreakIterator.DONE) return s;
            end = next;
        }
        return s.substring(0, end);
    }
}
