package pers.zlf;

import java.util.Comparator;

/**
 * Compares two version strings numerically. The null reference will be regard as
 * empty string ("")<p/>
 * <b>NOTE:</b> The version string can only be dot-decimal notation, and each part
 * of them should less than {@link java.lang.Long#MIN_VALUE}
 */
public class VersionComparator implements Comparator<String> {
    @Override
    public int compare(String v1, String v2) {
        v1 = (v1 == null) ? "" : v1;
        v2 = (v2 == null) ? "" : v2;

        int len1 = v1.length();
        int len2 = v2.length();

        int idx1 = 0, idx2 = 0;
        while (idx1 < len1 || idx2 < len2) {
            int l1 = 0, l2 = 0;

            while (idx1 < len1 && v1.charAt(idx1) != '.') {
                l1 = l1 * 10 + (v1.charAt(idx1) - '0');
                idx1++;
            }
            idx1++;

            while (idx2 < len2 && v2.charAt(idx2) != '.') {
                l2 = l2 * 10 + (v2.charAt(idx2) - '0');
                idx2++;
            }
            idx2++;

            if (l1 != l2) {
                return l1 - l2;
            }
        }
        return 0;
    }
}
