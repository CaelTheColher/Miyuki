package br.com.brjdevs.miyuki.lib;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CollectionUtils {
	public static <T, U> Map<T, U> concatMaps(Map<T, U> map1, Map<T, U> map2) {
		return Stream.concat(map1.entrySet().stream(), map2.entrySet().stream())
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				Map.Entry::getValue,
				(entry1, entry2) -> (entry1 == null ? entry2 : entry1)
				)
			);
	}

	public static Iterable<String> iterate(Matcher matcher) {
		return new Iterable<String>() {
			@Override
			public Iterator<String> iterator() {
				return new Iterator<String>() {
					@Override
					public boolean hasNext() {
						return matcher.find();
					}

					@Override
					public String next() {
						return matcher.group();
					}
				};
			}

			@Override
			public void forEach(Consumer<? super String> action) {
				while (matcher.find()) {
					action.accept(matcher.group());
				}
			}
		};
	}

	public static <T> List<T> subListOn(List<T> list, Predicate<T> predicate) {
		Optional<T> first = list.stream().filter(predicate).findFirst();
		if (!first.isPresent()) return list;
		return list.subList(0, list.indexOf(first.get()));
	}

	public static <T> T random(List<T> list, Random random) {
		return list.get(random.nextInt(list.size()));
	}

	public static <T> T random(T[] array, Random random) {
		return array[random.nextInt(array.length)];
	}

	public static <T> T random(List<T> list) {
		return list.get((int) Math.floor(Math.random() * list.size()));
	}

	public static <T> T random(T[] array) {
		return array[(int) Math.floor(Math.random() * array.length)];
	}

	public static <T, R> List<R> apply(List<T> list, Function<T, R> mapper) {
		return list.stream().map(mapper).collect(Collectors.toList());
	}

	public static <T> String toString(Collection<T> collection, Function<T, String> toString, String join) {
		return String.join(join, collection.stream().map(toString).toArray(String[]::new));
	}
}

