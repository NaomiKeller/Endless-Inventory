package com.kwwsyk.endinv.common.client.option;

import com.kwwsyk.endinv.common.menu.page.PageType;

/**Caches that keep pages' options, including:<br>
 * <ul>
 *     <li>options toggled in page (sort, search...)</li>
 *     <li>data synced from server (attaching)</li>
 * </ul>
 */
public class ConfigCache {

    private boolean serverAllowAttaching;

    public static PageType currentPage = PageType.ALL_ITEMS;
    public static boolean reverseSort = false;
}
