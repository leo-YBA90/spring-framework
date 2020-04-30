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

package org.springframework.core.annotation;

import java.lang.annotation.Annotation;

/**
 * 回调接口，可用于筛选特定的注解类型。
 *
 * Callback interface that can be used to filter specific annotation types.
 *
 * @author Phillip Webb
 * @since 5.2
 */
@FunctionalInterface
public interface AnnotationFilter {

	/**
	 * 匹配{@code java.lang}中的注解的{@link AnnotationFilter}。lang}和{@code org.springframework.lang}包及其子包。
	 *
	 * {@link AnnotationFilter} that matches annotations in the
	 * {@code java.lang} and {@code org.springframework.lang} packages
	 * and their subpackages.
	 */
	AnnotationFilter PLAIN = packages("java.lang", "org.springframework.lang");

	/**
	 * 匹配{@code java}和{@code javax}包及其子包中的注释的{@link AnnotationFilter}。
	 *
	 * {@link AnnotationFilter} that matches annotations in the
	 * {@code java} and {@code javax} packages and their subpackages.
	 */
	AnnotationFilter JAVA = packages("java", "javax");

	/**
	 * {@link AnnotationFilter}匹配全部，并且可以在完全不需要相关注释类型时使用。
	 *
	 * {@link AnnotationFilter} that always matches and can be used when no
	 * relevant annotation types are expected to be present at all.
	 */
	AnnotationFilter ALL = new AnnotationFilter() {
		@Override
		public boolean matches(Annotation annotation) {
			return true;
		}
		@Override
		public boolean matches(Class<?> type) {
			return true;
		}
		@Override
		public boolean matches(String typeName) {
			return true;
		}
		@Override
		public String toString() {
			return "All annotations filtered";
		}
	};

	/**
	 * {@link AnnotationFilter}全部不匹配
	 *
	 * {@link AnnotationFilter} that never matches and can be used when no
	 * filtering is needed (allowing for any annotation types to be present).
	 */
	AnnotationFilter NONE = new AnnotationFilter() {
		@Override
		public boolean matches(Annotation annotation) {
			return false;
		}
		@Override
		public boolean matches(Class<?> type) {
			return false;
		}
		@Override
		public boolean matches(String typeName) {
			return false;
		}
		@Override
		public String toString() {
			return "No annotation filtering";
		}
	};


	/**
	 * 测试给定的注释是否与筛选器匹配。
	 *
	 * Test if the given annotation matches the filter.
	 * @param annotation the annotation to test
	 * @return {@code true} if the annotation matches
	 */
	default boolean matches(Annotation annotation) {
		return matches(annotation.annotationType());
	}

	/**
	 * 测试给定的类型是否与筛选器匹配。
	 *
	 * Test if the given type matches the filter.
	 * @param type the annotation type to test
	 * @return {@code true} if the annotation matches
	 */
	default boolean matches(Class<?> type) {
		return matches(type.getName());
	}

	/**
	 * 测试给定的类型是否与筛选器匹配。
	 *
	 * Test if the given type name matches the filter.
	 * @param typeName the fully qualified class name of the annotation type to test
	 * @return {@code true} if the annotation matches
	 */
	boolean matches(String typeName);


	/**
	 * 创建一个新的{@link AnnotationFilter}来匹配指定包中的注解。
	 *
	 * Create a new {@link AnnotationFilter} that matches annotations in the
	 * specified packages.
	 * @param packages the annotation packages that should match
	 * @return a new {@link AnnotationFilter} instance
	 */
	static AnnotationFilter packages(String... packages) {
		return new PackagesAnnotationFilter(packages);
	}

}
