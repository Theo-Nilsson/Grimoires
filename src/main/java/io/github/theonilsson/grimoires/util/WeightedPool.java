package io.github.theonilsson.grimoires.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class WeightedPool<T> {

	private final List<T> items = new ArrayList<>();
	private final List<Integer> weights = new ArrayList<>();
	private int[] cumulative;

	public static <T> WeightedPool<T> of(T item) {
		return new WeightedPool<T>().add(item, 1);
	}

	public WeightedPool<T> add(T item, int weight) {
		if (weight <= 0) {
			throw new IllegalArgumentException("Weight must be greater than 0");
		}
		items.add(item);
		weights.add(weight);
		cumulative = null;
		return this;
	}

	public int size() {	return items.size(); }
	public boolean isEmpty() { return items.isEmpty(); }

	public T pick(Random rng) {
		int[] cum = cumulative();
		return items.get(indexFor(rng.nextInt(cum[cum.length - 1]), cum));
	}

	public T pick(double u) {
		int[] cum = cumulative();
		int total = cum[cum.length - 1];
		int roll = Math.min((int) (u * total), total - 1);
		return items.get(indexFor(roll, cum));
	}

	private int[] cumulative() {
		if (items.isEmpty()) throw new IllegalStateException("No items in pool");
		if (cumulative == null) {
			int[] cum = new int[weights.size()];
			int running = 0;
			for (int i = 0; i < cum.length; i++) {
				running += weights.get(i);
				cum[i] = running;
			}
			cumulative = cum;
		}
		return cumulative;
	}

	private static int indexFor(int roll, int[] cumulative) {
		 int i = Arrays.binarySearch(cumulative, roll + 1);
		 return i >= 0 ? i : -i - 1;
	}

	public WeightedPool<T> copy() {
		WeightedPool<T> c = new WeightedPool<>();
		c.items.addAll(items);
		c.weights.addAll(weights);
		return c;
	}
}
