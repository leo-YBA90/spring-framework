/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.servlet.support;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.util.UrlPathHelper;

/**
 * A base class for {@link FlashMapManager} implementations.
 *
 * @author Rossen Stoyanchev
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 3.1.1
 */
public abstract class AbstractFlashMapManager implements FlashMapManager {

	private static final Object DEFAULT_FLASH_MAPS_MUTEX = new Object();


	protected final Log logger = LogFactory.getLog(getClass());

	private int flashMapTimeout = 180;

	private UrlPathHelper urlPathHelper = new UrlPathHelper();


	/**
	 * Set the amount of time in seconds after a {@link FlashMap} is saved
	 * (at request completion) and before it expires.
	 * <p>The default value is 180 seconds.
	 */
	public void setFlashMapTimeout(int flashMapTimeout) {
		this.flashMapTimeout = flashMapTimeout;
	}

	/**
	 * Return the amount of time in seconds before a FlashMap expires.
	 */
	public int getFlashMapTimeout() {
		return this.flashMapTimeout;
	}

	/**
	 * Set the UrlPathHelper to use to match FlashMap instances to requests.
	 */
	public void setUrlPathHelper(UrlPathHelper urlPathHelper) {
		Assert.notNull(urlPathHelper, "UrlPathHelper must not be null");
		this.urlPathHelper = urlPathHelper;
	}

	/**
	 * Return the UrlPathHelper implementation to use.
	 */
	public UrlPathHelper getUrlPathHelper() {
		return this.urlPathHelper;
	}


	/**
	 * 检索更新List<FlashMap>
	 * @param request the current request
	 * @param response the current response
	 * @return
	 */
	@Override
	@Nullable
	public final FlashMap retrieveAndUpdate(HttpServletRequest request, HttpServletResponse response) {
		// 从存储介质中获取List<FlashMap>，是模板方法，子类实现
		List<FlashMap> allFlashMaps = retrieveFlashMaps(request);
		if (CollectionUtils.isEmpty(allFlashMaps)) {
			return null;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Retrieved FlashMap(s): " + allFlashMaps);
		}
		// 检查过去flashMap，并将它们设置到mapsToRemove变量
		List<FlashMap> mapsToRemove = getExpiredFlashMaps(allFlashMaps);
		// 获取与当前request匹配的flashMap，并设置到match变量
		FlashMap match = getMatchingFlashMap(allFlashMaps, request);
		// 如果有匹配的则将其添加到mapsToRemove,待下面删除
		if (match != null) {
			mapsToRemove.add(match);
		}
		// 删除mapsToRemove中保存的变量
		if (!mapsToRemove.isEmpty()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Removing FlashMap(s): " + mapsToRemove);
			}
			Object mutex = getFlashMapsMutex(request);
			if (mutex != null) {
				synchronized (mutex) {
					allFlashMaps = retrieveFlashMaps(request);
					if (allFlashMaps != null) {
						allFlashMaps.removeAll(mapsToRemove);
						updateFlashMaps(allFlashMaps, request, response);
					}
				}
			} else {
				allFlashMaps.removeAll(mapsToRemove);
				updateFlashMaps(allFlashMaps, request, response);
			}
		}

		return match;
	}

	/**
	 * Return a list of expired FlashMap instances contained in the given list.
	 */
	private List<FlashMap> getExpiredFlashMaps(List<FlashMap> allMaps) {
		List<FlashMap> result = new LinkedList<>();
		for (FlashMap map : allMaps) {
			if (map.isExpired()) {
				result.add(map);
			}
		}
		return result;
	}

	/**
	 * Return a FlashMap contained in the given list that matches the request.
	 * @return a matching FlashMap or {@code null}
	 */
	@Nullable
	private FlashMap getMatchingFlashMap(List<FlashMap> allMaps, HttpServletRequest request) {
		List<FlashMap> result = new LinkedList<>();
		for (FlashMap flashMap : allMaps) {
			if (isFlashMapForRequest(flashMap, request)) {
				result.add(flashMap);
			}
		}
		if (!result.isEmpty()) {
			Collections.sort(result);
			if (logger.isDebugEnabled()) {
				logger.debug("Found matching FlashMap(s): " + result);
			}
			return result.get(0);
		}
		return null;
	}

	/**
	 * Whether the given FlashMap matches the current request.
	 * Uses the expected request path and query parameters saved in the FlashMap.
	 */
	protected boolean isFlashMapForRequest(FlashMap flashMap, HttpServletRequest request) {
		String expectedPath = flashMap.getTargetRequestPath();
		if (expectedPath != null) {
			String requestUri = getUrlPathHelper().getOriginatingRequestUri(request);
			if (!requestUri.equals(expectedPath) && !requestUri.equals(expectedPath + "/")) {
				return false;
			}
		}
		MultiValueMap<String, String> actualParams = getOriginatingRequestParams(request);
		MultiValueMap<String, String> expectedParams = flashMap.getTargetRequestParams();
		for (String expectedName : expectedParams.keySet()) {
			List<String> actualValues = actualParams.get(expectedName);
			if (actualValues == null) {
				return false;
			}
			for (String expectedValue : expectedParams.get(expectedName)) {
				if (!actualValues.contains(expectedValue)) {
					return false;
				}
			}
		}
		return true;
	}

	private MultiValueMap<String, String> getOriginatingRequestParams(HttpServletRequest request) {
		String query = getUrlPathHelper().getOriginatingQueryString(request);
		return ServletUriComponentsBuilder.fromPath("/").query(query).build().getQueryParams();
	}

	/**
	 * 保存FlashMap过程
	 * @param flashMap the FlashMap to save
	 * @param request the current request
	 * @param response the current response
	 */
	@Override
	public final void saveOutputFlashMap(FlashMap flashMap, HttpServletRequest request, HttpServletResponse response) {
		if (CollectionUtils.isEmpty(flashMap)) {
			return;
		}
		// 首先对flashMap中的转发地址和参数进行编码，这里的request主要用来获取当前编码方式
		String path = decodeAndNormalizePath(flashMap.getTargetRequestPath(), request);
		flashMap.setTargetRequestPath(path);

		if (logger.isDebugEnabled()) {
			logger.debug("Saving FlashMap=" + flashMap);
		}
		// 设置有效期，默认180s
		flashMap.startExpirationPeriod(getFlashMapTimeout());
		// 用于获取互斥变量，是模板方法，如果子类返回值不为null，则同步执行，否则不需要同步
		Object mutex = getFlashMapsMutex(request);
		if (mutex != null) {
			synchronized (mutex) {
				// 取回保存的List<FlashMap>，如果没有获取到则新建一个，然后添加现有的flashMap
				// retrieveFlashMaps方法用于获取List<FlashMap>,是模板方法，子类实现
				List<FlashMap> allFlashMaps = retrieveFlashMaps(request);
				allFlashMaps = (allFlashMaps != null ? allFlashMaps : new CopyOnWriteArrayList<>());
				allFlashMaps.add(flashMap);
				updateFlashMaps(allFlashMaps, request, response);
			}
		} else {
			List<FlashMap> allFlashMaps = retrieveFlashMaps(request);
			allFlashMaps = (allFlashMaps != null ? allFlashMaps : new LinkedList<>());
			allFlashMaps.add(flashMap);
			updateFlashMaps(allFlashMaps, request, response);
		}
	}

	@Nullable
	private String decodeAndNormalizePath(@Nullable String path, HttpServletRequest request) {
		if (path != null && !path.isEmpty()) {
			path = getUrlPathHelper().decodeRequestString(request, path);
			if (path.charAt(0) != '/') {
				String requestUri = getUrlPathHelper().getRequestUri(request);
				path = requestUri.substring(0, requestUri.lastIndexOf('/') + 1) + path;
				path = StringUtils.cleanPath(path);
			}
		}
		return path;
	}

	/**
	 * Retrieve saved FlashMap instances from the underlying storage.
	 * @param request the current request
	 * @return a List with FlashMap instances, or {@code null} if none found
	 */
	@Nullable
	protected abstract List<FlashMap> retrieveFlashMaps(HttpServletRequest request);

	/**
	 * Update the FlashMap instances in the underlying storage.
	 * @param flashMaps a (potentially empty) list of FlashMap instances to save
	 * @param request the current request
	 * @param response the current response
	 */
	protected abstract void updateFlashMaps(
			List<FlashMap> flashMaps, HttpServletRequest request, HttpServletResponse response);

	/**
	 * Obtain a mutex for modifying the FlashMap List as handled by
	 * {@link #retrieveFlashMaps} and {@link #updateFlashMaps},
	 * <p>The default implementation returns a shared static mutex.
	 * Subclasses are encouraged to return a more specific mutex, or
	 * {@code null} to indicate that no synchronization is necessary.
	 * @param request the current request
	 * @return the mutex to use (may be {@code null} if none applicable)
	 * @since 4.0.3
	 */
	@Nullable
	protected Object getFlashMapsMutex(HttpServletRequest request) {
		return DEFAULT_FLASH_MAPS_MUTEX;
	}

}
